package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.PathParam;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.Context;

import sk.fiit.dps.team11.core.DeleteRequestState;

public class DeleteRequest extends BaseRequest<DeleteRequestState, DeleteResponse> {
	
	public DeleteRequest(@PathParam("key") String key,
			@Suspended AsyncResponse response,
			@Context HttpServletRequest servletRequest) {
		
		super(key.getBytes(), response, servletRequest);
	}

	@Override
	protected DeleteRequestState createRequestState() {
		return new DeleteRequestState(getResponse());
	}

	@Override
	public String getLabel() {
		return "DELETE";
	}
	
}
