package com.googlecode.cqengine.entity;

import com.google.gwt.user.client.rpc.CustomFieldSerializer;
import com.google.gwt.user.client.rpc.SerializationException;
import com.google.gwt.user.client.rpc.SerializationStreamReader;
import com.google.gwt.user.client.rpc.SerializationStreamWriter;

import java.util.Map;

public class MapEntity_CustomFieldSerializer extends CustomFieldSerializer<MapEntity> {
    @Override
    public void deserializeInstance(SerializationStreamReader streamReader, MapEntity instance) throws SerializationException {
        deserialize(streamReader, instance);
    }

    public static void deserialize(SerializationStreamReader streamReader, MapEntity instance) throws SerializationException {
        // Handled in instantiate.
    }

    @Override
    public void serializeInstance(SerializationStreamWriter streamWriter, MapEntity instance) throws SerializationException {
        serialize(streamWriter, instance);
    }

    public static void serialize(SerializationStreamWriter streamWriter, MapEntity instance) throws SerializationException {
        streamWriter.writeObject(instance.getWrappedMap());
    }

    @Override
    public MapEntity instantiateInstance(SerializationStreamReader streamReader) throws SerializationException {
        return instantiate(streamReader);
    }

    public static MapEntity instantiate(SerializationStreamReader streamReader) throws SerializationException {
        return new MapEntity(
                (Map) streamReader.readObject()
        );
    }

    @Override
    public boolean hasCustomInstantiateInstance() {
        return true;
    }

}