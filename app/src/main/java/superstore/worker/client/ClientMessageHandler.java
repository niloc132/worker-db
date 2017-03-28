package superstore.worker.client;

import com.colinalworth.gwt.websockets.client.AbstractClientImpl;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.Query;
import jsinterop.annotations.JsPackage;
import jsinterop.annotations.JsType;
import superstore.common.shared.StoreClient;
import superstore.common.shared.StoreServer;
import superstore.common.shared.attribute.AbstractMapAttribute;

import java.util.List;
import java.util.Map;

/**
 * Created by colin on 3/25/17.
 */
public class ClientMessageHandler extends AbstractClientImpl<StoreServer> implements StoreClient {
    @Override
    public void dataLoadedFromDisk(String schema, double percent, int rows) {
        Console.log("Data loading for " + schema + " from disk, " + (percent * 100) + ", " + rows + " loaded");
    }

    @Override
    public void buildingIndexes(String schema) {
        Console.log("Building indexes...");

    }

    @Override
    public void schemaLoaded(String name, Map<String, AbstractMapAttribute<?>> columns) {
        Console.log("Schema loaded, " + columns.size() + " columns");
    }

    @Override
    public void queryFinished(Query<?> query, int totalCount) {
        Console.log("Query had " + totalCount + " results");
    }

    @Override
    public void queryResults(Query<?> query, List<Map<String, String>> results, int offset) {
        Console.log("Results loading " + offset);
    }

    @Override
    public void uniqueKeysLoaded(Attribute<Map<String, String>, ?> attribute, int totalCount) {
        Console.log("unique keys for attribute " + attribute.getAttributeName() + " count: " + totalCount);
    }

    @Override
    public <T> void uniqueKeysResults(Attribute<Map<String, String>, T> attribute, List<T> results, int offset) {
        Console.log(attribute.getAttributeName() + " : " + results);
    }

    @JsType(isNative = true, name = "console", namespace = JsPackage.GLOBAL)
    public static class Console {
        public native static void log(String message);
    }
}
