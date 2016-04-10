package sk.fiit.dps.team11.core;

import javax.jms.Connection;
import javax.jms.DeliveryMode;
import javax.jms.Destination;
import javax.jms.ExceptionListener;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class MQ {

	private static final Logger LOGGER = LoggerFactory.getLogger(MQ.class);
	
	private static final ObjectMapper MAPPER = new ObjectMapper();
	
	public boolean send(String ip, String queueName, Object message) {
		return send(ip, queueName, message, ex ->
			LOGGER.error("Error sending message to queue at " + ip, ex));
	}
	
	public boolean send(String ip, String queueName, Object message, ExceptionListener onException) {
		ActiveMQConnectionFactory factory = new ActiveMQConnectionFactory(String.format("tcp://%s:61616", ip));
		
		Connection connection = null;
		Session session = null;
		
		try {
			connection = factory.createConnection();
			connection.start();
			connection.setExceptionListener(onException);
			
			session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
			Destination destination = session.createQueue(queueName);
			
			MessageProducer producer = session.createProducer(destination);
			producer.setDeliveryMode(DeliveryMode.NON_PERSISTENT);
			
			TextMessage msg = session.createTextMessage(MAPPER.writeValueAsString(message));
			producer.send(msg);
			
			return true;
		} catch (Exception e) {
			try {
				if (session != null) {
					session.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (JMSException e1) {}
			return false;
		}		
		
	}

}
