package com.googlecode.cqengine.query.option;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import java.util.Map;

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
        streamWriter.writeObject(instance.getOptions());
    }

    @Override
    public QueryOptions instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static QueryOptions instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new QueryOptions(
                (Map) streamReader.readObject()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}
