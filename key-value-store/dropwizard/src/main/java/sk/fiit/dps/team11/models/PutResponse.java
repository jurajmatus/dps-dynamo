package sk.fiit.dps.team11.models;

import com.fasterxml.jackson.annotation.JsonProperty;

public class PutResponse {
	
	private final boolean success;

	public PutResponse(@JsonProperty("success") boolean success) {
		this.success = success;
	}

	@JsonProperty
	public boolean isSuccess() {
		return success;
	}

}
