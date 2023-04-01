package superstore.common.shared;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import org.gwtproject.rpc.api.Client;
import org.gwtproject.rpc.api.Endpoint;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.List;
import java.util.Map;

/**
 * Created by colin on 3/24/17.
 */
@Endpoint
public interface StoreClient extends Client<StoreClient, StoreServer> {
    void dataLoadedFromDisk(String schema, double percent, int rows);
    void schemaLoaded(String name, Map<String, AbstractMapAttribute<?>> columns);


    void queryFinished(Query<Map<String, String>> query, int totalCount);
    void queryResults(Query<Map<String, String>> query, List<Map<String, String>> results, int offset);
    void additionalQueryResults(Query<Map<String, String>> query, List<Map<String, String>> items);

    void uniqueKeysLoaded(Attribute<Map<String, String>, ?> attribute, int totalCount);
    void uniqueKeysResults(Attribute<Map<String, String>, ?> attribute, List<?> results, int offset);

}
