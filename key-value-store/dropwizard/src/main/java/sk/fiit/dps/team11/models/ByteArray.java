package sk.fiit.dps.team11.models;

import java.io.IOException;
import java.util.Arrays;

import org.apache.commons.codec.binary.Base64;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.JsonSerializer;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;

import sk.fiit.dps.team11.models.ByteArray.Deserializer;
import sk.fiit.dps.team11.models.ByteArray.Serializer;

@JsonSerialize(using = Serializer.class)
@JsonDeserialize(using = Deserializer.class)
public class ByteArray {
	
	public static class Serializer extends JsonSerializer<ByteArray> {

		@Override
		public void serialize(ByteArray value, JsonGenerator gen, SerializerProvider serializers)
				throws IOException, JsonProcessingException {
			
			gen.writeString(value.toBase64());
		}
		
	}
	
	public static class Deserializer extends JsonDeserializer<ByteArray> {

		@Override
		public ByteArray deserialize(JsonParser p, DeserializationContext ctxt)
				throws IOException, JsonProcessingException {
			
			return new ByteArray(Base64.decodeBase64(p.getValueAsString("")));
		}
		
	}
	
	public final byte[] data;
	
	public ByteArray(byte[] data) {
		this.data = data;
	}
	
	public String toBase64() {
		return Base64.encodeBase64String(data);
	}
	
	@Override
	public String toString() {
		return toBase64();
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + Arrays.hashCode(data);
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		ByteArray other = (ByteArray) obj;
		if (!Arrays.equals(data, other.data))
			return false;
		return true;
	}
	
}
