package sk.fiit.dps.team11.workers;

import java.util.UUID;

import javax.inject.Inject;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjetland.dropwizard.activemq.ActiveMQBaseExceptionHandler;
import com.kjetland.dropwizard.activemq.ActiveMQReceiver;

import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.PutRequestState;
import sk.fiit.dps.team11.core.RequestStates;

public class PutWorker implements ActiveMQReceiver<UUID>, ActiveMQBaseExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(PutWorker.class);
	
	@Inject
	DatabaseAdapter db;
	
	@Inject
	RequestStates states;
	
	@Override
	public boolean onException(Message jmsMessage, String message, Exception exception) {
		
		LOGGER.error("Error reading from queue: {}", exception);
		
		return false;
	}

	@Override
	public void receive(UUID requestId) {
		
		states.withState(requestId, PutRequestState.class, s -> {
			// TODO - put into db
		}, () -> LOGGER.warn("Trying to continue putting inexistent state: {}", requestId));
		
	}

}
