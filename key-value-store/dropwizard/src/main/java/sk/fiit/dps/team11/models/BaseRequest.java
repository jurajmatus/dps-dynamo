package sk.fiit.dps.team11.models;

import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.container.AsyncResponse;

import com.fasterxml.jackson.annotation.JsonIgnore;

abstract public class BaseRequest {

	private final byte[] key;

	private AsyncResponse response;
	
	private HttpServletRequest servletRequest;

	public BaseRequest(byte[] key) {
		this.key = key;
	}

	protected void setResponse(AsyncResponse response) {
		this.response = response;
	}

	@JsonIgnore
	public AsyncResponse getResponse() {
		return response;
	}

	protected void setServletRequest(HttpServletRequest servletRequest) {
		this.servletRequest = servletRequest;
	}

	@JsonIgnore
	public HttpServletRequest getServletRequest() {
		return servletRequest;
	}
	
	public byte[] getKey() {
		return key;
	}
	
	@JsonIgnore
	abstract public String getLabel();

}