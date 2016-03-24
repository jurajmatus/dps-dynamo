package sk.fiit.dps.team11;

import org.glassfish.hk2.utilities.binding.AbstractBinder;

import com.codahale.metrics.health.HealthCheck;

import io.dropwizard.Application;
import io.dropwizard.configuration.EnvironmentVariableSubstitutor;
import io.dropwizard.configuration.SubstitutingSourceProvider;
import io.dropwizard.setup.Bootstrap;
import io.dropwizard.setup.Environment;
import sk.fiit.dps.team11.config.TopConfiguration;
import sk.fiit.dps.team11.providers.RuntimeExceptionMapper;
import sk.fiit.dps.team11.resources.SampleResource;

public class KeyValueStoreService extends Application<TopConfiguration> {

	public static void main(String[] args) throws Exception {
		new KeyValueStoreService().run(args);
	}

	@Override
	public void initialize(Bootstrap<TopConfiguration> bootstrap) {
		bootstrap.setConfigurationSourceProvider(new SubstitutingSourceProvider(
				bootstrap.getConfigurationSourceProvider(), new EnvironmentVariableSubstitutor(false)));
	}

	@Override
	public void run(final TopConfiguration configuration, Environment environment) {

		environment.healthChecks().register("empty", new HealthCheck() {
			@Override
			protected Result check() throws Exception {
				return Result.healthy();
			}
		});

		// Injections
		environment.jersey().register(new AbstractBinder() {
			@Override
			protected void configure() {
				bind(configuration).to(TopConfiguration.class);
			}
		});

		// Providers, handlers, mappers
		environment.jersey().register(RuntimeExceptionMapper.class);

		// Resources
		environment.jersey().register(SampleResource.class);

	}

}
