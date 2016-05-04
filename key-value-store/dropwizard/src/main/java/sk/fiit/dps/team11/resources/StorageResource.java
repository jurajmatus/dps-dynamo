package sk.fiit.dps.team11.resources;

import java.util.UUID;
import java.util.function.Consumer;

import javax.inject.Inject;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.codahale.metrics.annotation.Timed;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.GetRequestState;
import sk.fiit.dps.team11.core.PutRequestState;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.models.BaseRequest;
import sk.fiit.dps.team11.models.GetRequest;
import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.providers.InjectManager;

@Path("/storage")
public class StorageResource {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageResource.class);
	
	@Inject
	private InjectManager injectManager;
	
	@Inject
	private RequestStates states;
	
	@Inject
	TopConfiguration conf;
	
	@Inject
	Topology topology;
	
	@Inject
	MetricRegistry metrics;
	
	@MQSender(topic = "put")
	private ActiveMQSender putWorker;
	
	@MQSender(topic = "get")
	private ActiveMQSender getWorker;
	
	@MQSender(topic = "find-for-key")
	private ActiveMQSender replicaFinderWorker;
	
	private <T extends BaseRequest<U, ?>, U extends RequestState<?>> void base(
			T request, Class<U> requestClass, Consumer<U> stateHandler) {
		
		Timer timer = metrics.timer(
			MetricRegistry.name(StorageResource.class, request.getLabel(), topology.self().getIp()));
		Context time = timer.time();
		
		StorageExecutor storageExecutor = StorageExecutor.create(injectManager, request);
		storageExecutor.execute(() -> {
			UUID requestId = request.getRequestState().getRequestId();
			states.withState(requestId, requestClass, stateHandler,
					() -> LOGGER.warn("Trying to work with inexistent state: {}", requestId));
		});
		
		time.stop();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Timed(name = "GET")
	@Path("{key}")
	public void doGet(@Suspended AsyncResponse response, @BeanParam GetRequest request) {
		base(request, GetRequestState.class, s -> {
			replicaFinderWorker.send(s.getRequestId());
			getWorker.send(s.getRequestId());
		});
		
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Timed(name = "PUT")
	public void doPut(@Suspended AsyncResponse response, @BeanParam PutRequest request) {
		
		base(request, PutRequestState.class, s -> {
			replicaFinderWorker.send(s.getRequestId());
			putWorker.send(s.getRequestId());
		});
		
	}
	
}
