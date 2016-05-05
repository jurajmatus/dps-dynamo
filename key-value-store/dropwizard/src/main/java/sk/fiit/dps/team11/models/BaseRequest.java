package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;

abstract public class BaseRequest {

	private final byte[] key;

	private final AsyncResponse response;
	
	private final HttpServletRequest servletRequest;

	public BaseRequest(byte[] key, AsyncResponse response, HttpServletRequest servletRequest) {
		this.key = key;
		this.response = response;
		this.servletRequest = servletRequest;
	}

	public AsyncResponse getResponse() {
		return response;
	}

	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}
	
	public byte[] getKey() {
		return key;
	}
	
	abstract public String getLabel();

}