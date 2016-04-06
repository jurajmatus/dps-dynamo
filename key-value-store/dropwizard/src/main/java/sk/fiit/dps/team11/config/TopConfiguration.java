package sk.fiit.dps.team11.config;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.kjetland.dropwizard.activemq.ActiveMQConfig;
import com.kjetland.dropwizard.activemq.ActiveMQConfigHolder;

import io.dropwizard.Configuration;

public class TopConfiguration extends Configuration implements ActiveMQConfigHolder {

	@JsonProperty
    @NotNull
    @Valid
    private ActiveMQConfig activeMQ;
	
	public TopConfiguration() {}

    public ActiveMQConfig getActiveMQ() {
        return activeMQ;
    }
	
}
