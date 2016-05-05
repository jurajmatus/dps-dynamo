package sk.fiit.dps.team11.core;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.models.PutResponse;

public class PutRequestState extends RequestState<PutRequest> {

	public PutRequestState(AsyncResponse response, int all, PutRequest request) {
		super(response, all, request);
	}

	@Override
	protected int minimum() {
		return getRequest().getMinNumWrites();
	}

	@Override
	protected Object provideResponse() {
		// TODO
		return new PutResponse(true);
	}

}
