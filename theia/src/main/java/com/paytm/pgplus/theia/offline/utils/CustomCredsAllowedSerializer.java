package com.paytm.pgplus.theia.offline.utils;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import com.paytm.pgplus.facade.user.models.CredsAllowed;

import java.io.IOException;

public class CustomCredsAllowedSerializer extends StdSerializer<CredsAllowed> {
    public CustomCredsAllowedSerializer() {
        this(null);
    }

    public CustomCredsAllowedSerializer(Class<CredsAllowed> vc) {
        super(vc);
    }

    @Override
    public void serialize(CredsAllowed value, JsonGenerator jgen, SerializerProvider provider) throws IOException,
            JsonProcessingException {
        jgen.writeStartObject();
        jgen.writeStringField("CredsAllowedDLength", value.getCredsAllowedDLength());
        jgen.writeStringField("CredsAllowedDType", value.getCredsAllowedDType());
        jgen.writeStringField("CredsAllowedSubType", value.getCredsAllowedSubType());
        jgen.writeStringField("CredsAllowedType", value.getCredsAllowedType());
        jgen.writeStringField("dLength", value.getdLength());
        jgen.writeEndObject();
    }

}