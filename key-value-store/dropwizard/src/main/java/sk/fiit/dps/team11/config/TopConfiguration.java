package sk.fiit.dps.team11.config;

import java.util.concurrent.atomic.AtomicInteger;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.dropwizard.activemq.ActiveMQConfig;
import com.kjetland.dropwizard.activemq.ActiveMQConfigHolder;

import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.DefaultServerFactory;

public class TopConfiguration extends Configuration implements ActiveMQConfigHolder {

	@JsonProperty
    @NotNull
    @Valid
    private ActiveMQConfig activeMQ;
	
	@JsonProperty
    private ParallelismConfiguration parallelism;
	
	@JsonProperty
    private ReliabilityConfiguration reliability;
	
	public TopConfiguration() {}

    public ActiveMQConfig getActiveMQ() {
        return activeMQ;
    }

	public ParallelismConfiguration getParallelism() {
		return parallelism;
	}

	public ReliabilityConfiguration getReliability() {
		return reliability;
	}
	
	public int getPort() {
		AtomicInteger httpPort = new AtomicInteger(8080);
		try {
			DefaultServerFactory serverFactory = (DefaultServerFactory) this.getServerFactory();
			serverFactory.getApplicationConnectors().stream()
				.filter(c -> c instanceof HttpConnectorFactory)
				.findFirst()
				.ifPresent(_connector -> {
					HttpConnectorFactory connector = (HttpConnectorFactory) _connector;
				    httpPort.set(connector.getPort());
				});
		} catch (Exception e) {}
		return httpPort.get();
	}
	
}
