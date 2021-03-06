package sk.fiit.dps.team11.resources;

import java.util.function.Consumer;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.container.Suspended;
import javax.ws.rs.core.MediaType;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.Timer;
import com.codahale.metrics.Timer.Context;
import com.kjetland.dropwizard.activemq.ActiveMQSender;

import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.GetRequestState;
import sk.fiit.dps.team11.core.MetricsAdapter;
import sk.fiit.dps.team11.core.PutRequestState;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.core.VersionResolution;
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
	MetricsAdapter metrics;
	
	@MQSender(topic = "put")
	private ActiveMQSender putWorker;
	
	@MQSender(topic = "get")
	private ActiveMQSender getWorker;
	
	@MQSender(topic = "get-replication")
	private ActiveMQSender replicaFinderWorker;
	
	@Inject
	VersionResolution versionResolution;
	
	private <T extends BaseRequest, U extends RequestState<T>> void base(U state, Consumer<U> stateHandler) {
		
		Timer timer = metrics.get(MetricRegistry::timer, state.getRequest().getLabel());
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
	
	@POST
	@Produces(MediaType.APPLICATION_JSON)
	public void doGet(@Suspended AsyncResponse response,
			@javax.ws.rs.core.Context HttpServletRequest servletRequest,
			GetRequest request) {
		
		request.setResponse(response);
		request.setServletRequest(servletRequest);
		
		base(new GetRequestState(response, numReplicas(), request, versionResolution), s -> {
			replicaFinderWorker.send(s.getRequestId());
			getWorker.send(s.getRequestId());
		});
		
	}
	
	@PUT
	@Produces(MediaType.APPLICATION_JSON)
	public void doPut(@Suspended AsyncResponse response,
		@javax.ws.rs.core.Context HttpServletRequest servletRequest,
		PutRequest request) {
		
		request.setResponse(response);
		request.setServletRequest(servletRequest);
		
		base(new PutRequestState(response, numReplicas(), request), s -> {
			putWorker.send(s.getRequestId());
		});
		
	}
	
}
