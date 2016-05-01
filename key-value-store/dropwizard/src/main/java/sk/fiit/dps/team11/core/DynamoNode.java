package sk.fiit.dps.team11.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DynamoNode implements Comparable<DynamoNode> {

	private final String ip;

	public DynamoNode(@JsonProperty("ip") String ip) {
		this.ip = ip;
	}

	@JsonProperty
	public String getIp() {
		return ip;
	}
	
	@Override
	public String toString() {
		return String.format("DynamoNode [%s]", ip);
	}

	@Override
	public int compareTo(DynamoNode o) {
		return this.ip.compareTo(o.ip);
	}
	
}
