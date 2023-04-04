package superstore.server;

import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import com.googlecode.cqengine.query.parser.cqn.CQNParser;
import org.gwtproject.rpc.api.Callback;
import org.gwtproject.rpc.servlet.websocket.AbstractServerImpl;
import superstore.common.shared.DataStore;
import superstore.common.shared.StoreClient;
import superstore.common.shared.StoreClient_Impl;
import superstore.common.shared.StoreServer;
import superstore.common.shared.attribute.AbstractMapAttribute;

import javax.websocket.server.ServerEndpoint;
import java.util.Map;

@ServerEndpoint("/socket")
public class Server extends AbstractServerImpl<StoreServer, StoreClient> implements StoreServer {
    private DataStore.QueryConnection clientConnection;

    public Server() {
        super(StoreClient_Impl::new);
    }

    @Override
    public void onOpen(Connection connection, StoreClient client) {
        clientConnection = StartupListener.store.connect(client);
    }

    @Override
    public void onClose(Connection connection, StoreClient client) {
        clientConnection.close();
    }

    @Override
    public synchronized void runQuery(Query<Map<String, String>> query, QueryOptions options) {
        clientConnection.runQuery(query, options);

        //TODO schedule updates
        // start a thread telling the client that data is ready, streaming an hour per second, if it matches
        // the active query
        //TODO move this and cancel this on close
//        streamingResults = execService.scheduleAtFixedRate(() -> {
//            //scheduleAtFixedRate specifies:
//            //  If any execution of this task
//            //  takes longer than its period, then subsequent executions
//            //  may start late, but will not concurrently execute.
//
//            try {
//                if (activeQuery == null) {
//                    //something is wrong, TODO
//                    return;
//                }
//
//                synchronized (resultsStreamLock) {
//                    //can't use QueryFactory.between without a compound index thingie of date+hour
//                    Query<Map<String, String>> q = QueryFactory.and(activeQuery, QueryFactory.between(timestampAttribute, format(lastResultsSent), true, format(now), false));
//                    List<Map<String, String>> items = Lists.newArrayList(trafficData.retrieve((Query<Map<String, String>>) q, activeQueryOptions).iterator());
//                    if (!items.isEmpty()) {
//                        getClient().additionalQueryResults(activeQuery, items);
//                    }
//                    lastResultsSent = now;
//                }
//
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }, 1, 1, TimeUnit.SECONDS);
    }

    @Override
    public void parseQuery(String schema, String query, Callback<ParseResult<Map<String, String>>, IllegalStateException> callback) {
        try {
            callback.onSuccess(CQNParser.forPojoWithAttributes((Class<Map<String, String>>) (Class) Map.class, StartupListener.store.getColumns()).parse(query));
        } catch (IllegalStateException ex) {
            ex.printStackTrace();
            callback.onFailure(ex);
        } catch (Exception ex) {
            ex.printStackTrace();
            callback.onFailure(new IllegalStateException(ex));
        }
    }

    @Override
    public synchronized void loadUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute) {
        clientConnection.loadUniqueKeysForColumn(schema, attribute);
    }

    @Override
    public void onError(Throwable thr) {
        thr.printStackTrace();
        super.onError(thr);
    }
}
