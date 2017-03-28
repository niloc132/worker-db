package superstore.common.shared.attribute;

import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Map;

/**
 * Created by colin on 3/25/17.
 */
public class MapStringAttribute extends AbstractMapAttribute<String> {

    public MapStringAttribute() {
        this(null);
    }

    public MapStringAttribute(String key) {
        super(String.class, key);
    }

    @Override
    public String getValue(Map<String, String> object, QueryOptions queryOptions) {
        return object.get(getKey());
    }
}
