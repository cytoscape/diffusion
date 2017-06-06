package org.cytoscape.diffusion.internal.rest;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.ci.CIErrorFactory;
import org.cytoscape.ci.CIExceptionFactory;
import org.cytoscape.ci.model.CIError;
import org.cytoscape.ci.model.CIResponse;
import org.cytoscape.ci_bridge_impl.CIProvider;
import org.cytoscape.diffusion.internal.client.DiffusionServiceException;
import org.cytoscape.diffusion.internal.task.DiffusionContextMenuTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = {"Apps", "Diffusion"})
@Path("/diffusion/v1/")
public class DiffusionResource {

	private final CyApplicationManager cyApplicationManager;
	private final SynchronousTaskManager<?> taskManager;
	
	private final CyNetworkManager cyNetworkManager;
	private final CyNetworkViewManager cyNetworkViewManager;
	
	private final DiffusionContextMenuTaskFactory diffusionTaskFactory;
	private final DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory;
	
	private final CIExceptionFactory ciExceptionFactory;
	private final CIErrorFactory ciErrorFactory;
	
	
	public static final String CY_NETWORK_NOT_FOUND_CODE = "1";
	public static final String CY_NETWORK_VIEW_NOT_FOUND_CODE = "2";
	public static final String TASK_EXECUTION_ERROR_CODE= "3";
	
	
	private static final String GENERIC_SWAGGER_NOTES = "Diffusion will send the selected network view and its selected nodes to "
			+ "a web-based REST service to calculate network propagation. Results are returned and represented by columns "
			+ "in the node table." + '\n' + '\n'
			+ "Columns are created for each execution of Diffusion and their names are returned in the response."  + '\n' + '\n';


	public DiffusionResource(final CyApplicationManager cyApplicationManager, final SynchronousTaskManager<?> taskManager, final CyNetworkManager cyNetworkManager, final CyNetworkViewManager cyNetworkViewManager, final DiffusionContextMenuTaskFactory diffusionTaskFactory, final DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory, final CIExceptionFactory ciExceptionFactory, final CIErrorFactory ciErrorFactory) {
		this.cyApplicationManager = cyApplicationManager;
		this.taskManager = taskManager;
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.diffusionTaskFactory = diffusionTaskFactory;
		this.diffusionWithOptionsTaskFactory = diffusionWithOptionsTaskFactory;
		this.ciExceptionFactory = ciExceptionFactory;
		this.ciErrorFactory = ciErrorFactory;
		
	}

	private static final Logger logger = LoggerFactory.getLogger(DiffusionResource.class);


	private final static String cyRESTErrorRoot = "urn:cytoscape:ci:diffusion-app:v1";

	private CIError buildCIError(int status, String resourcePath, String code, String message, Exception e) {
		return ciErrorFactory.getCIError(status, cyRESTErrorRoot + ":" + resourcePath+ ":"+ code, message);
	}
	
	CIResponse<Object> buildCIErrorResponse(int status, String resourcePath, String code, String message, Exception e)
	{
		CIResponse<Object> response = CIProvider.getCIResponseFactory().getCIResponse(new Object());
	
		CIError error = buildCIError(status, resourcePath, code, message, e);
		if (e != null)
		{
			logger.error(message, e);
			if (e instanceof DiffusionServiceException) {
				response.errors.addAll(((DiffusionServiceException)e).getCIErrors());
			}
		}
		else
		{
			logger.error(message);
		}
	
		response.errors.add(error);
		return response;
	}

	public CyNetwork getCyNetwork(String resourcePath, String errorType)
	{
		CyNetwork cyNetwork = cyApplicationManager.getCurrentNetwork();
		
		if (cyNetwork == null) {
			String messageString = "Could not find current Network";
			throw ciExceptionFactory.getCIException(404, new CIError[]{this.buildCIError(404, resourcePath, errorType, messageString, null)});		
		}
		return cyNetwork;
	}
		
	public CyNetworkView getCyNetworkView(String resourcePath, String errorType) {
		CyNetworkView cyNetworkView = cyApplicationManager.getCurrentNetworkView();
		if (cyNetworkView == null) {
			String messageString = "Could not find current Network View";
			throw ciExceptionFactory.getCIException(404, new CIError[]{this.buildCIError(404, resourcePath, errorType, messageString, null)});		
		}
		return cyNetworkView;
	}
	
	public CyNetworkView getCyNetworkView(String resourcePath, String errorType, long networkSUID, long networkViewSUID)
	{
		final CyNetwork network = cyNetworkManager.getNetwork(networkSUID);
		if (network == null) {
			String messageString = "Could not find network with SUID: " + networkSUID;
			throw ciExceptionFactory.getCIException(404, new CIError[]{this.buildCIError(404, resourcePath, errorType, messageString, null)});		
		}
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		if (views.isEmpty()) {
			String messageString = "No views are available for network with SUID: " + networkSUID;
			throw ciExceptionFactory.getCIException(404, new CIError[]{this.buildCIError(404, resourcePath, errorType, messageString, null)});		
		}
		for (final CyNetworkView view : views) {
			final Long vid = view.getSUID();
			if (vid.equals(networkViewSUID)) {
				return view;
			}
		}
		String messageString = "Could not find network view with SUID: " + networkViewSUID + " for network with SUID: " + networkSUID;
		throw ciExceptionFactory.getCIException(404, new CIError[]{this.buildCIError(404, resourcePath, errorType, messageString, null)});		
	}
	
