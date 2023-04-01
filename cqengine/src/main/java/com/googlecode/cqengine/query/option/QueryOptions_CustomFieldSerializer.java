package com.googlecode.cqengine.query.option;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import java.util.Map;
import java.util.Map.Entry;

public class QueryOptions_CustomFieldSerializer extends CustomFieldSerializer<QueryOptions> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, QueryOptions instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, QueryOptions instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, QueryOptions instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, QueryOptions instance) throws SerializationException {
        streamWriter.writeInt(instance.getOptions().size());
        for (Entry<Object, Object> entry : instance.getOptions().entrySet()) {
            if (entry.getValue() == null) {
                continue;
            }
            //manually check each to see if its class is its key
            if (entry.getKey() instanceof Class) {
                Class clazz = (Class) entry.getKey();
                if (entry.getValue().getClass().equals(clazz)) {
                    streamWriter.writeBoolean(true);
                    streamWriter.writeObject(entry.getValue());
                } else {
                    //skip, TODO log or fail?
                }
            } else {
                streamWriter.writeBoolean(false);
                streamWriter.writeObject(entry.getKey());
                streamWriter.writeObject(entry.getValue());
            }
        }
    }

    @Override
    public QueryOptions instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static QueryOptions instantiate(SerializationStreamReader streamReader) throws SerializationException {
        QueryOptions options = new QueryOptions();
        int count = streamReader.readInt();
        for (int i = 0; i < count; i++) {
            boolean isClassKey = streamReader.readBoolean();
            if (isClassKey) {
                Object obj = streamReader.readObject();
                options.put(obj.getClass(), obj);
            } else {
                Object key = streamReader.readObject();
                Object value = streamReader.readObject();
                options.put(key, value);
            }
        }

        return options;
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}
