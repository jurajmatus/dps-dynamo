package sk.fiit.dps.team11.resources;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.annotation.Timed;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.GetRequestState;
import sk.fiit.dps.team11.core.MQ;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.models.GetRequest;

@Path("/storage")
public class StorageResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageResource.class);
	
	@Inject
	private DatabaseAdapter db;
	
	@Inject
	private MQ mq;
	
	@Inject
	private RequestStates states;
	
	@MQSender(topic = "insert")
	private ActiveMQSender insertWorker;
	
	@Inject
	private MetricRegistry metrics;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Timed(name = "GET")
	public void doGet(@Suspended AsyncResponse response, @BeanParam GetRequest request) {
		
		// TODO - query param - > path param
		// TODO - isMy ? execute : redirect
		// TODO - async response: timeout
		// TODO - extract to storage executor
		
		GetRequestState state = new GetRequestState(response);
		LOGGER.info("Received GET request with id {}", state.getRequestId());
		
		states.put(state.getRequestId(), state);
		// TODO - put into some message queue
		
		states.withState(state.getRequestId(), GetRequestState.class, r -> "", () -> "");
		
	}
	
	// TODO - PUT, DELETE
	
}
