package com.googlecode.cqengine.query.simple;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.googlecode.cqengine.attribute.Attribute;

import java.util.Set;

public class In_CustomFieldSerializer extends CustomFieldSerializer<In> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, In instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, In instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, In instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, In instance) throws SerializationException {
        streamWriter.writeObject(instance.getAttribute());
        streamWriter.writeBoolean(instance.isDisjoint());
        streamWriter.writeObject(instance.getValues());
    }

    @Override
    public In instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static In instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new In(
                (Attribute) streamReader.readObject(),
                streamReader.readBoolean(),
                (Set) streamReader.readObject()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}