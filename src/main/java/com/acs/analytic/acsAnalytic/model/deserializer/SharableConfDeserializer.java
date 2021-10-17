package com.acs.analytic.acsAnalytic.model.deserializer;

import java.io.IOException;

import com.acs.analytic.acsAnalytic.model.dto.SharableConfDto;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;

public class SharableConfDeserializer extends StdDeserializer<SharableConfDto> {

    protected SharableConfDeserializer(Class<?> vc) {
        super(vc);
    }

    private final ObjectMapper om = new ObjectMapper()
            .disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

    @Override
    public SharableConfDto deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
        JsonNode jsonNode = jsonParser.getCodec()
                .readTree(jsonParser);
        return om.readValue(om.writeValueAsString(jsonNode), new TypeReference<>() {});
    }
}
