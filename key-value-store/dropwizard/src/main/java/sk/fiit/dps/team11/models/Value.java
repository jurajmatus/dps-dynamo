package sk.fiit.dps.team11.models;

import com.fasterxml.jackson.annotation.JsonProperty;

import sk.fiit.dps.team11.core.Version;

public class Value {
	
	private final Version version;
	
	private final String value;

	public Value(@JsonProperty("version") String version,
		@JsonProperty("value") String value) {
		
		this.version = new Version();
		this.value = value;
	}

	@JsonProperty
	public String getVersion() {
		return version.toString();
	}

	@JsonProperty
	public String getValue() {
		return value;
	}

}
