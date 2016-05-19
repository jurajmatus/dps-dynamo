package sk.fiit.dps.team11.workers;

import java.util.Arrays;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.GetRequestState;
import sk.fiit.dps.team11.core.MQ;
import sk.fiit.dps.team11.core.MetricsAdapter;
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
	
	@Inject
	ScheduledExecutorService execService;
	
	@MQSender(topic = "find-for-key")
	private ActiveMQSender replicaFinderWorker;
	
	@Inject
	private MetricsAdapter metrics;

	@MQListener(queue = "put")
	public void receiveLocalPut(UUID requestId) {
		
		states.withState(requestId, PutRequestState.class, s -> {
			
			metrics.get(MetricRegistry::meter, "coordinator-put").mark();
			
			PutRequest req = s.getRequest();

			LOGGER.info("Inserting item: KEY='{}', VAL='{}', VERSION='{}'\n", 
					req.getKey(), req.getValue(), req.getFromVersion());
			
			VersionedValue oldValue = db.get(req.getKey());
			VersionedValue newValue = new VersionedValue(req.getFromVersion(), req.getValue());
			
			versionResolution.resolve(oldValue, newValue, true, valueToWrite -> {
				
				replicaFinderWorker.send(s.getRequestId());
				boolean success = db.put(req.getKey(), valueToWrite, oldValue);
				
				if (success) {				
					s.acknowledgeForSelf(success);
					metrics.get(MetricRegistry::meter, "optimistic-put-success").mark();
				} else {
					execService.execute(() -> receiveLocalPut(requestId));
					metrics.get(MetricRegistry::meter, "optimistic-put-failure").mark();
				}
				
			}, isValueCurrent -> {
				s.respondNow(isValueCurrent);
				if (!isValueCurrent) {
					metrics.get(MetricRegistry::meter, "old-version-put-failure").mark();
				}
			});
			
		}, () -> LOGGER.warn("Trying to continue putting inexistent state: {}", requestId));
		
	}

	@MQListener(queue = "put-replica")
	public void receiveRemovePut(RemotePutMessage putMessage) {
		
		VersionedValue oldValue = db.get(putMessage.getKey().data);
		VersionedValue newValue = new VersionedValue(putMessage.getFromVersion(),
			Arrays.asList(putMessage.getValue()));
		
		versionResolution.resolve(oldValue, newValue, false, valueToWrite -> {
			
			metrics.get(MetricRegistry::meter, "replica-put").mark();
			
			boolean success = db.put(putMessage.getKey().data, valueToWrite, oldValue);
			mq.send(putMessage.getFrom(), "put-ack", new RemotePutAcknowledgement(
				topology.self().getIp(), putMessage.getRequestId(), success));
			
			LOGGER.info("Received fresh put replication data message for request {}, success: {}",
					putMessage.getRequestId(), success);
			
		}, isValueCurrent -> {
			
			LOGGER.info("Received stale put replication data message for request {}, success: {}",
					putMessage.getRequestId(), isValueCurrent);
			
			metrics.get(MetricRegistry::meter, "replica-put").mark();
			
			mq.send(putMessage.getFrom(), "put-ack", new RemotePutAcknowledgement(
				topology.self().getIp(), putMessage.getRequestId(), isValueCurrent));
		});
		
	}

	@MQListener(queue = "put-ack")
	public void receivePutAck(RemotePutAcknowledgement putAck) {
		
		LOGGER.info("Received put replication acknowledgement for request {}", putAck.getRequestId());
		
		states.withState(putAck.getRequestId(), PutRequestState.class, s -> {
			
			metrics.get(MetricRegistry::meter, "put-ack").mark();
			
			s.acknowledgeForNode(topology.nodeForIp(putAck.getFrom()), putAck.isSuccess());
			
		}, () -> LOGGER.warn("Trying to receive put acknowledgement for inexistent state: {}", putAck.getRequestId()));
		
	}

	@MQListener(queue = "get")
	public void receiveLocalGet(UUID requestId) {
		
		states.withState(requestId, GetRequestState.class, s -> {
			
			metrics.get(MetricRegistry::meter, "coordinator-get").mark();
			
			VersionedValue value = db.get(s.getRequest().getKey());
			s.putDataForSelf(value);
			
		}, () -> LOGGER.warn("Trying to continue getting inexistent state: {}", requestId));
		
	}

	@MQListener(queue = "get-replica")
	public void receiveRemoveGet(RemoteGetMessage getMessage) {
		
		metrics.get(MetricRegistry::meter, "replica-get").mark();
		
		VersionedValue value = db.get(getMessage.getKey().data);
		
		mq.send(getMessage.getFrom(), "get-ack", new RemoteGetAcknowledgement(
				topology.self().getIp(), getMessage.getRequestId(), value));
		
		LOGGER.info("Received get replication data message for request {}, success: {}",
				getMessage.getRequestId(), value);
		
	}

	@MQListener(queue = "get-ack")
	public void receiveGetAck(RemoteGetAcknowledgement getAck) {
		
		LOGGER.info("Received get replication acknowledgement for request {}", getAck.getRequestId());
		
		states.withState(getAck.getRequestId(), GetRequestState.class, s -> {
			
			metrics.get(MetricRegistry::meter, "get-ack").mark();
			
			s.putDataForNode(topology.nodeForIp(getAck.getFrom()), getAck.getValue());
			
		}, () -> LOGGER.warn("Trying to receive get acknowledgement for inexistent state: {}", getAck.getRequestId()));
		
	}

}
