package sk.fiit.dps.team11.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class SimpleEntry {

	private final String key;
	
	private final String value;

	public SimpleEntry(@JsonProperty("key") String key, @JsonProperty("value") String value) {
		this.key = key;
		this.value = value;
	}

	@JsonProperty
	public String getKey() {
		return key;
	}

	@JsonProperty
	public String getValue() {
		return value;
	}
	
}
