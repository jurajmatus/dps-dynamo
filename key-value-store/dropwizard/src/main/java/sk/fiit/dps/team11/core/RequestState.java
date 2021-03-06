package sk.fiit.dps.team11.core;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Optional;
import java.util.TreeMap;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.ws.rs.container.AsyncResponse;

import sk.fiit.dps.team11.models.BaseRequest;


abstract public class RequestState<T extends BaseRequest> {
	
	private final UUID requestId;
	
	private final AsyncResponse response;
	
	private final AtomicBoolean responseSent = new AtomicBoolean(false);
	
	private final int all;
	
	private final T request;
	
	private Optional<Object> dataFromSelf = Optional.empty();
	
	private final Map<DynamoNode, Optional<Object>> data = new TreeMap<>();

	public RequestState(AsyncResponse response, int all, T request) {
		this.requestId = UUID.randomUUID();
		this.response = response;
		this.all = all;
		this.request = request;
	}
	
	public UUID getRequestId() {
		return requestId;
	}

	public T getRequest() {
		return request;
	}

	public void addNodes(Collection<DynamoNode> nodes) {
		synchronized (this.data) {
			for (DynamoNode node : nodes) {
				this.data.put(node, Optional.empty());
			}
		}
	}

	public void addNodes(DynamoNode... nodes) {
		synchronized (this.data) {
			for (DynamoNode node : nodes) {
				this.data.put(node, Optional.empty());
			}
		}
	}
	
	private List<DynamoNode> getTimeoutedNodes(boolean remove) {
		List<DynamoNode> nodes = new ArrayList<>();
		synchronized (this.data) {
			for (Entry<DynamoNode, Optional<Object>> entry : this.data.entrySet()) {
				if (!entry.getValue().isPresent()) {
					nodes.add(entry.getKey());
					if (remove) {
						this.data.remove(entry.getKey());
					}
				}
			}
		}
		return nodes;
	}
	
	public List<DynamoNode> getAndRemoveTimeoutedNodes() {
		return getTimeoutedNodes(true);
	}
	
	public List<DynamoNode> getNodesWithoutResponse() {
		return getTimeoutedNodes(false);
	}
	
	protected boolean putForNode(DynamoNode node, Object data) {
		synchronized (this.data) {
			// Duplicate data will be discarded
			if (this.data.containsKey(node) && this.data.get(node).isPresent()) {
				return false;
			}
			this.data.put(node, Optional.of(data));
			return true;
		}
	}
	
	protected boolean putForSelf(Object data) {
		synchronized (this) {
			if (dataFromSelf.isPresent()) {
				return false;
			}
			dataFromSelf = Optional.of(data);
			return true;
		}
	}
	
	protected <D> List<D> getData(Class<D> as) {
		List<D> data = new ArrayList<>();
		
		synchronized (this) {
			if (dataFromSelf.filter(as::isInstance).isPresent()) {
				data.add(as.cast(dataFromSelf.get()));
			}
		}
		synchronized (this.data) {
			for (Entry<DynamoNode, Optional<Object>> entry : this.data.entrySet()) {
				if (entry.getValue().filter(as::isInstance).isPresent()) {
					data.add(as.cast(entry.getValue().get()));
				}
			}
		}
		
		return data;
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
	
	abstract protected int minimum();

	public boolean isReady() {
		return getState() >= Math.min(all, minimum());
	}
	
	public boolean isDone() {
		return getState() >= all;
	}
	
	protected abstract Object provideResponse();
	
	protected void respond() {
		if (!responseSent.getAndSet(true)) {
			response.resume(provideResponse());
		}
	}

}
