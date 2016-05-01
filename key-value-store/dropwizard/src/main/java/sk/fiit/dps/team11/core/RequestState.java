package sk.fiit.dps.team11.core;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.container.AsyncResponse;


abstract public class RequestState<T> {
	
	private final UUID requestId;
	
	private final AsyncResponse response;
	
	private final AtomicBoolean responseSent = new AtomicBoolean(false);
	
	private final int minimum;
	
	private final int all;
	
	private Optional<Object> dataFromSelf = Optional.empty();
	
	private final Map<DynamoNode, Optional<Object>> data = new TreeMap<>();

	public RequestState(AsyncResponse response, int minimum, int all) {
		this.requestId = UUID.randomUUID();
		this.response = response;
		this.minimum = minimum;
		this.all = all;
	}
	
	public UUID getRequestId() {
		return requestId;
	}
	
	public void addNodes(DynamoNode... nodes) {
		synchronized (this.data) {
			for (DynamoNode node : nodes) {
				this.data.put(node, Optional.empty());
			}
		}
	}
	
	public List<DynamoNode> getAndRemoveTimeoutedNodes() {
		List<DynamoNode> nodes = new ArrayList<>();
		synchronized (this.data) {
			for (Entry<DynamoNode, Optional<Object>> entry : this.data.entrySet()) {
				if (!entry.getValue().isPresent()) {
					nodes.add(entry.getKey());
					this.data.remove(entry.getKey());
				}
			}
		}
		return nodes;
	}
	
	public void putDataForNode(DynamoNode node, Object data) {
		synchronized (this.data) {
			// Duplicate data will be discarded
			this.data.computeIfPresent(node, (key, oldVal) ->
				oldVal.isPresent() ? oldVal : Optional.of(data));
		}
	}
	
	public void putDataForSelf(Object data) {
		synchronized (this) {
			dataFromSelf = Optional.of(data);
		}
	}
	
	protected int getState() {
		int state;
		synchronized (this) {
			state = dataFromSelf.isPresent() ? 1 : 0;
		}
		synchronized (data) {
			state += data.entrySet().stream().filter(e -> e.getValue().isPresent()).count();
		}
		return state;
	}

	public boolean isReady() {
		return getState() >= minimum;
	}
	
	public boolean isDone() {
		return getState() >= all;
	}
	
	protected abstract T doRespond();
	
	protected void respond() {
		if (!responseSent.getAndSet(true)) {
			response.resume(doRespond());
		}
	}

}
