package sk.fiit.dps.team11.core;

import com.fasterxml.jackson.annotation.JsonProperty;

public class DynamoNode implements Comparable<DynamoNode> {

	private final String ip;
	
	private final long position;

	public DynamoNode(@JsonProperty("ip") String ip, @JsonProperty("position") long position) {
		this.ip = ip;
		this.position = position;
	}

	@JsonProperty
	public String getIp() {
		return ip;
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
	// Generated
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((ip == null) ? 0 : ip.hashCode());
		result = prime * result + (int) (position ^ (position >>> 32));
		return result;
	}

	@Override
	// Generated
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		DynamoNode other = (DynamoNode) obj;
		if (ip == null) {
			if (other.ip != null)
				return false;
		} else if (!ip.equals(other.ip))
			return false;
		if (position != other.position)
			return false;
		return true;
	}

	@Override
	public int compareTo(DynamoNode o) {
		return Long.compare(position, o.position);
	}
	
}
