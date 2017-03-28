package com.googlecode.cqengine.query.logical;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.googlecode.cqengine.query.Query;

import java.util.Collection;

public class Not_CustomFieldSerializer extends CustomFieldSerializer<Not> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, Not instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, Not instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, Not instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, Not instance) throws SerializationException {
        streamWriter.writeObject(instance.getNegatedQuery());
    }

    @Override
    public Not instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static Not instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new Not(
                (Query) streamReader.readObject()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}