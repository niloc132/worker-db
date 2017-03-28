package com.googlecode.cqengine.query.simple;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.googlecode.cqengine.attribute.Attribute;

public class Between_CustomFieldSerializer extends CustomFieldSerializer<Between> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, Between instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, Between instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, Between instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, Between instance) throws SerializationException {
        streamWriter.writeObject(instance.getAttribute());
        streamWriter.writeObject(instance.getLowerValue());
        streamWriter.writeBoolean(instance.isLowerInclusive());
        streamWriter.writeObject(instance.getUpperValue());
        streamWriter.writeBoolean(instance.isUpperInclusive());
    }

    @Override
    public Between instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static Between instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new Between(
                (Attribute) streamReader.readObject(),
                (Comparable) streamReader.readObject(),
                streamReader.readBoolean(),
                (Comparable) streamReader.readObject(),
                streamReader.readBoolean()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}