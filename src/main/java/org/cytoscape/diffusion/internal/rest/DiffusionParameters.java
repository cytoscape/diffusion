package org.cytoscape.diffusion.internal.rest;

import org.cytoscape.diffusion.internal.task.DiffuseSelectedTask;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

/**
 * Parameters for passing to DiffuseSelectedWithOptionsTask
 * 
 * @author davidotasek
 *
 */
@ApiModel(value="Diffusion Parameters", description="Parameters for Diffusion analysis")
public class DiffusionParameters {
	@ApiModelProperty(value = "A node column name intended to override the default table column 'diffusion_input'. This represents the query vector and corresponds to h in the diffusion equation.", example=DiffuseSelectedTask.DIFFUSION_INPUT_COL_NAME)
	public String heatColumnName;
	@ApiModelProperty(value = "Time", example="0.1")
	public Double time;
}
