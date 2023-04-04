package superstore.common.shared;

import org.gwtproject.rpc.api.Client;
import org.gwtproject.rpc.api.Endpoint;

/**
 * Created by colin on 3/24/17.
 */
@Endpoint
public interface StoreClient extends Client<StoreClient, StoreServer>, DataStore.QueryListener {


}
