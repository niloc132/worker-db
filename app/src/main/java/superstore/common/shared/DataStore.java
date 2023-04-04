package superstore.common.shared;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.index.support.KeyStatisticsAttributeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;
import elemental2.dom.DomGlobal;
import superstore.common.shared.attribute.AbstractMapAttribute;
import superstore.common.shared.attribute.MapIntegerAttribute;
import superstore.common.shared.attribute.MapStringAttribute;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Simple data store that models how the client and server will interact with data, allowing for remote
 * interactions, such as either data changes arriving or data being queried.
 *
 * TODO consider making this generic and factoring out the indexes/schema
 */
public class DataStore {

    public interface QueryListener {
        /**
         * Currently indicates that more data is loading, the user should wait a bit
         */
        void dataLoadedFromDisk(String schema, int rows);

        /**
         * Intended to signal the presence of a table and its columns that can be used in other generic operations
         * @param name
         * @param columns
         */
        void schemaLoaded(String name, Map<String, AbstractMapAttribute<?>> columns);

        /**
         * First response to a call to {@link QueryConnection#runQuery(Query, QueryOptions)},
         * signaling that the query is finished, the size of the results, and the number of values
         * that are on their way.
         */
        void queryFinished(Query<Map<String, String>> query, int totalCount);

        /**
         * Batch response of results from a {@link QueryConnection#runQuery(Query, QueryOptions)}.
         */
        void queryResults(Query<Map<String, String>> query, List<Map<String, String>> results, int offset);

        /**
         * Follow-up response to a finished query, indicating that more results have become available
         */
        void additionalQueryResults(Query<Map<String, String>> query, List<Map<String, String>> items);

        /**
         * First response to a {@link QueryConnection#loadUniqueKeysForColumn(String, AbstractMapAttribute)},
         * indicating how many unique values are present.
         */
        void uniqueKeysLoaded(Attribute<Map<String, String>, ?> attribute, int totalCount);

        /**
         * Batch response of results from a {@link QueryConnection#loadUniqueKeysForColumn(String, AbstractMapAttribute)}
         * call, listing the results that were found.
         */
        void uniqueKeysResults(Attribute<Map<String, String>, ?> attribute, List<?> results, int offset);
    }

    public interface QueryConnection {
        void runQuery(Query<Map<String, String>> query, QueryOptions options);
        void loadUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute);

        void close();
    }
    private static final MapStringAttribute dateAttribute = new MapStringAttribute("Date");
    private static final MapIntegerAttribute hourAttribute = new MapIntegerAttribute("Hour");

    private static final Attribute<Map<String, String>, String> timestampAttribute = new SimpleAttribute<Map<String, String>, String>((Class<Map<String, String>>) (Class) Map.class, String.class) {
        @Override
        public String getValue(Map<String, String> object, QueryOptions queryOptions) {
            return dateAttribute.getValue(object, queryOptions) + "T" + hourAttribute.getValue(object, queryOptions);
        }
    };

    private IndexedCollection<Map<String, String>> data = new ConcurrentIndexedCollection<>();
    private Map<String, AbstractMapAttribute<?>> columns;
    private Set<QueryListener> activeListeners = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public DataStore() {

        data.addIndex(NavigableIndex.onAttribute(dateAttribute));
        //TODO partial index on each station for its date and hour?
        data.addIndex(HashIndex.onAttribute(new MapStringAttribute("STA")));
        data.addIndex(HashIndex.onAttribute(new MapStringAttribute("Direction")));

        //one more attribute, to get us the ability to pretend-replay history
        data.addIndex(NavigableIndex.onAttribute(timestampAttribute));

        columns = new HashMap<>();
        columns.put("Date", new MapStringAttribute("Date"));
        columns.put("STA", new MapStringAttribute("STA"));
        columns.put("Hour", new MapIntegerAttribute("Hour"));
        columns.put("Value", new MapIntegerAttribute("Value"));
        columns.put("Direction", new MapStringAttribute("Direction"));
    }

    public Map<String, AbstractMapAttribute<?>> getColumns() {
        return columns;
    }

    public void addAll(List<Map<String, String>> rows) {
        data.addAll(rows);
        for (QueryListener listener : activeListeners) {
            listener.dataLoadedFromDisk("traffic", size());
        }
    }

    public void clear() {
        data.clear();
    }

    public int size() {
        return data.size();
    }

    public QueryConnection connect(QueryListener listener) {
        activeListeners.add(listener);
        listener.schemaLoaded("traffic", columns);

        return new QueryConnection() {
            private volatile Query<Map<String, String>> activeQuery;
            private QueryOptions activeQueryOptions;

            @Override
            public void runQuery(Query<Map<String, String>> query, QueryOptions options) {
                activeQuery = query;//TODO helper to handle updates?
                activeQueryOptions = options;
                //lastResultsSent = now
                ResultSet<Map<String, String>> results = data.retrieve(query, options);
                int size = results.size();
                listener.queryFinished(activeQuery, size);
                if (size == 0) {
                    return;
                }
                Iterator<Map<String, String>> iterator = results.iterator();
                int offset = 0;
                while (/*query.equals(activeQuery) && */iterator.hasNext()) {
                    listener.queryResults(activeQuery, Lists.newArrayList(Iterators.limit(iterator, 10000)), offset);
                    offset += 10000;
                }
            }

            @Override
            public void loadUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute) {
                assert schema.equals("traffic");

                Optional<KeyStatisticsAttributeIndex<Object, Map<String, String>>> hasIndex = Streams.stream(data.getIndexes())
                        .filter(index -> index instanceof KeyStatisticsAttributeIndex)
                        .map(index -> (KeyStatisticsAttributeIndex<Object, Map<String, String>>) index)
                        .filter(index -> index.getAttribute().equals(attribute))
                        .findFirst();
                if (hasIndex.isPresent()) {
                    DomGlobal.console.log("index present");
                    int count = hasIndex.get().getCountOfDistinctKeys(QueryFactory.noQueryOptions());
                    listener.uniqueKeysLoaded(attribute, count);

                    CloseableIterator<Object> iterator = hasIndex.get().getDistinctKeys(QueryFactory.noQueryOptions()).iterator();
                    int offset = 0;
                    while (iterator.hasNext()) {
                        listener.uniqueKeysResults(attribute, Lists.newArrayList(Iterators.limit(iterator, 1000)), offset);
                        offset += 1000;
                    }
                } else {
                    listener.uniqueKeysLoaded(attribute, 0);
                }
            }

            @Override
            public void close() {

            }
        };
    }

}
