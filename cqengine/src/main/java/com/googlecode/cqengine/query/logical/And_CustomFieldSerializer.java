package com.googlecode.cqengine.query.logical;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import java.util.Collection;

public class And_CustomFieldSerializer extends CustomFieldSerializer<And> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, And instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, And instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, And instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, And instance) throws SerializationException {
        streamWriter.writeObject(instance.getChildQueries());
    }

    @Override
    public And instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static And instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new And(
                (Collection) streamReader.readObject()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}