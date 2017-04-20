package org.cytoscape.diffusion.internal.rest;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;

@ApiModel(value="Diffusion Status Message")
public class DiffusionStatusMessage {
	@ApiModelProperty(value = "true if successful, false if unsuccessful", required=true, example="true")
	public boolean successful = true;
}
