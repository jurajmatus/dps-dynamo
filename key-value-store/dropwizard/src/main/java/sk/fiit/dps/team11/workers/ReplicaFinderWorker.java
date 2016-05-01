package sk.fiit.dps.team11.workers;

import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

public class ReplicaFinderWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReplicaFinderWorker.class);
	
	@Inject
	RequestStates states;

	@MQListener(queue = "find-for-key")
	public void findReplicasForKey(UUID requestId) {
		
		// TODO
		
	}

}
