package sk.fiit.dps.team11.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DeleteResponse {
	
	private final String key;
	
	private final boolean success;

	public DeleteResponse(@JsonProperty("key") String key,
		@JsonProperty("success") boolean success) {
		
		this.key = key;
		this.success = success;
	}

	@JsonProperty
	public String getKey() {
		return key;
	}

	@JsonProperty
	public boolean isSuccess() {
		return success;
	}

}
