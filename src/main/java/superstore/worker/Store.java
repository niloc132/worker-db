package superstore.worker;

import com.google.common.collect.Lists;
import com.google.gwt.core.client.Callback;
import com.google.gwt.core.client.EntryPoint;
import com.googlecode.cqengine.ConcurrentIndexedCollection;
import com.googlecode.cqengine.IndexedCollection;
import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.SimpleNullableMapAttribute;
import com.googlecode.cqengine.index.hash.HashIndex;
import com.googlecode.cqengine.index.navigable.NavigableIndex;
import com.googlecode.cqengine.index.unique.UniqueIndex;
import com.googlecode.cqengine.persistence.onheap.OnHeapPersistence;
import com.googlecode.cqengine.query.Query;
import com.googlecode.cqengine.query.QueryFactory;
import com.googlecode.cqengine.query.option.QueryOptions;
import com.googlecode.cqengine.resultset.ResultSet;

import java.io.Serializable;
import java.util.*;
import java.util.function.BinaryOperator;
import java.util.function.UnaryOperator;

/**
 * Experiments with indexing data in the browser.
 */
public class Store implements EntryPoint {
    private final IndexedCollection<Map<String, ?>> collection;

    public Store() {
        collection = new ConcurrentIndexedCollection<>();
        collection.addIndex(UniqueIndex.onAttribute(makeAttribute("id", String.class)));
        collection.addIndex(NavigableIndex.onAttribute(makeAttribute("score", Double.class)));
        collection.addIndex(HashIndex.onAttribute(makeAttribute("street", String.class)));
    }

    private static <T> Attribute<Map<String, ?>, T> makeAttribute(String key, Class<T> type) {
        //noinspection unchecked
        return (Attribute) new SimpleNullableMapAttribute<>(key, type);
    }



    public void addItem(Map<String, ?> item) {
        //noinspection unchecked
        collection.add(QueryFactory.mapEntity(item));
    }

    public void query(Query<Map<String, ?>> query, Callback<ArrayList<Map<String, ?>>, Throwable> callback) {
        try {
            //TODO consider changing API to stream results instead
            callback.onSuccess(Lists.newArrayList(collection.retrieve(query)));
        } catch (Throwable t) {
            callback.onFailure(t);
        }

    }

    @Override
    public void onModuleLoad() {
        addItem(new HashMap<>());
    }


}
