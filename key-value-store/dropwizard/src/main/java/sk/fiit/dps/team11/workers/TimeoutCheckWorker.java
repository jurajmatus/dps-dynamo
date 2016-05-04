package sk.fiit.dps.team11.workers;

import java.util.List;
import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.DynamoNode;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

public class TimeoutCheckWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutCheckWorker.class);
	
	@Inject
	RequestStates states;
	
	@Inject
	ScheduledExecutorService execService;
	
	@Inject
	Topology topology;
	
	@Inject
	TopConfiguration conf;
	
	@MQSender(topic = "find-for-key")
	ActiveMQSender replicaFinderWorker;
	
	private final class Checker implements Runnable {
		
		private final UUID uuid;
		
		public Checker(UUID uuid) {
			this.uuid = uuid;
		}
		
		private void checkForUnresponsiveNodes(RequestState<?> state) {
			if (state.isDone()) {
				return;
			}
			
			List<DynamoNode> timeoutedNodes = state.getAndRemoveTimeoutedNodes();
			
			if (timeoutedNodes.size() == 0) {
				return;
			}
			
			LOGGER.warn("Timeout elapsed for {} nodes for request {}",
				timeoutedNodes.size(), state.getRequestId());
			
			for (DynamoNode node : timeoutedNodes) {
				topology.notifyFailedNode(node);
			}
			
			replicaFinderWorker.send(state.getRequestId());
		}

		@Override
		public void run() {
			states.withState(uuid, RequestState.class, this::checkForUnresponsiveNodes, () -> {
				// No problem if state is not found - it means it's completed
				// before timeout
			});
		}
		
	}

	@MQListener(queue = "timeout-check")
	public void registerCheck(UUID requestId) {
		execService.schedule(new Checker(requestId),
			conf.getReliability().getNodeResponseTimeoutMillis(), TimeUnit.MILLISECONDS);
	}

}
