package sk.fiit.dps.team11.core;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

public class RequestStates {

	private final Map<UUID, RequestState<?>> states = new ConcurrentHashMap<>();

	public void put(UUID requestId, RequestState<?> state) {
		this.states.put(requestId, state);
	}
	
	public <T extends RequestState<?>, U> U withState(UUID requestId, Class<T> subClass,
		Function<T, U> withPresent, Supplier<U> withNonPresent) {
		
		if (states.containsKey(requestId)) {
			
			RequestState<?> state = states.get(requestId);
			if (subClass.isInstance(state)) {
				U ret = withPresent.apply(subClass.cast(state));
				
				if (state.isReady()) {
					state.respond();
				}
				if (state.isDone()) {
					states.remove(requestId);
				}
				
				return ret;
			}
			
		}
		return withNonPresent.get();
		
	}

}
