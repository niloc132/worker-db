package superstore.common.shared;

import com.colinalworth.gwt.websockets.shared.Server;
import com.google.gwt.core.client.Callback;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;

/**
 * Created by colin on 3/24/17.
 */
public interface StoreServer extends Server<StoreClient> {

    void runQuery(Query<?> query, QueryOptions options);

    void parseQuery(String schema, String query, Callback<ParseResult<?>, IllegalStateException> callback);

}
