package sk.fiit.dps.team11.models;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PutResponse {
	
	private final String key;
	
	private final List<Value> values;

	public PutResponse(@JsonProperty("key") String key,
		@JsonProperty("values") List<Value> values) {
		
		this.key = key;
		this.values = values;
	}

	@JsonProperty
	public String getKey() {
		return key;
	}

	@JsonProperty
	public List<Value> getValues() {
		return values;
	}

}
