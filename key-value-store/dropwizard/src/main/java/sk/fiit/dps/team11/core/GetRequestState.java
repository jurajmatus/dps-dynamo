package sk.fiit.dps.team11.core;

import java.util.Arrays;

import javax.ws.rs.container.AsyncResponse;

import org.glassfish.jersey.internal.util.Base64;

import sk.fiit.dps.team11.models.GetResponse;
import sk.fiit.dps.team11.models.Value;

public class GetRequestState extends RequestState<GetResponse> {

	public GetRequestState(AsyncResponse response, int minimum, int all) {
		super(response, minimum, all);
	}

	@Override
	protected GetResponse doRespond() {
		return new GetResponse(
				Base64.encode("TESTING KEY".getBytes()),
				Arrays.asList(new Value(Version.INITIAL, Base64.encode("ENCODED STRING".getBytes()))));
	}

}
