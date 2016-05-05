package sk.fiit.dps.team11.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

public class RemoteGetMessage extends RemoteMessage {

	private final ByteArray key;
	
	public RemoteGetMessage(@JsonProperty("from") String from,
		@JsonProperty("requestId") UUID requestId,
		@JsonProperty("key") ByteArray key) {
		
		super(from, requestId);
		this.key = key;
	}

	@JsonProperty
	public ByteArray getKey() {
		return key;
	}

}
