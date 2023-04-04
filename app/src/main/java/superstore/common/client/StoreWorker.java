package superstore.common.client;

import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import org.gwtproject.rpc.api.Callback;
import org.gwtproject.rpc.api.Endpoint;
import org.gwtproject.rpc.worker.client.MessagePortEndpoint;
import superstore.common.shared.DataStore;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.Map;

/**
 * Created by colin on 3/28/17.
 */
@Endpoint
public interface StoreWorker extends MessagePortEndpoint<StoreApp>, DataStore.QueryConnection {

    /**
     * Runs the query on the remote server, loading into the worker, so it can be queried
     * via {@link #runQuery(Query, QueryOptions)}.
     */
    void runRemoteQuery(Query<Map<String, String>> query, QueryOptions options);

    /**
     * Delegates to the server for parsing via reflection, we don't presently have a nice way to build
     * queries on the fly on the client.
     */
    void parseQuery(String schema, String query, Callback<ParseResult<Map<String, String>>, IllegalStateException> callback);
}
