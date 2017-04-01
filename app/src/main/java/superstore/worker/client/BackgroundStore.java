package superstore.worker.client;

import com.colinalworth.gwt.websockets.client.ServerBuilder;
import com.colinalworth.gwt.worker.client.WorkerFactory;
import com.colinalworth.gwt.worker.client.worker.MessagePort;
import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.common.collect.Streams;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.support.CloseableIterator;
import com.googlecode.cqengine.index.support.KeyStatisticsAttributeIndex;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import com.googlecode.cqengine.resultset.ResultSet;
import superstore.common.client.StoreApp;
import superstore.common.client.StoreWorker;
import superstore.common.shared.StoreServer;
import superstore.common.shared.attribute.AbstractMapAttribute;
import superstore.common.shared.attribute.MapStringAttribute;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Created by colin on 3/28/17.
 */
public class BackgroundStore implements EntryPoint {
    interface Server extends ServerBuilder<StoreServer> {}

    interface App extends WorkerFactory<StoreApp, StoreWorker> {}

    private IndexedCollection<Map<String, String>> trafficData = new ConcurrentIndexedCollection<>();
    private Map<String, AbstractMapAttribute<?>> columns;
    private volatile Query<?> activeQuery;


    private StoreApp app;


    @Override
    public void onModuleLoad() {

        //TODO copy/pasted from Server, make an abstract superclass for both?
        trafficData.addIndex(NavigableIndex.onAttribute(new MapStringAttribute("Date")));
        //TODO partial index on each station for its date and hour?
        trafficData.addIndex(HashIndex.onAttribute(new MapStringAttribute("STA")));
        trafficData.addIndex(HashIndex.onAttribute(new MapStringAttribute("Direction")));


        Server server = GWT.create(Server.class);
        server.setPath("/socket");

        StoreServer socket = server.start();

        StoreWorker workerImpl = new StoreWorker() {
            @Override
            public void runRemoteQuery(Query<?> query, QueryOptions options) {
                socket.runQuery(query, options);
            }

            @Override
            public void runLocalQuery(Query<?> query, QueryOptions options) {
                activeQuery = query;
                //race here, new data could start to show up before the existing data, and could be included twice
                ResultSet<Map<String, String>> results = trafficData.retrieve((Query<Map<String, String>>) query, options);
                if (!query.equals(activeQuery)) {
                    return;
                }
                int size = results.size();
                getRemote().queryFinished(query, size);
                if (size == 0) {
                    return;
                }
                Iterator<Map<String, String>> iterator = results.iterator();
                int offset = 0;
                while (query.equals(activeQuery) && iterator.hasNext()) {
                    getRemote().queryResults(query, Lists.newArrayList(Iterators.limit(iterator, 1000)), offset);
                    offset += 1000;
                }
            }

            @Override
            public void parseQuery(String schema, String query, Callback<ParseResult<?>, IllegalStateException> callback) {
                //straight pass-through to the server and back again
                socket.parseQuery(schema, query, callback);
            }

            @Override
            public void loadLocalUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute) {
                assert schema.equals("traffic");

                Optional<KeyStatisticsAttributeIndex<Object, Map<String, String>>> hasIndex = Streams.stream(trafficData.getIndexes())
                        .filter(index -> index instanceof KeyStatisticsAttributeIndex)
                        .map(index -> (KeyStatisticsAttributeIndex<Object, Map<String, String>>) index)
                        .filter(index -> index.getAttribute().equals(attribute))
                        .findFirst();
                if (hasIndex.isPresent()) {
                    int count = hasIndex.get().getCountOfDistinctKeys(QueryFactory.noQueryOptions());
                    getRemote().uniqueKeysLoaded(attribute, count);

                    CloseableIterator<Object> iterator = hasIndex.get().getDistinctKeys(QueryFactory.noQueryOptions()).iterator();
                    int offset = 0;
                    while (iterator.hasNext()) {
                        getRemote().uniqueKeysResults((Attribute) attribute, Lists.newArrayList(Iterators.limit(iterator, 200)), offset);
                        offset += 200;
                    }
                } else {
                    getRemote().uniqueKeysLoaded(attribute, 0);
                }
            }

            @Override
            public void setRemote(StoreApp storeApp) {
                app = storeApp;
            }

            @Override
            public StoreApp getRemote() {
                return app;
            }
        };

        socket.setClient(new ClientMessageHandler() {
            int incomingResults;
            @Override
            public void schemaLoaded(String name, Map<String, AbstractMapAttribute<?>> columns) {
                super.schemaLoaded(name, columns);

                BackgroundStore.this.columns = columns;
                app.schemaLoaded(name, columns);
            }

            @Override
            public void queryFinished(Query<?> query, int totalCount) {
                super.queryFinished(query, totalCount);

                incomingResults = totalCount;
                trafficData.clear();
            }

            @Override
            public void queryResults(Query<?> query, List<Map<String, String>> results, int offset) {
                super.queryResults(query, results, offset);

                trafficData.addAll(results);
                if (incomingResults == trafficData.size()) {
//                    //all data is in the client, tell the app about current unique values or something
                    for (AbstractMapAttribute<?> attribute : columns.values()) {
                        workerImpl.loadLocalUniqueKeysForColumn("traffic", attribute);
                    }
                }
            }
        });

        App appWrapper = GWT.create(App.class);

        appWrapper.wrapRemoteMessagePort(self(), workerImpl);

    }

    private native MessagePort self() /*-{
      return $wnd;
    }-*/;
}
