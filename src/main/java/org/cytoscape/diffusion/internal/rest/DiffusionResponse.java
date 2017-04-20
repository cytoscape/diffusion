package org.cytoscape.diffusion.internal.rest;

import java.util.List;

import org.cytoscape.diffusion.internal.client.CIError;


import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * 
 * Response from Diffusion service
 * 
 */
@ApiModel(value="Diffusion Response", description="Diffusion Analysis Results in CI Format")
public class DiffusionResponse<T> {

	public T data;
	@ApiModelProperty(value = "An Array of Errors, if any, produced while processing the Diffusion request", required=true)
	public List<CIError> errors;
}
