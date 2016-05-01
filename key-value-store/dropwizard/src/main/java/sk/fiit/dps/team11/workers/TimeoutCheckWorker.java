package sk.fiit.dps.team11.workers;

import java.util.UUID;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.workers.WrappedMethodWorker.MQListener;

public class TimeoutCheckWorker {

	private static final Logger LOGGER = LoggerFactory.getLogger(TimeoutCheckWorker.class);
	
	@Inject
	RequestStates states;
	
	@Inject
	ScheduledExecutorService execService;
	
	@Inject
	TopConfiguration conf;
	
	private final class Checker implements Runnable {
		
		private final UUID uuid;
		
		public Checker(UUID uuid) {
			this.uuid = uuid;
		}

		@Override
		public void run() {
			// TODO - check for unresponsive nodes, notify topology of dead ones
			// and inititate new replication
			states.withState(uuid, RequestState.class, s -> {}, () -> {});
		}
		
	}

	@MQListener(queue = "timeout-check")
	public void registerCheck(UUID requestId) {
		execService.schedule(new Checker(requestId),
			conf.getReliability().getNodeResponseTimeoutMillis(), TimeUnit.MILLISECONDS);
	}

}
