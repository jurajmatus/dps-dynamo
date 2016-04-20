package sk.fiit.dps.team11.core;

import java.util.Arrays;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.PutResponse;
import sk.fiit.dps.team11.models.Value;

public class PutRequestState extends RequestState<PutResponse> {

	public PutRequestState(AsyncResponse response) {
		super(response);
	}

	@Override
	protected boolean isReady() {
		// TODO - enough read responses from peers
		return true;
	}

	@Override
	protected boolean isDone() {
		// TODO - all read responses from peers
		return true;
	}

	@Override
	protected PutResponse doRespond() {
		return new PutResponse("TODO", Arrays.asList(new Value("TODO", "TODO")));
	}

}
