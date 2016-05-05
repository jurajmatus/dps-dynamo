package sk.fiit.dps.team11.workers;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.core.DynamoNode;
import sk.fiit.dps.team11.core.MQ;
import sk.fiit.dps.team11.core.PutRequestState;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.models.ByteArray;
import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.models.RemoteGetMessage;
import sk.fiit.dps.team11.models.RemotePutMessage;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

public class ReplicationWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(ReplicationWorker.class);
	
	@Inject
	RequestStates states;
	
	@MQSender(topic = "timeout-check")
	ActiveMQSender timeoutCheckerWorker;
	
	@Inject
	Topology topology;
	
	@Inject
	MQ mq;
	
	private void sendToAllNodes(RequestState<?> state, String mqPrefix, Supplier<Object> messageProvider) {
		
		List<DynamoNode> toContact = state.getNodesWithoutResponse();
		
		for (DynamoNode node : toContact) {
			
			mq.send(node.getIp(), String.format("%s-replica", mqPrefix), messageProvider.get());
			
		}
		
		timeoutCheckerWorker.send(state.getRequestId());
		
	}

	@MQListener(queue = "put-replication")
	public void sendPutReplicas(UUID requestId) {
		
		states.withState(requestId, PutRequestState.class, s -> sendToAllNodes(s, "put", () -> {
			
			PutRequest request = s.getRequest();
			
			return new RemotePutMessage(topology.self().getIp(), requestId,
				new ByteArray(request.getKey()), request.getFromVersion(), new ByteArray(request.getValue()));
			
		}), () -> LOGGER.warn("Trying to send replicas for inexistent state: {}", requestId));
		
	}

	@MQListener(queue = "get-replication")
	public void coordinateGetReplicas(UUID requestId) {
		
		states.withState(requestId, PutRequestState.class, s -> sendToAllNodes(s, "get", () -> {
			
			return new RemoteGetMessage(topology.self().getIp(), requestId,
				new ByteArray(s.getRequest().getKey()));
			
		}), () -> LOGGER.warn("Trying to get replicas for inexistent state: {}", requestId));
		
	}

}
