package sk.fiit.dps.team11.providers;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.model.Parameter;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;

abstract public class SimpleValueFactoryProvider implements ValueFactoryProvider {

	private final ServiceLocator locator;

	protected SimpleValueFactoryProvider(ServiceLocator locator) {
		this.locator = locator;
	}

	protected abstract Factory<?> createValueFactory(Parameter parameter);

	@Override
	public final Factory<?> getValueFactory(Parameter parameter) {		
		final Factory<?> valueFactory = createValueFactory(parameter);
		if (valueFactory == null) {
			return null;
		}
		
		locator.inject(valueFactory);
		return valueFactory;
	}

	@Override
	public PriorityType getPriority() {
		return Priority.NORMAL;
	}

	protected final ServiceLocator getLocator() {
		return locator;
	}

}
