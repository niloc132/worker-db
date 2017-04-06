package superstore.common.shared;

import com.colinalworth.gwt.websockets.shared.Client;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.List;
import java.util.Map;

/**
 * Created by colin on 3/24/17.
 */
public interface StoreClient extends Client<StoreServer> {
    void dataLoadedFromDisk(String schema, double percent, int rows);
    void schemaLoaded(String name, Map<String, AbstractMapAttribute<?>> columns);


    void queryFinished(Query<?> query, int totalCount);
    void queryResults(Query<?> query, List<Map<String, String>> results, int offset);
    void additionalQueryResults(Query<?> query, List<Map<String, String>> items);

    void uniqueKeysLoaded(Attribute<Map<String, String>, ?> attribute, int totalCount);
    <T> void uniqueKeysResults(Attribute<Map<String, String>, T> attribute, List<T> results, int offset);

}
