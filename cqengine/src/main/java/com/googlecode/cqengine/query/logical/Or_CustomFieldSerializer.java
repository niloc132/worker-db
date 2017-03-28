package com.googlecode.cqengine.query.logical;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import java.util.Collection;

public class Or_CustomFieldSerializer extends CustomFieldSerializer<Or> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, Or instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, Or instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, Or instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, Or instance) throws SerializationException {
        streamWriter.writeObject(instance.getChildQueries());
    }

    @Override
    public Or instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static Or instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new Or(
                (Collection) streamReader.readObject()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}