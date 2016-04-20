package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;

import sk.fiit.dps.team11.core.GetRequestState;

public class GetRequest extends BaseRequest<GetRequestState, GetResponse> {
	
	public GetRequest(@PathParam("key") String key,
			@Suspended AsyncResponse response,
			@Context HttpServletRequest servletRequest) {
		
		super(key.getBytes(), response, servletRequest);
	}

	@Override
	protected GetRequestState createRequestState() {
		return new GetRequestState(getResponse());
	}

	@Override
	public String getLabel() {
		return "GET";
	}
	
}
