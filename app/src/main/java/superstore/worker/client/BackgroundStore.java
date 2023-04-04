package superstore.worker.client;

import com.google.common.collect.Iterators;
import com.google.common.collect.Lists;
import com.google.gwt.core.client.EntryPoint;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import com.googlecode.cqengine.resultset.ResultSet;
import elemental2.dom.DomGlobal;
import org.gwtproject.rpc.api.Callback;
import org.gwtproject.rpc.gwt.client.ServerBuilder;
import org.gwtproject.rpc.worker.client.WorkerFactory;
import superstore.common.client.StoreApp;
import superstore.common.client.StoreApp_Impl;
import superstore.common.client.StoreWorker;
import superstore.common.shared.DataStore;
import superstore.common.shared.StoreServer;
import superstore.common.shared.StoreServer_Impl;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Created by colin on 3/28/17.
 */
public class BackgroundStore implements EntryPoint {

    private final DataStore store = new DataStore();

    private StoreApp app;


    @Override
    public void onModuleLoad() {

        ServerBuilder<StoreServer> server = ServerBuilder.of(StoreServer_Impl::new);
        server.setPath("/socket");

        StoreServer socket = server.start();

        StoreWorker workerImpl = new StoreWorker() {
            private DataStore.QueryConnection pageConnection;
            @Override
            public void onError(Throwable throwable) {
                //TODO
            }

            @Override
            public void runRemoteQuery(Query<Map<String, String>> query, QueryOptions options) {
                socket.runQuery(query, options);
            }

            @Override
            public void runQuery(Query<Map<String, String>> query, QueryOptions options) {
                pageConnection.runQuery(query, options);

//
//
//                activeQuery = query;
//                activeQueryOptions = options;
//                //race here, new data could start to show up before the existing data, and could be included twice
//                ResultSet<Map<String, String>> results = trafficData.retrieve(query, options);
//                if (!query.equals(activeQuery)) {
//                    return;
//                }
//                int size = results.size();
//                getRemote().queryFinished(query, size);
//                if (size == 0) {
//                    return;
//                }
//                Iterator<Map<String, String>> iterator = results.iterator();
//                int offset = 0;
//                while (query.equals(activeQuery) && iterator.hasNext()) {
//                    getRemote().queryResults(query, Lists.newArrayList(Iterators.limit(iterator, 1000)), offset);
//                    offset += 1000;
//                }
            }

            @Override
            public void parseQuery(String schema, String query, Callback<ParseResult<Map<String, String>>, IllegalStateException> callback) {
                //straight pass-through to the server and back again
                socket.parseQuery(schema, query, callback);
            }

            @Override
            public void loadUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute) {
                DomGlobal.console.log("asked to load unique keys for " + schema);
                assert schema.equals("traffic");
                pageConnection.loadUniqueKeysForColumn(schema, attribute);
            }

            @Override
            public void close() {
                pageConnection.close();
            }

            @Override
            public void setRemote(StoreApp storeApp) {
                pageConnection = store.connect(storeApp);

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

                //TODO we should use the server schemas to set up client DataStore instances
//                BackgroundStore.this.columns = columns;
//                app.schemaLoaded(name, columns);
            }

            @Override
            public void queryFinished(Query<Map<String, String>> query, int totalCount) {
                incomingResults = totalCount;
                store.clear();
            }

            @Override
            public void queryResults(Query<Map<String, String>> query, List<Map<String, String>> results, int offset) {
                super.queryResults(query, results, offset);
                store.addAll(results);
                if (incomingResults == store.size()) {
                    //all data is in the client, tell the app about current unique values or something
                    for (AbstractMapAttribute<?> attribute : store.getColumns().values()) {
                        workerImpl.loadUniqueKeysForColumn("traffic", attribute);
                    }
                }
            }

            @Override
            public void additionalQueryResults(Query<Map<String, String>> query, List<Map<String, String>> items) {
                super.additionalQueryResults(query, items);

                store.addAll(items);

                //TODO wire this up via DataStore
//                if (activeQuery != null) {
//                    List<Map<String, String>> filteredItems = items.stream().filter(obj -> activeQuery.matches(obj, activeQueryOptions)).collect(Collectors.toList());
//                    if (!filteredItems.isEmpty()) {
//                        app.additionalQueryResults(activeQuery, filteredItems);
//                    }
//                }
            }
        });

        WorkerFactory<StoreApp, StoreWorker> factory = WorkerFactory.of(StoreApp_Impl::new);

        factory.wrapDedicatedWorkerGlobalScope(workerImpl);

    }
}
