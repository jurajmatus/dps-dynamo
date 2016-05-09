package sk.fiit.dps.team11.workers;

import java.util.Arrays;
import java.util.UUID;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.MQ;
import sk.fiit.dps.team11.core.PutRequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.core.VersionResolution;
import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.models.RemoteGetAcknowledgement;
import sk.fiit.dps.team11.models.RemoteGetMessage;
import sk.fiit.dps.team11.models.RemotePutAcknowledgement;
import sk.fiit.dps.team11.models.RemotePutMessage;
import sk.fiit.dps.team11.models.VersionedValue;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

public class DataManipulationWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(DataManipulationWorker.class);
	
	@Inject
	DatabaseAdapter db;
	
	@Inject
	RequestStates states;
	
	@Inject
	MQ mq;
	
	@Inject
	Topology topology;
	
	@Inject
	VersionResolution versionResolution;
	
	@MQSender(topic = "find-for-key")
	private ActiveMQSender replicaFinderWorker;

	@MQListener(queue = "put")
	public void receiveLocalPut(UUID requestId) {
		
		states.withState(requestId, PutRequestState.class, s -> {
			
			PutRequest req = s.getRequest();
			
			VersionedValue oldValue = db.get(req.getKey());
			VersionedValue newValue = new VersionedValue(req.getFromVersion(), req.getValue());
			
			versionResolution.resolve(oldValue, newValue, true, valueToWrite -> {
				
				replicaFinderWorker.send(s.getRequestId());
				boolean success = db.put(req.getKey(), valueToWrite);
				s.acknowledgeForSelf(success);
				
			}, isValueCurrent -> {
				s.respondNow(isValueCurrent);
			});
			
		}, () -> LOGGER.warn("Trying to continue putting inexistent state: {}", requestId));
		
	}

	@MQListener(queue = "put-replica")
	public void receiveRemovePut(RemotePutMessage putMessage) {
		
		VersionedValue oldValue = db.get(putMessage.getKey().data);
		VersionedValue newValue = new VersionedValue(putMessage.getFromVersion(),
			Arrays.asList(putMessage.getValue()));
		
		versionResolution.resolve(oldValue, newValue, true, valueToWrite -> {
			
			boolean success = db.put(putMessage.getKey().data, valueToWrite);
			mq.send(putMessage.getFrom(), "put-ack", new RemotePutAcknowledgement(
				topology.self().getIp(), putMessage.getRequestId(), success));
			
		}, isValueCurrent -> {
			mq.send(putMessage.getFrom(), "put-ack", new RemotePutAcknowledgement(
				topology.self().getIp(), putMessage.getRequestId(), isValueCurrent));
		});
		
	}

	@MQListener(queue = "put-ack")
	public void receivePutAck(RemotePutAcknowledgement putAck) {
		
		states.withState(putAck.getRequestId(), PutRequestState.class, s -> {
			
			s.acknowledgeForNode(topology.nodeForIp(putAck.getFrom()), putAck.isSuccess());
			
		}, () -> LOGGER.warn("Trying to receive acknowledgement for inexistent state: {}", putAck.getRequestId()));
		
	}

	@MQListener(queue = "get")
	public void receiveLocalGet(UUID requestId) {
		
		// TODO
		
	}

	@MQListener(queue = "get-replica")
	public void receiveRemoveGet(RemoteGetMessage getMessage) {
		
		// TODO
		
	}

	@MQListener(queue = "get-ack")
	public void receiveGetAck(RemoteGetAcknowledgement getAck) {
		
		// TODO
		
	}

}
