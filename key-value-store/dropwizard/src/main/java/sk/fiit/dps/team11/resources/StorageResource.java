package sk.fiit.dps.team11.resources;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.annotation.Timed;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.MQ;
import sk.fiit.dps.team11.models.GetRequest;
import sk.fiit.dps.team11.providers.InjectManager;

@Path("/storage")
public class StorageResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageResource.class);
	
	@Inject
	private DatabaseAdapter db;
	
	@Inject
	private MQ mq;
	
	@Inject
	private InjectManager injectManager;
	
	@MQSender(topic = "insert")
	private ActiveMQSender insertWorker;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Timed(name = "GET")
	@Path("{key}")
	public void doGet(@BeanParam GetRequest request) {
		
		StorageExecutor storageExecutor = StorageExecutor.create(injectManager, request);
		storageExecutor.execute(() -> {
			// TODO - put into message queue
		});
		
	}
	
	// TODO - PUT, DELETE
	
}
