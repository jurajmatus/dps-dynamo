package sk.fiit.dps.team11.core;

import java.util.Collections;
import java.util.List;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.GetRequest;
import sk.fiit.dps.team11.models.GetResponse;
import sk.fiit.dps.team11.models.VersionedValue;

public class GetRequestState extends RequestState<GetRequest> {
	
	private final VersionResolution versionResolution;

	public GetRequestState(AsyncResponse response, int all, GetRequest request,
		VersionResolution versionResolution) {
		
		super(response, all, request);
		this.versionResolution = versionResolution;
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
		VersionedValue value = getData(VersionedValue.class).stream()
			.reduce((val1, val2) -> versionResolution.resolve(val2, val1, false, v -> {}, isUTD -> {}))
			.orElse(new VersionedValue(Version.INITIAL, Collections.emptyList()));
		
		return new GetResponse(getRequest().getKey(), value);
	}

}
