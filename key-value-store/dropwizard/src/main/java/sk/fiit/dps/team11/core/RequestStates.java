package sk.fiit.dps.team11.core;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class RequestStates {

	private static final Logger LOGGER = LoggerFactory.getLogger(RequestStates.class);

	private final Map<UUID, RequestState<?>> states = new ConcurrentHashMap<>();

	public void put(UUID requestId, RequestState<?> state) {
		this.states.put(requestId, state);
	}
	
	public <T extends RequestState<?>> void withState(UUID requestId, Class<T> subClass,
		Consumer<? super T> ifPresent, Runnable ifNotPresent) {
		
		if (states.containsKey(requestId)) {
			
			RequestState<?> state = states.get(requestId);
			if (subClass.isInstance(state)) {
				ifPresent.accept(subClass.cast(state));
				
				if (state.isReady()) {
					state.respond();
				}
				if (state.isDone()) {
					states.remove(requestId);
				}
				if (state.isTerminated()) {
					states.remove(requestId);
					LOGGER.warn("Request {} was terminated", requestId);
				}
			}
			
		} else {
			ifNotPresent.run();
		}
		
	}
	
	public void forceRemove(UUID requestId) {
		states.remove(requestId);
	}

}
