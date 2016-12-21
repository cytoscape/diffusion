package org.cytoscape.diffusion.internal.client;

import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

public class DiffusionResponse {

	private final Map<String, NodeAttributes> data;
	private final List<Object> errors;

	@JsonCreator
	public DiffusionResponse(@JsonProperty("data") final Map<String, NodeAttributes> data,
			@JsonProperty("errors") final List<Object> errors) {
		this.data = data;
		this.errors = errors;
	}

	public Map<String, NodeAttributes> getData() {
		return data;
	}

	public List<Object> getErrors() {
		return errors;
	}

}