package sk.fiit.dps.team11.core;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.annotation.JsonProperty;

import sk.fiit.dps.team11.resources.StorageExecutor;

public class DynamoNode implements Comparable<DynamoNode> {

	private static final Logger LOGGER = LoggerFactory.getLogger(StorageExecutor.class);

	private final String ip;
	
	/**
	 * Position in chord
	 * All keys with hashes higher than this will belong to this node
	 */
	private Long position;

	public DynamoNode(@JsonProperty("ip") String ip, @JsonProperty("position") Long position) {
		this.ip = ip;
		this.position = position;
	}

	@JsonProperty
	public String getIp() {
		return ip;
	}
	
	public void setPosition(long position) {
		this.position = position;
	}

	@JsonProperty
	public long getPosition() {
		return position;
	}

	@Override
	public String toString() {
		return String.format("DynamoNode [%s] (pos: %d)", ip, position);
	}

	@Override
	public int hashCode() {
		return ip.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		DynamoNode dn = (DynamoNode) obj;
		if (this == obj) {
			return true;
		}
		if (obj == null) {
			return false;
		}
		if (!(obj instanceof DynamoNode)) {
			return false;
		}
		
		if ( (this.ip.compareToIgnoreCase(dn.ip) == 0) && (this.position.equals(dn.position)) )
			return true;
		else
			return false;
		
	}

	@Override
	public int compareTo(DynamoNode o) {
		return Long.compare(position, o.position);
	}
	
}
