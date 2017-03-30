package com.googlecode.cqengine.query.parser.common;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

public class InvalidQueryException_CustomFieldSerializer extends CustomFieldSerializer<InvalidQueryException> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, InvalidQueryException instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, InvalidQueryException instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, InvalidQueryException instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, InvalidQueryException instance) throws SerializationException {
        streamWriter.writeString(instance.getMessage());
    }

    @Override
    public InvalidQueryException instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static InvalidQueryException instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new InvalidQueryException(streamReader.readString());
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}
