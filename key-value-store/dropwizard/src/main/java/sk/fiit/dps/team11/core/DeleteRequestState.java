package sk.fiit.dps.team11.core;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.DeleteResponse;

public class DeleteRequestState extends RequestState<DeleteResponse> {

	public DeleteRequestState(AsyncResponse response) {
		super(response);
	}

	@Override
	protected boolean isReady() {
		// TODO - enough ack responses from peers
		return true;
	}

	@Override
	protected boolean isDone() {
		// TODO - all ack responses from peers
		return true;
	}

	@Override
	protected DeleteResponse doRespond() {
		return new DeleteResponse("TODO", true);
	}

}
