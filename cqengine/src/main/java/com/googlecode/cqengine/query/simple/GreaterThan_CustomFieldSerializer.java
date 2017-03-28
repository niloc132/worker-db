package com.googlecode.cqengine.query.simple;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;
import com.googlecode.cqengine.attribute.Attribute;

public class GreaterThan_CustomFieldSerializer extends CustomFieldSerializer<GreaterThan> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, GreaterThan instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, GreaterThan instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, GreaterThan instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, GreaterThan instance) throws SerializationException {
        streamWriter.writeObject(instance.getAttribute());
        streamWriter.writeObject(instance.getValue());
        streamWriter.writeBoolean(instance.isValueInclusive());
    }

    @Override
    public GreaterThan instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static GreaterThan instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new GreaterThan(
                (Attribute) streamReader.readObject(),
                (Comparable) streamReader.readObject(),
                streamReader.readBoolean()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}