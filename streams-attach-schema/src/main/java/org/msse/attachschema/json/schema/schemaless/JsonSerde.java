package org.msse.attachschema.json.schema.schemaless;

import com.fasterxml.jackson.databind.JsonNode;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serializer;

public class JsonSerde implements Serde<JsonNode> {

    @Override
    public Serializer<JsonNode> serializer() {
        return new JsonSerializer();
    }

    @Override
    public Deserializer<JsonNode> deserializer() {
        return new JsonDeserializer();
    }

}
