package superstore.server;

import com.colinalworth.gwt.websockets.server.AbstractServerImpl;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gwt.core.client.Callback;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.AttributeIndex;
import com.googlecode.cqengine.index.Index;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.index.support.KeyStatisticsAttributeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import com.googlecode.cqengine.query.parser.cqn.CQNParser;
import com.googlecode.cqengine.resultset.ResultSet;
import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import superstore.common.shared.StoreClient;
import superstore.common.shared.StoreServer;
import superstore.common.shared.attribute.AbstractMapAttribute;
import superstore.common.shared.attribute.MapIntegerAttribute;
import superstore.common.shared.attribute.MapStringAttribute;

import javax.websocket.server.ServerEndpoint;
import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

@ServerEndpoint("/socket")
public class Server extends AbstractServerImpl<StoreServer, StoreClient> implements StoreServer {
    private IndexedCollection<Map<String, String>> trafficData = new ConcurrentIndexedCollection<>();
    private static final int batchSize = 50_000;

    /** currently executing and updated query for this instance's active connection */
    private volatile Query<?> activeQuery;

    public Server() {
        super(StoreClient.class);
    }

    @Override
    public void onOpen(Connection connection, StoreClient client) {
        super.onOpen(connection, client);

        trafficData.addIndex(NavigableIndex.onAttribute(new MapStringAttribute("Date")));
        //TODO partial index on each station for its date and hour?
        trafficData.addIndex(HashIndex.onAttribute(new MapStringAttribute("STA")));
//        trafficData.addIndex(HashIndex.onAttribute(new MapStringAttribute("Direction")));

        //load data from disk
        File csv = new File("/Users/colin/Downloads/mn_dot_traffic_5m.csv");
        long totalLength = csv.length();

        try (CountingFileReader reader = (new CountingFileReader(csv))) {
            try (CSVParser parser = new CSVParser(new BufferedReader(reader), CSVFormat.RFC4180.withHeader())) {
                Iterator<CSVRecord> iterator = parser.iterator();
                while (iterator.hasNext()) {
                    List<Map<String, String>> page = Streams.stream(Iterators.limit(iterator, batchSize))
                            .map(CSVRecord::toMap)
                            .map((map) -> (Map<String, String>) QueryFactory.mapEntity(map))
                            .collect(Collectors.toList());
                    trafficData.addAll(page);


                    double percent = (double) reader.getPosition() / totalLength;
                    client.dataLoadedFromDisk("traffic", percent, trafficData.size());
//                    if (trafficData.size() % 1_000_000 == 0) {
                    System.out.println(new Date());
                        System.out.println("" + (percent * 100) + "% loaded, " + trafficData.size() + " items");
//                    }
                }

            }
        } catch (IOException e) {
            e.printStackTrace();
        }


        //inform client of schemas
        client.schemaLoaded("traffic", getSchema("traffic"));

        // start a thread telling the client that data is ready, streaming an hour per second, if it matches
        // the active query

    }

    @Override
    public void runQuery(Query<?> query, QueryOptions options) {
        activeQuery = query;
        //race here, new data could start to show up before the existing data, and could be included twice
        ResultSet<Map<String, String>> results = trafficData.retrieve((Query<Map<String, String>>) query, options);
        if (!query.equals(activeQuery)) {
            return;
        }
        int size = results.size();
        getClient().queryFinished(query, size);
        if (size == 0) {
            return;
        }
        Iterator<Map<String, String>> iterator = results.iterator();
        int offset = 0;
        while (query.equals(activeQuery) && iterator.hasNext()) {
            getClient().queryResults(query, Lists.newArrayList(Iterators.limit(iterator, 1000)), offset);
            offset += 1000;
        }
    }

    @Override
    public void parseQuery(String schema, String query, Callback<ParseResult<?>, IllegalStateException> callback) {
        try {
            callback.onSuccess(CQNParser.forPojoWithAttributes((Class<Map<String, String>>) (Class) Map.class, getSchema(schema)).parse(query));
        } catch (IllegalStateException ex) {
            callback.onFailure(ex);
        } catch (Exception ex) {
            callback.onFailure(new IllegalStateException(ex));
        }
    }

    @Override
    public void loadUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute) {
        assert schema.equals("traffic");

        Optional<KeyStatisticsAttributeIndex<Object, Map<String, String>>> hasIndex = Streams.stream(trafficData.getIndexes())
                .filter(index -> index instanceof KeyStatisticsAttributeIndex)
                .map(index -> (KeyStatisticsAttributeIndex<Object, Map<String, String>>) index)
                .filter(index -> index.getAttribute().equals(attribute))
                .findFirst();
        if (hasIndex.isPresent()) {
            int count = hasIndex.get().getCountOfDistinctKeys(QueryFactory.noQueryOptions());
            getClient().uniqueKeysLoaded(attribute, count);

            CloseableIterator<Object> iterator = hasIndex.get().getDistinctKeys(QueryFactory.noQueryOptions()).iterator();
            int offset = 0;
            while (iterator.hasNext()) {
                getClient().uniqueKeysResults((Attribute) attribute, Lists.newArrayList(Iterators.limit(iterator, 200)), offset);
                offset += 200;
            }
        } else {
            getClient().uniqueKeysLoaded(attribute, 0);
        }
    }

    private Map<String, AbstractMapAttribute<?>> getSchema(String schema) {
        assert schema.equals("traffic");
        Map<String, AbstractMapAttribute<?>> traffic = new HashMap<>();
        traffic.put("Date", new MapStringAttribute("Date"));
        traffic.put("STA", new MapStringAttribute("STA"));
        traffic.put("Hour", new MapIntegerAttribute("Hour"));
        traffic.put("Value", new MapIntegerAttribute("Value"));
        traffic.put("Direction", new MapStringAttribute("Direction"));
        return traffic;
    }
}
