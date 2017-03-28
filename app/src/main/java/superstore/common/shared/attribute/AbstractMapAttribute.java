package superstore.common.shared.attribute;

import com.googlecode.cqengine.attribute.Attribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Collections;
import java.util.Map;

/**
 * This really should be a SimpleAttribute to get CQ to short-circuit a few places, but
 * serialization is much cleaner without having to worry about Class instances.
 */
public abstract class AbstractMapAttribute<A> implements Attribute<Map<String, String>, A> {
    private transient final Class<A> attributeType;
    private final String key;

    public AbstractMapAttribute(Class<A> attributeType, String key) {
        this.attributeType = attributeType;
        this.key = key;
    }

    protected AbstractMapAttribute() {
        this.attributeType = null;
        this.key = null;
    }

    public String getKey() {
        return key;
    }

    @Override
    public Class<Map<String, String>> getObjectType() {
        return (Class) Map.class;
    }

    @Override
    public String getAttributeName() {
        return key;
    }

    @Override
    public Class<A> getAttributeType() {
        return attributeType;
    }

    @Override
    public Iterable<A> getValues(Map<String, String> object, QueryOptions queryOptions) {
        return Collections.singleton(getValue(object, queryOptions));
    }

    public abstract A getValue(Map<String, String> object, QueryOptions queryOptions);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        AbstractMapAttribute<?> that = (AbstractMapAttribute<?>) o;

        if (attributeType != null ? !attributeType.equals(that.attributeType) : that.attributeType != null)
            return false;
        return !(key != null ? !key.equals(that.key) : that.key != null);
    }

    @Override
    public int hashCode() {
        int result = attributeType != null ? attributeType.hashCode() : 0;
        result = 31 * result + (key != null ? key.hashCode() : 0);
        return result;
    }
}
