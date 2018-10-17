package org.cytoscape.diffusion.internal;

import io.swagger.annotations.ApiModelProperty;

public class DiffusionDocumentation {
	public static final String GENERIC_SWAGGER_NOTES = "Diffusion will send the selected network view and its selected nodes to "
			+ "a web-based REST service to calculate network propagation. Results are returned and represented by columns "
			+ "in the node table." + '\n' + '\n'
			+ "Columns are created for each execution of Diffusion and their names are returned in the response."  + '\n' + '\n';
	
	public static final String ADDITIONAL_SELECTION_SWAGGER_NOTES =  "The nodes you would like to use as input should be selected. This will be used to "
			+ "generate the contents of the **diffusion\\_input** column, which represents the query vector and corresponds to h in the diffusion equation."  + '\n' + '\n';

	public static final String HEAT_COLUMN_NAME_LONG_DESCRIPTION =  "A node column name intended to override the default table column 'diffusion_input'. This represents the query vector and corresponds to h in the diffusion equation.";
			
	public static final String TIME_LONG_DESCRIPTION = "The extent of spread over the network. This corresponds to t in the diffusion equation.";

	public static final String COMMAND_EXAMPLE_JSON = "{\n" + 
			"    \"heatColumn\": \"diffusion_output_heat\",\n" + 
			"    \"rankColumn\": \"diffusion_output_rank\"\n" + 
			"}";
}
