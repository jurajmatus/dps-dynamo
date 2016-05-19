package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ByteArraySerializer;

import sk.fiit.dps.team11.core.ByteArrayDeserializer;
import sk.fiit.dps.team11.core.Version;

public class PutRequest extends BaseRequest {
	
	private final byte[] value;
	
	private final Version fromVersion;
	
	private final int minNumWrites;
	
	public PutRequest(@JsonProperty("key") @JsonDeserialize(using = ByteArrayDeserializer.class) byte[] key,
			@JsonProperty("value") @JsonDeserialize(using = ByteArrayDeserializer.class) byte[] value,
			@JsonProperty("fromVersion") Version fromVersion,
			@JsonProperty("minNumWrites") int minNumWrites) {
		
		super(key);
		this.value = value;
		this.fromVersion = fromVersion == null ? Version.INITIAL : fromVersion;
		this.minNumWrites = minNumWrites;
	}

	@Override
	public String getLabel() {
		return "POST";
	}
	
	@Override
	@JsonSerialize(using = ByteArraySerializer.class)
	@JsonProperty
	public byte[] getKey() {
		return super.getKey();
	}

	@JsonSerialize(using = ByteArraySerializer.class)
	@JsonProperty
	public byte[] getValue() {
		return value;
	}

	@JsonProperty
	public Version getFromVersion() {
		return fromVersion;
	}

	@JsonProperty
	public int getMinNumWrites() {
		return minNumWrites;
	}
	
}
