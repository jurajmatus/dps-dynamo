package sk.fiit.dps.team11.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReliabilityConfiguration {

	private int numReplicas = 3;
	
	private int responseTimeoutMillis = 500;

	@JsonProperty
	public int getNumReplicas() {
		return numReplicas;
	}

	@JsonProperty
	public void setNumReplicas(int numReplicas) {
		this.numReplicas = numReplicas;
	}

	@JsonProperty
	public int getResponseTimeoutMillis() {
		return responseTimeoutMillis;
	}

	@JsonProperty
	public void setResponseTimeoutMillis(int responseTimeoutMillis) {
		this.responseTimeoutMillis = responseTimeoutMillis;
	}
	
}
