package sk.fiit.dps.team11.core;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.PutResponse;

public class PutRequestState extends RequestState<PutResponse> {

	public PutRequestState(AsyncResponse response, byte[] key, int minimum, int all) {
		super(response, key, minimum, all);
	}

	@Override
	protected PutResponse doRespond() {
		return new PutResponse(true);
	}

}
