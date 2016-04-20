package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;

import com.fasterxml.jackson.annotation.JsonProperty;

import sk.fiit.dps.team11.core.PutRequestState;
import sk.fiit.dps.team11.core.Version;

public class PutRequest extends BaseRequest<PutRequestState, PutResponse> {
	
	private final byte[] value;
	
	private final Version fromVersion;
	
	public PutRequest(@JsonProperty("key") String key,
			@JsonProperty("value") String value,
			@JsonProperty("fromVersion") Version fromVersion,
			@Suspended AsyncResponse response,
			@Context HttpServletRequest servletRequest) {
		
		super(key.getBytes(), response, servletRequest);
		this.value = value.getBytes();
		this.fromVersion = fromVersion == null ? Version.INITIAL : fromVersion;
	}

	@Override
	protected PutRequestState createRequestState() {
		return new PutRequestState(getResponse());
	}

	@Override
	public String getLabel() {
		return "POST";
	}
	
	@Override
	@JsonProperty
	public byte[] getKey() {
		return super.getKey();
	}

	@JsonProperty
	public byte[] getValue() {
		return value;
	}

	@JsonProperty
	public Version getFromVersion() {
		return fromVersion;
	}
	
}
