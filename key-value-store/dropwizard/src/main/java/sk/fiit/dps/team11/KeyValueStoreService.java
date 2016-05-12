package sk.fiit.dps.team11;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.stream.Stream;

import javax.inject.Singleton;

import org.apache.activemq.broker.BrokerService;
import org.glassfish.hk2.api.InjectionResolver;
import org.glassfish.hk2.api.TypeLiteral;
import org.glassfish.hk2.utilities.binding.AbstractBinder;
import org.glassfish.jersey.server.spi.internal.ValueFactoryProvider;
import org.glassfish.jersey.servlet.ServletContainer;
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
import sk.fiit.dps.team11.core.DatabaseAdapter;
import sk.fiit.dps.team11.core.MQ;
import sk.fiit.dps.team11.core.RequestStates;
import sk.fiit.dps.team11.core.Topology;
import sk.fiit.dps.team11.core.VersionResolution;
import sk.fiit.dps.team11.providers.ActiveMQSenderFactoryProvider;
import sk.fiit.dps.team11.providers.InjectManager;
import sk.fiit.dps.team11.providers.RuntimeExceptionMapper;
import sk.fiit.dps.team11.resources.CheckConnectivityResource;
import sk.fiit.dps.team11.resources.StorageResource;
import sk.fiit.dps.team11.workers.DataManipulationWorker;
import sk.fiit.dps.team11.workers.GlobalMetricsWorker;
import sk.fiit.dps.team11.workers.ReplicaFinderWorker;
import sk.fiit.dps.team11.workers.ReplicationWorker;
import sk.fiit.dps.team11.workers.TimeoutCheckWorker;
import sk.fiit.dps.team11.workers.WrappedMethodWorker;


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
			String localIp = InetAddress.getLocalHost().getHostAddress();
			System.out.printf("Host IP address is '%s'\n.", localIp);
			brokerService.addConnector(String.format("tcp://%s:61616", localIp));
			brokerService.setBrokerName("local-mq");
			brokerService.setPersistent(false);
			brokerService.start();
		} catch (Exception e) {
			LOGGER.error("Couldn't initialize Message queue");
			e.printStackTrace();
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
		
		InjectManager injectManager = new InjectManager();
		environment.lifecycle().addServerLifecycleListener(server -> {
			injectManager.injectLocator(((ServletContainer) environment.getJerseyServletContainer())
											.getApplicationHandler().getServiceLocator());
		});
		
		// Core objects		
		DatabaseAdapter db = new DatabaseAdapter();
		environment.lifecycle().manage(db);
		
		ScheduledExecutorService execService =
			environment.lifecycle().scheduledExecutorService("sch-thread-pool-%d")
				.threads(configuration.getParallelism().getNumScheduledThreads())
				.build();
		
		injectManager.register(new GlobalMetricsWorker());
		
		// Injections
		environment.jersey().register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(configuration).to(TopConfiguration.class);
				bind(activeMQBundle).to(ActiveMQBundle.class);
				bind(metrics).to(MetricRegistry.class);
				bind(db).to(DatabaseAdapter.class);
				bind(MQ.class).to(MQ.class).in(Singleton.class);
				bind(RequestStates.class).to(RequestStates.class).in(Singleton.class);
				bind(execService).to(ScheduledExecutorService.class);
				bind(Topology.class).to(Topology.class).in(Singleton.class);
				bind(injectManager).to(InjectManager.class);
				bind(VersionResolution.class).to(VersionResolution.class).in(Singleton.class);
				
				bind(ActiveMQSenderFactoryProvider.class).to(ValueFactoryProvider.class).in(Singleton.class);
				bind(ActiveMQSenderFactoryProvider.InjectionResolver.class)
					.to(new TypeLiteral<InjectionResolver<MQSender>>() {}).in(Singleton.class);
			}
		});
		
		// Providers, handlers, mappers
		environment.jersey().register(RuntimeExceptionMapper.class);

		// Resources
		environment.jersey().register(StorageResource.class);
		environment.jersey().register(CheckConnectivityResource.class);
		
		// Queue workers
		Stream<Object> workers = Stream.of(new DataManipulationWorker(),
			new ReplicaFinderWorker(), new ReplicationWorker(), new TimeoutCheckWorker());
		
		workers
			.map(worker -> injectManager.register(worker))
			.flatMap(worker -> WrappedMethodWorker.scan(worker).stream())
			.forEach(wrappedWorker -> wrappedWorker.register(activeMQBundle));

	}

}
