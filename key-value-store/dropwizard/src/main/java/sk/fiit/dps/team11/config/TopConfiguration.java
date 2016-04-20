package sk.fiit.dps.team11.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.dropwizard.activemq.ActiveMQConfig;
import com.kjetland.dropwizard.activemq.ActiveMQConfigHolder;

import io.dropwizard.Configuration;
import io.dropwizard.jetty.HttpConnectorFactory;
import io.dropwizard.server.SimpleServerFactory;

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
		int httpPort = 0;
		SimpleServerFactory serverFactory = (SimpleServerFactory) this.getServerFactory();
		HttpConnectorFactory connector = (HttpConnectorFactory) serverFactory.getConnector();
		if (connector.getClass().isAssignableFrom(HttpConnectorFactory.class)) {
		    httpPort = connector.getPort();
		}
		return httpPort;
	}
	
}
