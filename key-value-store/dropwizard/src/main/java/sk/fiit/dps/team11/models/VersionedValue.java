package sk.fiit.dps.team11.models;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import sk.fiit.dps.team11.core.Version;

public class VersionedValue {
	
	private final static ObjectMapper MAPPER = new ObjectMapper();
	
	private final Version version;
	
	private final List<ByteArray> values;

	public VersionedValue(Version version, byte[] value) {
		this(version, Arrays.asList(new ByteArray(value)));
	}

	public VersionedValue(@JsonProperty("version") Version version,
		@JsonProperty("values") List<ByteArray> values) {
		
		this.version = version;
		this.values = isEmpty(values) ? Collections.emptyList() : values;
	}
	
	private boolean isEmpty(List<ByteArray> values) {
		return values.isEmpty() || values.stream().allMatch(byteArray -> byteArray.data.length == 0);
	}

	@JsonProperty
	public Version getVersion() {
		return version;
	}

	@JsonProperty
	public List<ByteArray> getValues() {
		return values;
	}
	
	@Override
	public String toString() {
		try {
			return MAPPER.writeValueAsString(this);
		} catch (JsonProcessingException e) {
			return "";
		}
	}

}
