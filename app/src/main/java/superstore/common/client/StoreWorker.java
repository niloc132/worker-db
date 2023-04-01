package superstore.common.client;

import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import org.gwtproject.rpc.api.Callback;
import org.gwtproject.rpc.api.Endpoint;
import org.gwtproject.rpc.worker.client.MessagePortEndpoint;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.Map;

/**
 * Created by colin on 3/28/17.
 */
@Endpoint
public interface StoreWorker extends MessagePortEndpoint<StoreApp> {

    void runRemoteQuery(Query<Map<String, String>> query, QueryOptions options);

    void runLocalQuery(Query<Map<String, String>> query, QueryOptions options);

    void parseQuery(String schema, String query, Callback<ParseResult<Map<String, String>>, IllegalStateException> callback);

    void loadLocalUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute);

}
