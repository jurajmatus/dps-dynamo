package sk.fiit.dps.team11.workers;

import java.util.List;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjetland.dropwizard.activemq.ActiveMQSender;

import static java.util.stream.Collectors.toList;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.core.DynamoNode;
import sk.fiit.dps.team11.core.GetRequestState;
import sk.fiit.dps.team11.core.PutRequestState;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

public class ReplicaFinderWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReplicaFinderWorker.class);
	
	@Inject
	RequestStates states;
	
	@Inject
	Topology topology;
	
	@MQSender(topic = "put-replication")
	ActiveMQSender putReplicator;
	
	@MQSender(topic = "get-replication")
	ActiveMQSender getReplicator;
	
	private void handleState(RequestState<?> state) {
		List<DynamoNode> nodesForKey = topology.nodesForKey(state.getRequest().getKey());
		nodesForKey.remove(0);
		state.addNodes(nodesForKey);


		LOGGER.info("Will send replicas to the following nodes for request {}: {}", state.getRequestId(),
				nodesForKey.stream().map(node -> node.getIp()).collect(toList()));
		
		ActiveMQSender nextPhase = (state instanceof GetRequestState) ? getReplicator
			: ((state instanceof PutRequestState) ? putReplicator : null);
		
		if (nextPhase != null) {
			nextPhase.send(state.getRequestId());
		}
	}

	@MQListener(queue = "find-for-key")
	public void findReplicasForKey(UUID requestId) {
		
		states.withState(requestId, RequestState.class, this::handleState,
			() -> LOGGER.warn("Trying to find replicas for inexistent state: {}", requestId));
		
	}

}
