package sk.fiit.dps.team11.providers;

import java.util.Queue;
import java.util.function.Consumer;

import org.eclipse.jetty.util.ArrayQueue;
import org.glassfish.hk2.api.ServiceLocator;
import org.javatuples.Pair;

public class InjectManager {
	
	private final Queue<Pair<Class<?>, Consumer<?>>> toInject = new ArrayQueue<>();
	
	private ServiceLocator locator;
	
	public InjectManager() {}
	
	public <T> void register(Class<T> classToCreate, Consumer<T> withInstance) {
		toInject.add(new Pair<>(classToCreate, withInstance));
		tryToCreate();
	}
	
	@SuppressWarnings("unchecked")
	private void tryToCreate() {
		if (locator != null) {
			while (!toInject.isEmpty()) {
				Pair<Class<?>, Consumer<?>> creation = toInject.poll();
				Object instance = locator.createAndInitialize(creation.getValue0());
				((Consumer<Object>) creation.getValue1()).accept(instance);
			}
		}
	}

	public void injectLocator(ServiceLocator locator) {
		this.locator = locator;
		tryToCreate();
	}

}
