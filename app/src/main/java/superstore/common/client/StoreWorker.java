package superstore.common.client;

import com.colinalworth.gwt.worker.client.Endpoint;
import com.google.gwt.core.client.Callback;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.query.parser.common.ParseResult;
import superstore.common.shared.attribute.AbstractMapAttribute;

/**
 * Created by colin on 3/28/17.
 */
public interface StoreWorker extends Endpoint<StoreApp> {

    void runRemoteQuery(Query<?> query, QueryOptions options);

    void runLocalQuery(Query<?> query, QueryOptions options);

    void parseQuery(String schema, String query, Callback<ParseResult<?>, IllegalStateException> callback);

    void loadLocalUniqueKeysForColumn(String schema, AbstractMapAttribute<?> attribute);

}
