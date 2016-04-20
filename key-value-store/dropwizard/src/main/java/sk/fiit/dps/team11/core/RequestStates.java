package sk.fiit.dps.team11.core;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Consumer;

public class RequestStates {

	private final Map<UUID, RequestState<?>> states = new ConcurrentHashMap<>();

	public void put(UUID requestId, RequestState<?> state) {
		this.states.put(requestId, state);
	}
	
	public <T extends RequestState<?>> void withState(UUID requestId, Class<T> subClass,
		Consumer<T> ifPresent, Runnable ifNotPresent) {
		
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
			}
			
		} else {
			ifNotPresent.run();
		}
		
	}

}
