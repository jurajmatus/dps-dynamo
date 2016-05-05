package sk.fiit.dps.team11.models;

import java.util.UUID;

import com.fasterxml.jackson.annotation.JsonProperty;

import sk.fiit.dps.team11.core.Version;

public class RemotePutMessage extends RemoteMessage {

	private final ByteArray key;
	
	private final Version fromVersion;
	
	private final ByteArray value;
	
	public RemotePutMessage(@JsonProperty("from") String from,
		@JsonProperty("requestId") UUID requestId,
		@JsonProperty("key") ByteArray key,
		@JsonProperty("fromVersion") Version fromVersion,
		@JsonProperty("value") ByteArray value) {
		
		super(from, requestId);
		this.key = key;
		this.fromVersion = fromVersion;
		this.value = value;
	}

	@JsonProperty
	public ByteArray getKey() {
		return key;
	}

	@JsonProperty
	public Version getFromVersion() {
		return fromVersion;
	}

	@JsonProperty
	public ByteArray getValue() {
		return value;
	}

}
