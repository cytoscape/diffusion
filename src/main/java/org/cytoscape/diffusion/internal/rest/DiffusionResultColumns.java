package org.cytoscape.diffusion.internal.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Diffusion Result Columns")
public class DiffusionResultColumns {
	
	@ApiModelProperty(value = "The node column containing the result of the diffusion process, corresponding to d in the diffusion equation.", required=true, example="diffusion_output_heat")
	public String heatColumn;
	@ApiModelProperty(value = "The node column containing rank according to output heat. This column is recommended for analysis, as it is very robust to parameter choice.", required=true, example="diffusion_output_rank")
	public String rankColumn;
	
	public DiffusionResultColumns() {};
	
	public DiffusionResultColumns(String heatColumn, String rankColumn) {
		this.heatColumn = heatColumn;
		this.rankColumn = rankColumn;
	}
}
