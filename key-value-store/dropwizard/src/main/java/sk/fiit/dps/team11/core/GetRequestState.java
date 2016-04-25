package sk.fiit.dps.team11.core;

import java.util.Arrays;

import javax.ws.rs.container.AsyncResponse;

import org.glassfish.jersey.internal.util.Base64;

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
		return new GetResponse(
				Base64.encode("TESTING KEY".getBytes()),
				Arrays.asList(new Value("VERSION", Base64.encode("ENCODED STRING".getBytes()))));
	}

}
