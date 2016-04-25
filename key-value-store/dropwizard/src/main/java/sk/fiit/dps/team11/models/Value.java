package sk.fiit.dps.team11.models;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer;

import sk.fiit.dps.team11.core.ByteArrayDeserializer;
import sk.fiit.dps.team11.core.Version;

public class Value {
	
	private final Version version;
	
	private final byte[] value;

	public Value(@JsonProperty("version") String version,
		@JsonProperty("value") @JsonDeserialize(using = ByteArrayDeserializer.class) byte[] value) {
		
		this.version = new Version();
		this.value = value;
	}

	@JsonProperty
	public String getVersion() {
		return version.toString();
	}

	@JsonProperty
	@JsonSerialize(using = ByteArraySerializer.class)
	public byte[] getValue() {
		return value;
	}

}
