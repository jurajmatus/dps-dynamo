package sk.fiit.dps.team11.resources;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.BeanParam;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

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
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.models.BaseRequest;
import sk.fiit.dps.team11.models.GetRequest;
import sk.fiit.dps.team11.models.PutRequest;
import sk.fiit.dps.team11.providers.InjectManager;

@Path("/storage")
public class StorageResource {
	
	@Inject
	private InjectManager injectManager;
	
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
	
	private <T extends BaseRequest, U extends RequestState<T>> void base(U state, Consumer<U> stateHandler) {
		
		Timer timer = metrics.timer(
			MetricRegistry.name(StorageResource.class, state.getRequest().getLabel(), topology.self().getIp()));
		Context time = timer.time();
		
		StorageExecutor storageExecutor = StorageExecutor.create(injectManager, state.getRequest());
		storageExecutor.execute(state, () -> {
			stateHandler.accept(state);
		});
		
		time.stop();
	}
	
	private int numReplicas() {
		return conf.getReliability().getNumReplicas();
	}
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	@Timed(name = "GET")
	@Path("{key}")
	public void doGet(@Suspended AsyncResponse response, @BeanParam GetRequest request) {
		
		base(new GetRequestState(response, numReplicas(), request), s -> {
			replicaFinderWorker.send(s.getRequestId());
			getWorker.send(s.getRequestId());
		});
		
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	@Timed(name = "PUT")
	public void doPut(@Suspended AsyncResponse response,
		@javax.ws.rs.core.Context HttpServletRequest servletRequest, PutRequest request) {
		
		request.setResponse(response);
		request.setServletRequest(servletRequest);
		
		base(new PutRequestState(response, numReplicas(), request), s -> {
			replicaFinderWorker.send(s.getRequestId());
			putWorker.send(s.getRequestId());
		});
		
	}
	
}
