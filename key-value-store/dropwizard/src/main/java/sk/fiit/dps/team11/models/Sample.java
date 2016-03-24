package sk.fiit.dps.team11.models;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Sample {

	private final String text;
	
	@JsonCreator
	public Sample(@JsonProperty("text") String text) {
		this.text = text;
	}

	@JsonProperty
	public String getText() {
		return text;
	}
	
}
