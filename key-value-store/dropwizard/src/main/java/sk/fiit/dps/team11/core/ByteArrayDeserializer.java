package sk.fiit.dps.team11.core;

import java.io.IOException;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;

public class ByteArrayDeserializer extends JsonDeserializer<byte[]> {

	@Override
	public byte[] deserialize(JsonParser p, DeserializationContext ctxt)
			throws IOException, JsonProcessingException {
		
		return Base64.decodeBase64(p.getValueAsString());
	}
	
}