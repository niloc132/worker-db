package superstore.common.shared.attribute;

import com.googlecode.cqengine.attribute.SimpleAttribute;
import com.googlecode.cqengine.query.option.QueryOptions;

import java.util.Map;

/**
 * Created by colin on 3/25/17.
 */
public class MapDoubleAttribute extends AbstractMapAttribute<Double> {

    public MapDoubleAttribute(String key) {
        super(Double.class, key);
    }

    public MapDoubleAttribute() {
        this(null);
    }

    @Override
    public Double getValue(Map<String, String> object, QueryOptions queryOptions) {
        return Double.parseDouble(object.get(getKey()));
    }
}
