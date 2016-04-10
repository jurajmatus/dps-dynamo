package sk.fiit.dps.team11.providers;

import java.util.Queue;

import org.eclipse.jetty.util.ArrayQueue;
import org.glassfish.hk2.api.ServiceLocator;

public class InjectManager {
	
	private final Queue<Object> toInject = new ArrayQueue<>();
	
	private ServiceLocator locator;
	
	public InjectManager() {}
	
	public <T> T register(T toInject) {
		this.toInject.add(toInject);
		tryToCreate();
		return toInject;
	}
	
	private void tryToCreate() {
		if (locator != null) {
			while (!toInject.isEmpty()) {
				Object toInject = this.toInject.poll();
				locator.inject(toInject);
			}
		}
	}

	public void injectLocator(ServiceLocator locator) {
		this.locator = locator;
		tryToCreate();
	}

}
