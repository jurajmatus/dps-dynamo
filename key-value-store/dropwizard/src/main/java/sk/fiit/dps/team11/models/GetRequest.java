package sk.fiit.dps.team11.models;

import javax.ws.rs.QueryParam;

public class GetRequest {

	private final String key;

	public GetRequest(@QueryParam("key") String key) {
		super();
		this.key = key;
	}

	public String getKey() {
		return key;
	}
	
}
