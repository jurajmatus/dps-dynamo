package sk.fiit.dps.team11.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemotePutAcknowledgement extends RemoteMessage {
	
	private final boolean success;
	
	public RemotePutAcknowledgement(@JsonProperty("from") String from,
		@JsonProperty("requestId") UUID requestId,
		@JsonProperty("success") boolean success) {
		
		super(from, requestId);
		this.success = success;
	}

	@JsonProperty
	public boolean isSuccess() {
		return success;
	}

}
