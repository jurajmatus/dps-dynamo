package sk.fiit.dps.team11.workers;

import javax.inject.Inject;
import javax.jms.Message;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.kjetland.dropwizard.activemq.ActiveMQBaseExceptionHandler;
import com.kjetland.dropwizard.activemq.ActiveMQReceiver;

import sk.fiit.dps.team11.providers.DatabaseAdapter;

public class SampleWorker implements ActiveMQReceiver<String>, ActiveMQBaseExceptionHandler {

	private static final Logger LOGGER = LoggerFactory.getLogger(SampleWorker.class);
	
	@Inject
	private DatabaseAdapter db;
	
	@Override
	public boolean onException(Message jmsMessage, String message, Exception exception) {
		
		LOGGER.error("Error reading from queue: {}", exception);
		
		return false;
	}

	@Override
	public void receive(String message) {
		
		LOGGER.info("Message from queue: {}", message);
		
	}

}
