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
import org.cytoscape.diffusion.internal.client.CIError;

import org.cytoscape.diffusion.internal.client.CIResponse;

import org.cytoscape.diffusion.internal.task.DiffusionContextMenuTaskFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
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
	private final String logLocation;

	
	private static final String GENERIC_SWAGGER_NOTES = "Diffusion will send the selected network view and its selected nodes to "
			+ "a web-based REST service to calculate network propagation. Results are returned and represented by columns "
			+ "in the node table." + '\n' + '\n'
			+ "Columns are created for each execution of Diffusion and their names are returned in the response."  + '\n' + '\n';


	public DiffusionResource(final CyApplicationManager cyApplicationManager, final SynchronousTaskManager<?> taskManager, final CyNetworkManager cyNetworkManager, final CyNetworkViewManager cyNetworkViewManager, final DiffusionContextMenuTaskFactory diffusionTaskFactory, final DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory, final String logLocation) {
		this.cyApplicationManager = cyApplicationManager;
		this.taskManager = taskManager;
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.diffusionTaskFactory = diffusionTaskFactory;
		this.diffusionWithOptionsTaskFactory = diffusionWithOptionsTaskFactory;
		this.logLocation = logLocation;
	}

	private static final Logger logger = LoggerFactory.getLogger(DiffusionResource.class);


	private final static String cyRESTErrorRoot = "urn:cytoscape:ci:diffusion-app:v1";

	private CIResponse<Object> buildCIErrorResponse(int status, String resourcePath, String code, String message, Exception e)
	{
		CIResponse<Object> response = new CIResponse<Object>();
		response.data = new Object();
		List<CIError> errors = new ArrayList<CIError>();
		CIError error = new CIError();

		error.type = cyRESTErrorRoot + ":" + resourcePath+ ":"+ code;

		if (e != null)
		{
			logger.error(message, e);
		}
		else
		{
			logger.error(message);
		}
		URI link = (new File(logLocation)).toURI();
		error.link = link;
		error.status = status;
		error.message = message;
		errors.add(error);
		response.errors = errors;
		return response;
	}

	class DiffusionTaskObserver implements TaskObserver {

		private CIResponse<?> response;
		DiffusionResultColumns diffusionResultColumns;
		private String resourcePath;
		private String errorCode;

		public DiffusionTaskObserver(String resourcePath, String errorCode){
			response = null;
			this.resourcePath = resourcePath;
			this.errorCode = errorCode;
		}

		@Override
		public void allFinished(FinishStatus arg0) {

			if (arg0.getType() == FinishStatus.Type.SUCCEEDED || arg0.getType() == FinishStatus.Type.CANCELLED) {
				response = new CIResponse<DiffusionResultColumns>();
				((CIResponse<DiffusionResultColumns>)response).data = diffusionResultColumns;
				response.errors = new ArrayList<CIError>();
			}
			else {
				response = buildCIErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resourcePath, errorCode, arg0.getException().getMessage(), arg0.getException());
			}
		}

		@Override
		public void taskFinished(ObservableTask arg0) {
			DiffusionResultColumns jsonResult = arg0.getResults(DiffusionResultColumns.class);
			diffusionResultColumns = jsonResult;	
		}
	}

	public CyNetwork getCyNetwork(String resourcePath, String errorType)
	{
		CyNetwork cyNetwork = cyApplicationManager.getCurrentNetwork();
		
		if (cyNetwork == null) {
			String messageString = "Could not find current Network";
			throw new NotFoundException(messageString, Response.status(Response.Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON)
					.entity(buildCIErrorResponse(404, resourcePath, errorType, messageString, null)).build());
		}
		return cyNetwork;
	}
		
	public CyNetworkView getCyNetworkView(String resourcePath, String errorType) {
		CyNetworkView cyNetworkView = cyApplicationManager.getCurrentNetworkView();
		if (cyNetworkView == null) {
			String messageString = "Could not find current Network View";
			throw new NotFoundException(messageString, Response.status(Response.Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON)
					.entity(buildCIErrorResponse(404, resourcePath, errorType, messageString, null)).build());
		}
		return cyNetworkView;
	}
	
	public CyNetworkView getCyNetworkView(String resourcePath, String errorType, long networkSUID, long networkViewSUID)
	{
		final CyNetwork network = cyNetworkManager.getNetwork(networkSUID);
		if (network == null) {
			String messageString = "Could not find network with SUID: " + networkSUID;
			throw new NotFoundException(messageString, Response.status(Response.Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON)
					.entity(buildCIErrorResponse(404, resourcePath, errorType, messageString, null)).build());
		}
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		if (views.isEmpty()) {
			String messageString = "No views are available for network with SUID: " + networkSUID;
			throw new NotFoundException(messageString, Response.status(Response.Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON)
					.entity(buildCIErrorResponse(404, resourcePath, errorType, messageString, null)).build());
		}
		for (final CyNetworkView view : views) {
			final Long vid = view.getSUID();
			if (vid.equals(networkViewSUID)) {
				return view;
			}
		}
		String messageString = "Could not find network view with SUID: " + networkViewSUID + " for network with SUID: " + networkSUID;
		throw new NotFoundException(messageString, Response.status(Response.Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON)
					.entity(buildCIErrorResponse(404, resourcePath, errorType, messageString, null)).build());
		
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
		CyNetwork cyNetwork = getCyNetwork("diffuse_current_view_with_options", "1");
		CyNetworkView cyNetworkView = getCyNetworkView("diffuse_current_view_with_options", "2");
		
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
		CyNetworkView cyNetworkView = getCyNetworkView("diffuse_with_options", "1", networkSUID, networkViewSUID);
		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver("diffuse_with_options", "2");
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
		CyNetwork cyNetwork = getCyNetwork("diffuse_current_view", "1");
		CyNetworkView cyNetworkView = getCyNetworkView("diffuse_current_view", "2");
		
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
		CyNetworkView cyNetworkView = getCyNetworkView("diffuse", "1", networkSUID, networkViewSUID);
		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver("diffuse", "1");
		TaskIterator taskIterator = diffusionTaskFactory.createTaskIterator(cyNetworkView);
		taskManager.execute(taskIterator, taskObserver);
		return Response.status(taskObserver.response.errors.size() == 0 ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.entity(taskObserver.response).build();

	}
}
