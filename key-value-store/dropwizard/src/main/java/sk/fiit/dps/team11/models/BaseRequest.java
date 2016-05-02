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
		return requestState;
	}

	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}
	
	public byte[] getKey() {
		return key;
	}
	
	public T createRequestState(int numReplicas) {
		requestState = _createRequestState(numReplicas);
		return requestState;
	}
	
	abstract public T _createRequestState(int numReplicas);
	
	abstract public String getLabel();

}