package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.core.RequestState;

abstract public class BaseRequest<T extends RequestState<U>, U> {

	private final AsyncResponse response;
	
	private T requestState = null;

	public BaseRequest(AsyncResponse response) {
		this.response = response;
	}

	public AsyncResponse getResponse() {
		return response;
	}
	
	public T getRequestState() {
		if (requestState == null) {
			requestState = createRequestState();
		}
		return requestState;
	}

	abstract public byte[] getKey();
	
	abstract protected T createRequestState();
	
	abstract public String getLabel();
	
	abstract public HttpServletRequest getServletRequest();

}