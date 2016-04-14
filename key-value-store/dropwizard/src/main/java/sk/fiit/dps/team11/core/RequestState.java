package sk.fiit.dps.team11.core;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.container.AsyncResponse;

abstract public class RequestState<T> {
	
	private final UUID requestId;
	
	private final AsyncResponse response;
	
	private final AtomicBoolean responseSent = new AtomicBoolean(false);

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
		if (!responseSent.getAndSet(true)) {
			response.resume(doRespond());
		}
	}

}