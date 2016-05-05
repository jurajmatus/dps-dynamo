package sk.fiit.dps.team11.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

abstract public class RemoteMessage {

	private final String from;
	
	private final UUID requestId;

	public RemoteMessage(String from, UUID requestId) {
		super();
		this.from = from;
		this.requestId = requestId;
	}

	@JsonProperty
	public String getFrom() {
		return from;
	}

	@JsonProperty
	public UUID getRequestId() {
		return requestId;
	}
	
}
