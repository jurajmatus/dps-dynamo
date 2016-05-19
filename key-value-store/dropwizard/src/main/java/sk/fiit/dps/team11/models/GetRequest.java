package sk.fiit.dps.team11.models;

import javax.ws.rs.DefaultValue;

import org.glassfish.jersey.internal.util.Base64;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetRequest extends BaseRequest {
	
	private final int minNumReads;
	
	public GetRequest(@JsonProperty("key") String key,
			@JsonProperty("minNumReads") @DefaultValue("1") int minNumReads) {
		
		super(Base64.decode(key.getBytes()));
		this.minNumReads = minNumReads;
	}

	@Override
	public String getLabel() {
		return "GET";
	}

	@JsonProperty
	public int getMinNumReads() {
		return minNumReads;
	}
	
	@Override
	@JsonProperty
	public byte[] getKey() {
		// TODO Auto-generated method stub
		return super.getKey();
	}
	
}
