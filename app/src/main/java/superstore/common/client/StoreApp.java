package superstore.common.client;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import org.gwtproject.rpc.api.Endpoint;
import org.gwtproject.rpc.worker.client.MessagePortEndpoint;
import superstore.common.shared.DataStore;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.List;
import java.util.Map;

/**
 * Created by colin on 3/28/17.
 */
@Endpoint
public interface StoreApp extends MessagePortEndpoint<StoreWorker>, DataStore.QueryListener {

}
