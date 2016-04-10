package sk.fiit.dps.team11.core;

import java.util.Arrays;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.GetResponse;
import sk.fiit.dps.team11.models.Value;

public class GetRequestState extends RequestState<GetResponse> {

	public GetRequestState(AsyncResponse response) {
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
	protected GetResponse doRespond() {
		return new GetResponse("TODO", Arrays.asList(new Value("TODO", "TODO")));
	}

}
