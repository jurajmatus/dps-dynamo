package sk.fiit.dps.team11.core;

import java.util.List;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.GetResponse;
import sk.fiit.dps.team11.models.VersionedValue;

public class GetRequestState extends RequestState<GetResponse> {

	public GetRequestState(AsyncResponse response, byte[] key, int minimum, int all) {
		super(response, key, minimum, all);
	}
	
	public boolean putDataForNode(DynamoNode node, VersionedValue data) {
		return super.putForNode(node, data);
	}
	
	public boolean putDataForSelf(VersionedValue data) {
		return super.putForSelf(data);
	}
	
	public List<VersionedValue> getAllData() {
		return getData(VersionedValue.class);
	}

	@Override
	protected GetResponse doRespond() {
		return new GetResponse("TESTING KEY".getBytes(),
					new VersionedValue(Version.INITIAL, "ENCODED STRING".getBytes()));
	}

}