	@ApiModel(value="Diffusion App Response", description="Diffusion Analysis Results in CI Format", parent=CIResponse.class)
	public static class DiffusionAppResponse extends CIResponse<DiffusionResultColumns>{
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("currentView/diffuse_with_options")
	@ApiOperation(value = "Execute Diffusion Analysis on Current View with Options",
	notes = GENERIC_SWAGGER_NOTES,
	response = DiffusionAppResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network or Network View does not exist", response = CIResponse.class)
	})
	public Response diffuseWithOptions(@ApiParam(value = "Diffusion Parameters", required = true) DiffusionParameters diffusionParameters)
	{
		CyNetwork cyNetwork = getCyNetwork("diffuse_current_view_with_options", CY_NETWORK_NOT_FOUND_CODE);
		CyNetworkView cyNetworkView = getCyNetworkView("diffuse_current_view_with_options", CY_NETWORK_VIEW_NOT_FOUND_CODE);
		
		return diffuseWithOptions(cyNetwork.getSUID(), cyNetworkView.getSUID(), diffusionParameters);
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("{networkSUID}/views/{networkViewSUID}/diffuse_with_options")
	@ApiOperation(value = "Execute Diffusion Analysis on a Specific Network View with Options",
	notes = GENERIC_SWAGGER_NOTES,
	response = DiffusionAppResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network does not exist", response = CIResponse.class)
	})

	public Response diffuseWithOptions(@ApiParam(value="Network SUID (see GET /v1/networks)") @PathParam("networkSUID") long networkSUID, @ApiParam(value="Network View SUID (see GET /v1/networks/{networkId}/views)") @PathParam("networkViewSUID") long networkViewSUID, @ApiParam(value = "Diffusion Parameters", required = true) DiffusionParameters diffusionParameters) {

		System.out.println("Accessing Diffusion with options via REST");
		CyNetworkView cyNetworkView = getCyNetworkView("diffuse_with_options", CY_NETWORK_VIEW_NOT_FOUND_CODE, networkSUID, networkViewSUID);
		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver(this, "diffuse_with_options", TASK_EXECUTION_ERROR_CODE);
		
		Map<String, Object> tunableMap = new HashMap<String, Object>();
		
		//This next section is VERY interesting. Since we're accessing DiffusionWithOptionsTaskFactory without the
		//benefit of interceptors or CommandExecutor, we have the option of literally building tunables from scratch.
		ListSingleSelection<String> heatColumnName = new ListSingleSelection<String>();
		List<String> heatColumns = new ArrayList<String>();
		heatColumns.add(diffusionParameters.heatColumnName);
		heatColumnName.setPossibleValues(heatColumns);
		heatColumnName.setSelectedValue(diffusionParameters.heatColumnName);
		
		tunableMap.put("heatColumnName", heatColumnName);
		tunableMap.put("time", diffusionParameters.time);
		TaskIterator taskIterator = diffusionWithOptionsTaskFactory.createTaskIterator(cyNetworkView);
		taskManager.setExecutionContext(tunableMap);
		taskManager.execute(taskIterator, taskObserver);
		return Response.status(taskObserver.response.errors.size() == 0 ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.entity(taskObserver.response).build();
	}


	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("currentView/diffuse")
	@ApiOperation(value = "Execute Diffusion Analysis on Current View",
	notes = GENERIC_SWAGGER_NOTES,
	response = DiffusionAppResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network or Network View does not exist", response = CIResponse.class)
	})
	public Response diffuse()
	{
		CyNetwork cyNetwork = getCyNetwork("diffuse_current_view", CY_NETWORK_NOT_FOUND_CODE);
		CyNetworkView cyNetworkView = getCyNetworkView("diffuse_current_view", CY_NETWORK_VIEW_NOT_FOUND_CODE);
		
		return diffuse(cyNetwork.getSUID(), cyNetworkView.getSUID());
	}
	
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("{networkSUID}/views/{networkViewSUID}/diffuse")
	@ApiOperation(value = "Execute Diffusion Analysis on a Specific Network View",
	notes = GENERIC_SWAGGER_NOTES 
			+ "The nodes you would like to use as input should be selected. This will be used to "
			+ "generate the contents of the **diffusion\\_input** column, which represents the query vector and corresponds to h in the diffusion equation."  + '\n' + '\n',
			response = DiffusionAppResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network does not exist", response = CIResponse.class),
	})

	public Response diffuse(@ApiParam(value="Network SUID (see GET /v1/networks)") @PathParam("networkSUID") long networkSUID, @ApiParam(value="Network View SUID (see GET /v1/networks/{networkId}/views)") @PathParam("networkViewSUID") long networkViewSUID) {

		System.out.println("Accessing Diffusion via REST");
		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver(this, "diffuse", TASK_EXECUTION_ERROR_CODE);
	
		executeDiffuse(networkSUID, networkViewSUID, taskObserver);
		
		return Response.status(taskObserver.response.errors.size() == 0 ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.entity(taskObserver.response).build();

	}
	
	public void executeDiffuse(long networkSUID, long networkViewSUID, TaskObserver taskObserver) {
		CyNetworkView cyNetworkView = getCyNetworkView("diffuse", CY_NETWORK_VIEW_NOT_FOUND_CODE, networkSUID, networkViewSUID);
		TaskIterator taskIterator = diffusionTaskFactory.createTaskIterator(cyNetworkView);
		taskManager.execute(taskIterator, taskObserver);
	
	}
}
