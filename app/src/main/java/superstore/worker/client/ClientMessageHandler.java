package superstore.worker.client;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import org.gwtproject.rpc.gwt.client.AbstractClientImpl;
import superstore.common.shared.StoreClient;
import superstore.common.shared.StoreServer;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.List;
import java.util.Map;

import static elemental2.dom.DomGlobal.console;

/**
 * Created by colin on 3/25/17.
 */
public class ClientMessageHandler extends AbstractClientImpl<StoreClient, StoreServer> implements StoreClient {
    @Override
    public void dataLoadedFromDisk(String schema, int rows) {
        console.log("Data into server loading for " + schema + " from disk, " + rows + " loaded");
    }

    @Override
    public void schemaLoaded(String name, Map<String, AbstractMapAttribute<?>> columns) {
        console.log("Server schema loaded, " + columns.size() + " columns");
    }

    @Override
    public void queryFinished(Query<Map<String, String>> query, int totalCount) {
        console.log("Server query had " + totalCount + " results");
    }

    @Override
    public void queryResults(Query<Map<String, String>> query, List<Map<String, String>> results, int offset) {
        console.log("Server results loading into worker " + offset);
    }

    @Override
    public void additionalQueryResults(Query<Map<String, String>> query, List<Map<String, String>> items) {
        console.log("More results that match: " + items.size());
    }

    @Override
    public void uniqueKeysLoaded(Attribute<Map<String, String>, ?> attribute, int totalCount) {
        console.log("Unique keys from server for attribute " + attribute.getAttributeName() + " count: " + totalCount);
    }

    @Override
    public void uniqueKeysResults(Attribute<Map<String, String>, ?> attribute, List<?> results, int offset) {
        console.log(attribute.getAttributeName() + " : " + results);
    }
}
