package sk.fiit.dps.team11.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReliabilityConfiguration {

	private int numReplicas = 2;
	
	private int responseTimeoutMillis = 3000;
	
	private int nodeResponseTimeoutMillis = 2000;
	
	private int topologyChangeTimeoutMillis = 1500;

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

	@JsonProperty
	public int getNodeResponseTimeoutMillis() {
		return nodeResponseTimeoutMillis;
	}

	@JsonProperty
	public void setNodeResponseTimeoutMillis(int nodeResponseTimeoutMillis) {
		this.nodeResponseTimeoutMillis = nodeResponseTimeoutMillis;
	}

	public int getTopologyChangeTimeoutMillis() {
		return topologyChangeTimeoutMillis;
	}

	public void setTopologyChangeTimeoutMillis(int topologyChangeTimeoutMillis) {
		this.topologyChangeTimeoutMillis = topologyChangeTimeoutMillis;
	}
	
}
