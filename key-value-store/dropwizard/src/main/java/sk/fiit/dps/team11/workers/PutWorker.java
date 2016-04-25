package sk.fiit.dps.team11.workers;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.PutRequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

public class PutWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(PutWorker.class);
	
	@Inject
	DatabaseAdapter db;
	
	@Inject
	RequestStates states;

	@MQListener(queue = "put")
	public void receiveLocalPut(UUID requestId) {
		
		states.withState(requestId, PutRequestState.class, s -> {
			// TODO - put into db
		}, () -> LOGGER.warn("Trying to continue putting inexistent state: {}", requestId));
		
	}

}
