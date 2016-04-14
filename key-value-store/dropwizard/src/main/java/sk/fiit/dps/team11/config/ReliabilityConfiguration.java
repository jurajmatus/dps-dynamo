package sk.fiit.dps.team11.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ReliabilityConfiguration {

	private int numReplicas = 3;

	@JsonProperty
	public int getNumReplicas() {
		return numReplicas;
	}

	@JsonProperty
	public void setNumReplicas(int numReplicas) {
		this.numReplicas = numReplicas;
	}
	
}
