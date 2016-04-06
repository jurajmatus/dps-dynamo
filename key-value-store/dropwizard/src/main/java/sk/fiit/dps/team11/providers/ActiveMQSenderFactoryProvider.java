package sk.fiit.dps.team11.providers;

import javax.inject.Inject;
import javax.inject.Singleton;

import org.glassfish.hk2.api.Factory;
import org.glassfish.hk2.api.ServiceLocator;
import org.glassfish.jersey.server.internal.inject.AbstractContainerRequestValueFactory;
import org.glassfish.jersey.server.internal.inject.ParamInjectionResolver;

import com.kjetland.dropwizard.activemq.ActiveMQBundle;

import sk.fiit.dps.team11.annotations.MQSender;


@Singleton
final public class ActiveMQSenderFactoryProvider extends AnnotationReadingValueFactoryProvider<MQSender> {

	@Singleton
	public static final class InjectionResolver extends ParamInjectionResolver<MQSender> {
		public InjectionResolver() {
			super(ActiveMQSenderFactoryProvider.class);
		}
	}

	private static final class ActiveMQSenderFactory extends AbstractContainerRequestValueFactory<Object> {

		private final ActiveMQBundle bundle;
		
		private final MQSender info;

		public ActiveMQSenderFactory(ActiveMQBundle bundle, MQSender info) {
			this.bundle = bundle;
			this.info = info;
		}

		@Override
		public Object provide() {
			return bundle.createSender(info.topic(), false);
		}
	}

	private final ActiveMQBundle bundle;

	@Inject
	public ActiveMQSenderFactoryProvider(ServiceLocator locator, ActiveMQBundle bundle) {
		super(locator, MQSender.class);
		this.bundle = bundle;
	}

	@Override
	protected Factory<?> createValueFactory(MQSender info) {
		return new ActiveMQSenderFactory(bundle, info);
	}
}
