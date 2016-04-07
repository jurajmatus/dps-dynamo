package sk.fiit.dps.team11;

import javax.inject.Singleton;

import org.apache.activemq.broker.BrokerService;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codahale.metrics.MetricRegistry;
import com.codahale.metrics.health.HealthCheck;
import com.kjetland.dropwizard.activemq.ActiveMQBundle;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import sk.fiit.dps.team11.annotations.MQSender;
import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.providers.ActiveMQSenderFactoryProvider;
import sk.fiit.dps.team11.providers.DatabaseAdapter;
import sk.fiit.dps.team11.providers.RuntimeExceptionMapper;
import sk.fiit.dps.team11.resources.CheckConnectivityResource;
import sk.fiit.dps.team11.resources.SampleResource;
import sk.fiit.dps.team11.workers.SampleWorker;


public class KeyValueStoreService extends Application<TopConfiguration> {

	private static final Logger LOGGER = LoggerFactory.getLogger(KeyValueStoreService.class);

    private ActiveMQBundle activeMQBundle;
	
	public static void main(String[] args) throws Exception {
		new KeyValueStoreService().run(args);
	}

	@Override
	public void initialize(Bootstrap<TopConfiguration> bootstrap) {
		bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
				bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
		
		BrokerService brokerService = new BrokerService();
		try {
			brokerService.addConnector("tcp://localhost:61616");
			brokerService.setBrokerName("local-mq");
			brokerService.setPersistent(false);
			brokerService.start();
		} catch (Exception e) {
			LOGGER.error("Couldn't initialize Message queue");
			System.exit(1);
		}
		
		this.activeMQBundle = new ActiveMQBundle();
		bootstrap.addBundle(activeMQBundle);
	}

	@Override
	public void run(final TopConfiguration configuration, Environment environment) {

		environment.healthChecks().register("base-app", new HealthCheck() {
			@Override
			protected Result check() throws Exception {
				return Result.healthy();
			}
		});
		
		MetricRegistry metrics = environment.metrics();
		
		DatabaseAdapter db = new DatabaseAdapter();
		environment.lifecycle().manage(db);

		// Injections
		environment.jersey().register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(configuration).to(TopConfiguration.class);
				bind(activeMQBundle).to(ActiveMQBundle.class);
				bind(metrics).to(MetricRegistry.class);
				bind(db).to(DatabaseAdapter.class);
				
				bind(ActiveMQSenderFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
				bind(ActiveMQSenderFactoryProvider.InjectionResolver.class)
					.to(new TypeLiteral<InjectionResolver<MQSender>>() {}).in(Singleton.class);
			}
		});

		// Providers, handlers, mappers
		environment.jersey().register(RuntimeExceptionMapper.class);

		// Resources
		environment.jersey().register(SampleResource.class);
		environment.jersey().register(CheckConnectivityResource.class);
		
		// Queue workers
		activeMQBundle.registerReceiver("sample", new SampleWorker(), String.class, true);

	}

}
