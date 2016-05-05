package sk.fiit.dps.team11.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoteGetAcknowledgement extends RemoteMessage {
	
	private final VersionedValue value;
	
	public RemoteGetAcknowledgement(@JsonProperty("from") String from,
		@JsonProperty("requestId") UUID requestId,
		@JsonProperty("value") VersionedValue value) {
		
		super(from, requestId);
		this.value = value;
	}

	@JsonProperty
	public VersionedValue getValue() {
		return value;
	}

}
