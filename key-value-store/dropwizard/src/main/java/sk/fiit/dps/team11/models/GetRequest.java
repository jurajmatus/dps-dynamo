package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.PathParam;
import javax.ws.rs.QueryParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;

import org.glassfish.jersey.internal.util.Base64;

import com.fasterxml.jackson.annotation.JsonProperty;

public class GetRequest extends BaseRequest {
	
	private final int minNumReads;
	
	public GetRequest(@PathParam("key") String key,
			@Suspended AsyncResponse response,
			@Context HttpServletRequest servletRequest,
			@QueryParam("minNumReads") @DefaultValue("1") int minNumReads) {
		
		super(Base64.decode(key.getBytes()));
		this.minNumReads = minNumReads;
		this.setResponse(response);
		this.setServletRequest(servletRequest);
	}

	@Override
	public String getLabel() {
		return "GET";
	}

	@JsonProperty
	public int getMinNumReads() {
		return minNumReads;
	}
	
}
