package superstore.common.shared.attribute;

import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.attribute.SimpleNullableAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Map;

/**
 * Created by colin on 3/25/17.
 */
public class MapIntegerAttribute extends AbstractMapAttribute<Integer> {


    public MapIntegerAttribute(String key) {
        super(Integer.class, key);
    }

    public MapIntegerAttribute() {
        this(null);
    }

    @Override
    public Integer getValue(Map<String, String> object, QueryOptions queryOptions) {
        return Integer.parseInt(object.get(getKey()));
    }
}
