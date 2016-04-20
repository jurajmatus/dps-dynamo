package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.core.RequestState;

abstract public class BaseRequest<T extends RequestState<U>, U> {

	private final byte[] key;

	private final AsyncResponse response;
	
	private final HttpServletRequest servletRequest;
	
	private T requestState = null;

	public BaseRequest(byte[] key, AsyncResponse response, HttpServletRequest servletRequest) {
		this.key = key;
		this.response = response;
		this.servletRequest = servletRequest;
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

	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}

	public byte[] getKey() {
		return key;
	}
	
	abstract protected T createRequestState();
	
	abstract public String getLabel();

}