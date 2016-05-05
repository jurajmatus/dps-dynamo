package sk.fiit.dps.team11.core;

import java.util.List;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.GetRequest;
import sk.fiit.dps.team11.models.GetResponse;
import sk.fiit.dps.team11.models.VersionedValue;

public class GetRequestState extends RequestState<GetRequest> {

	public GetRequestState(AsyncResponse response, int all, GetRequest request) {
		super(response, all, request);
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
	protected int minimum() {
		return getRequest().getMinNumReads();
	}

	@Override
	protected Object provideResponse() {
		// TODO
		return new GetResponse("TESTING KEY".getBytes(),
					new VersionedValue(Version.INITIAL, "ENCODED STRING".getBytes()));
	}

}
