package sk.fiit.dps.team11.models;

import java.util.Arrays;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;

import sk.fiit.dps.team11.core.Version;

public class VersionedValue {
	
	private final Version version;
	
	private final List<ByteArray> values;

	public VersionedValue(Version version, byte[] value) {
		this(version, Arrays.asList(new ByteArray(value)));
	}

	public VersionedValue(@JsonProperty("version") Version version,
		@JsonProperty("values") List<ByteArray> values) {
		
		this.version = version;
		this.values = values;
	}

	@JsonProperty
	public Version getVersion() {
		return version;
	}

	@JsonProperty
	public List<ByteArray> getValues() {
		return values;
	}

}
