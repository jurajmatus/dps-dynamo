package sk.fiit.dps.team11.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetResponse {
	
	private final ByteArray key;
	
	private final VersionedValue value;

	public GetResponse(byte[] key, VersionedValue value) {
		this(new ByteArray(key), value);
	}
	
	public GetResponse(@JsonProperty("key") ByteArray key,
		@JsonProperty("value") VersionedValue value) {
		
		this.key = key;
		this.value = value;
	}

	@JsonProperty
	public ByteArray getKey() {
		return key;
	}

	@JsonProperty
	public VersionedValue getValue() {
		return value;
	}

}
