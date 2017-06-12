package org.cytoscape.diffusion.internal.client;

import java.util.Collections;
import java.util.List;

import org.cytoscape.ci.model.CIError;

public class DiffusionServiceException extends Exception {
	
	public final List<CIError> ciErrors;
	
	public DiffusionServiceException(String message, List<CIError> ciErrors) {
		super(message);
		this.ciErrors = Collections.unmodifiableList(ciErrors);
	}
	
	public List<CIError> getCIErrors() {
		return ciErrors;
	}
}
