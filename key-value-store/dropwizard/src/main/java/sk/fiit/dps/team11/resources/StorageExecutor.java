package sk.fiit.dps.team11.resources;

import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.container.AsyncResponse;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.DynamoNode;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.models.BaseRequest;
import sk.fiit.dps.team11.providers.InjectManager;

public class StorageExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageExecutor.class);

	@Inject
	private final BaseRequest<?, ?> request;
	
	@Inject
	private RequestStates states;
	
	@Inject
	private TopConfiguration conf;
	
	@Inject
	private Topology topology;

	public static StorageExecutor create(InjectManager injectManager, BaseRequest<?, ?> request) {
		return injectManager.register(new StorageExecutor(request));
	}
	
	private StorageExecutor(BaseRequest<?, ?> request) {
		this.request = request;
	}
	
	private void handleMy(Runnable handler) {
		RequestState<?> state = request.getRequestState();
		states.put(state.getRequestId(), state);
		
		handler.run();
	}
	
	private void handleRedirect(AsyncResponse response) {
		List<DynamoNode> nodes = topology.nodesForKey(request.getKey());
		
		if (nodes.size() == 0) {
			LOGGER.error("No node found for key {}", new String(request.getKey()));
		}
		
		DynamoNode coordinatorNode = nodes.get(0);
		
		String url = String.format("http://%s:%d%s", coordinatorNode.getIp(), conf.getPort(), "" /* TODO - Path */);
		HttpServletRequest servletRequest = request.getServletRequest();

		ClientBuilder.newClient().target(url).request()
			.build(servletRequest.getMethod())
			// TODO - query parameters
			// TODO - input stream
			.invoke();
		
		// TODO
	}
	
	public void execute(Runnable handler) {
		LOGGER.info("Received GET request with id {}", request.getRequestState());
		
		AsyncResponse response = request.getResponse();
		response.setTimeout(conf.getReliability().getResponseTimeoutMillis(), TimeUnit.MILLISECONDS);
		
		// The key is in this node's responsibility - it will become the coordinator
		if (topology.isMy(request.getKey())) {
			handleMy(handler);
		}
		
		// The key is not in this node's responsibility - the request will be redirected to the responsible node
		else {
			handleRedirect(response);
		}
	}
	
}
