package sk.fiit.dps.team11.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer;

import sk.fiit.dps.team11.core.ByteArrayDeserializer;

public class GetResponse {
	
	private final byte[] key;
	
	private final List<Value> values;

	public GetResponse(@JsonProperty("key") @JsonDeserialize(using = ByteArrayDeserializer.class) byte[] key,
		@JsonProperty("values") List<Value> values) {
		
		this.key = key;
		this.values = values;
	}

	@JsonProperty
	@JsonSerialize(using = ByteArraySerializer.class)
	public byte[] getKey() {
		return key;
	}

	@JsonProperty
	public List<Value> getValues() {
		return values;
	}

}
