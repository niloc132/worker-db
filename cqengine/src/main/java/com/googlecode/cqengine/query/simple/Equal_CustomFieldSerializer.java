package com.googlecode.cqengine.query.simple;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.googlecode.cqengine.attribute.Attribute;

public class Equal_CustomFieldSerializer extends CustomFieldSerializer<Equal> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, Equal instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, Equal instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, Equal instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, Equal instance) throws SerializationException {
        streamWriter.writeObject(instance.getAttribute());
        streamWriter.writeObject(instance.getValue());
    }

    @Override
    public Equal instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static Equal instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new Equal(
                (Attribute) streamReader.readObject(),
                streamReader.readObject()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}