package sk.fiit.dps.team11.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;

public class Version {

	public static final Version INITIAL = new Version() {};
	
	public static class Serializer extends JsonSerializer<Version> {

		@Override
		public void serialize(Version value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			
			// TODO
			gen.writeString("COMPRESSED VERSION STRING");
		}
		
	}
	
	public static class Deserializer extends JsonDeserializer<Version> {

		@Override
		public Version deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			
			String compressed = p.getValueAsString();
			// TODO - decompression
			
			return new Version();
		}
		
	}
	
	private Version() {
		
	}
	
}
