package sk.fiit.dps.team11.core;

import java.util.concurrent.atomic.AtomicReference;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.models.PutResponse;

public class PutRequestState extends RequestState<PutRequest> {

	private final AtomicReference<Boolean> forced = new AtomicReference<Boolean>(null);
	
	public PutRequestState(AsyncResponse response, int all, PutRequest request) {
		super(response, all, request);
	}

	@Override
	protected int minimum() {
		return getRequest().getMinNumWrites();
	}
	
	public boolean acknowledgeForNode(DynamoNode node, boolean success) {
		return super.putForNode(node, success);
	}
	
	public boolean acknowledgeForSelf(boolean success) {
		return super.putForSelf(success);
	}
	
	public void respondNow(boolean success) {
		if (forced.compareAndSet(null, success)) {
			this.respond();
		}
	}
	
	@Override
	public boolean isReady() {
		if (forced.get() != null) {
			return true;
		}
		
		return super.isReady();
	}
	
	@Override
	public boolean isDone() {
		if (forced.get() != null) {
			return true;
		}
		
		return super.isDone();
	}

	@Override
	protected Object provideResponse() {
		Boolean _forced = forced.get();
		if (_forced != null) {
			return new PutResponse(_forced);
		}
		
		boolean success = getData(Boolean.class).stream().allMatch(b -> b.booleanValue());
		return new PutResponse(success);
	}

}
