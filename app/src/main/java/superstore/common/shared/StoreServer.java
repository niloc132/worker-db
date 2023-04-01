package superstore.common.shared;

import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import org.gwtproject.rpc.api.Callback;
import org.gwtproject.rpc.api.Endpoint;
import org.gwtproject.rpc.api.Server;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.Map;

/**
 * Created by colin on 3/24/17.
 */
@Endpoint
public interface StoreServer extends Server<StoreServer, StoreClient> {

    void runQuery(Query<Map<String, String>> query, QueryOptions options);

    void parseQuery(String schema, String query, Callback<ParseResult<Map<String, String>>, IllegalStateException> callback);

    void loadUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute);

}
