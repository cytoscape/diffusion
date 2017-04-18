package org.cytoscape.diffusion.internal.rest;

import java.util.List;

import org.cytoscape.diffusion.internal.client.CIError;

/**
 * 
 * Response from Diffusion service
 * 
 */
public class DiffusionResponse {

	public StatusMessage data;
	public List<CIError> errors;
}
