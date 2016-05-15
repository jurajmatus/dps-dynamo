package sk.fiit.dps.team11.resources;

import java.io.UnsupportedEncodingException;
import java.nio.ByteBuffer;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.inject.Inject;
import javax.servlet.http.HttpServletRequest;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.Invocation.Builder;
import javax.ws.rs.client.InvocationCallback;
import javax.ws.rs.container.AsyncResponse;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.glassfish.jersey.client.ClientProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.core.DynamoNode;
import sk.fiit.dps.team11.core.RequestState;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.models.BaseRequest;
import sk.fiit.dps.team11.providers.InjectManager;

public class StorageExecutor {
	
	private static final Logger LOGGER = LoggerFactory.getLogger(StorageExecutor.class);

	private final BaseRequest request;
	
	@Inject
	private RequestStates states;
	
	@Inject
	private TopConfiguration conf;
	
	@Inject
	private Topology topology;
	
	private final static ObjectMapper MAPPER = new ObjectMapper();

	public static StorageExecutor create(InjectManager injectManager, BaseRequest request) {
		return injectManager.register(new StorageExecutor(request));
	}
	
	private StorageExecutor(BaseRequest request) {
		this.request = request;
	}
	
	private void handleMy(RequestState<? extends BaseRequest> state, Runnable handler) {
		states.put(state.getRequestId(), state);
		handler.run();
	}
	
	private void handleRedirect(AsyncResponse response) {
		List<DynamoNode> nodes = topology.nodesForKey(request.getKey());
		if (nodes.size() == 0) {
			LOGGER.error("No node found for key {}", ByteBuffer.wrap(request.getKey()).getLong());
		}
		
		DynamoNode coordinatorNode = nodes.get(0);
		try {
			LOGGER.info("Key {}-{} is in responsibility of node[{};{}], not my -- redirecting request", 
					ByteBuffer.wrap(request.getKey()).getLong(), new String(request.getKey(), "UTF-8"),
					coordinatorNode.getIp(), coordinatorNode.getPosition());
		} catch (UnsupportedEncodingException e1) {
			LOGGER.error("", e1);
		}

		HttpServletRequest servletRequest = request.getServletRequest();
		String url = UriBuilder.fromUri(servletRequest.getRequestURI())
				.scheme("http")
				.host(coordinatorNode.getIp())
				.port(conf.getPort())
				.replaceQuery(servletRequest.getQueryString())
				.build()
				.toString();
		
		JsonNode input;
		try {
			input = MAPPER.valueToTree(request);
		} catch (IllegalArgumentException e) {
			input = null;
		}
		
		int timeout = conf.getReliability().getNodeResponseTimeoutMillis();
		Builder requestBuilder = ClientBuilder.newClient()
			.property(ClientProperties.CONNECT_TIMEOUT, timeout)
			.property(ClientProperties.READ_TIMEOUT, timeout)
			.target(url).request();
		Invocation invocation;
		
		if (input instanceof ObjectNode && !servletRequest.getMethod().equalsIgnoreCase("GET")) {
			invocation = requestBuilder.build(servletRequest.getMethod(), Entity.entity(input, MediaType.APPLICATION_JSON));
		} else {
			invocation = requestBuilder.build(servletRequest.getMethod());
		}
		invocation.submit(new InvocationCallback<Response>() {

			@Override
			public void completed(Response resp) {
				response.resume(resp);
			}

			@Override
			public void failed(Throwable throwable) {
				if (throwable.getClass().getSimpleName().contains("Timeout")) {
					LOGGER.error("Timeout elapsed for request to {}, updating topology and retrying", url);
					topology.notifyFailedNode(coordinatorNode);
					handleRedirect(response);
				} else {
					LOGGER.error("Redirect of request to {} failed", url);
				}
			}
			
		});
	}
	
	public void execute(RequestState<? extends BaseRequest> state, Runnable handler) {
		
		LOGGER.info("Received GET request with id {}", state.getRequestId());
		
		AsyncResponse response = request.getResponse();
		response.setTimeout(conf.getReliability().getResponseTimeoutMillis(), TimeUnit.MILLISECONDS);
		response.setTimeoutHandler(resp -> {
			resp.cancel();
			states.forceRemove(state.getRequestId());
		});
		
		// The key is in this node's responsibility - it will become the coordinator
		if (topology.isMy(request.getKey())) {
			handleMy(state, handler);
		}
		
		// The key is not in this node's responsibility - the request will be redirected to the responsible node
		else {
			handleRedirect(response);
		}
	}
	
}
