package sk.fiit.dps.team11.core;

import java.io.IOException;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import sk.fiit.dps.team11.core.Version.Deserializer;
import sk.fiit.dps.team11.core.Version.Serializer;

@JsonSerialize(using = Serializer.class)
@JsonDeserialize(using = Deserializer.class)
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
	
	public static enum Comp {
		FIRST_NEWER,
		SECOND_NEWER,
		MIXED,
		EQUAL
	}
	
	public static Comp compare(Version v1, Version v2) {
		// TODO
		return Comp.EQUAL;
	}
	
	private Version() {
		
	}
	
	public Version increment(DynamoNode atNode) {
		// TODO - pass modified set to constructor
		return new Version();
	}
	
	public Version merge(Version version) {
		// TODO - pass set of maximums to constructors
		return new Version();
	}
	
}
