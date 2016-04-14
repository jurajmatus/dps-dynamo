package sk.fiit.dps.team11.config;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ParallelismConfiguration {

	private int numScheduledThreads = 4;

	@JsonProperty
	public int getNumScheduledThreads() {
		return numScheduledThreads;
	}

	@JsonProperty
	public void setNumScheduledThreads(int numScheduledThreads) {
		this.numScheduledThreads = numScheduledThreads;
	}
	
}
