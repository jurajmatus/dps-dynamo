package sk.fiit.dps.team11.workers;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

public class ReplicationWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationWorker.class);
	
	@Inject
	RequestStates states;

	@MQListener(queue = "put-replication")
	public void sendPutReplicas(UUID requestId) {
		
		// TODO
		
	}

	@MQListener(queue = "get-replication")
	public void coordinateGetReplicas(UUID requestId) {
		
		// TODO
		
	}

}
