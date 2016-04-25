package sk.fiit.dps.team11.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.deser.std.PrimitiveArrayDeserializers;

public class ByteArrayDeserializer extends JsonDeserializer<byte[]> {
	
	@SuppressWarnings("unchecked")
	private final static JsonDeserializer<byte[]> des = (JsonDeserializer<byte[]>)
			PrimitiveArrayDeserializers.forType(Byte.class);

	@Override
	public byte[] deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		return des.deserialize(p, ctxt);
	}
	
}