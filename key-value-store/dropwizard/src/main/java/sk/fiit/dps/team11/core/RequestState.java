package sk.fiit.dps.team11.core;

import java.util.UUID;

import javax.ws.rs.container.AsyncResponse;

abstract public class RequestState<T> {
	
	private final UUID requestId;
	
	private AsyncResponse response;

	public RequestState(AsyncResponse response) {
		this.requestId = UUID.randomUUID();
		this.response = response;
	}
	
	public UUID getRequestId() {
		return requestId;
	}

	protected abstract boolean isReady();
	
	protected abstract boolean isDone();
	
	protected abstract T doRespond();
	
	protected void respond() {
		AsyncResponse response = null;
		synchronized (this.response) {
			if (this.response != null) {
				response = this.response;
				this.response = null;
			}
		}
		if (response != null) {
			response.resume(doRespond());
		}
	}

}
