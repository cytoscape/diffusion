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

import org.cytoscape.diffusion.internal.client.CIError;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import io.swagger.annotations.ApiResponse;
import io.swagger.annotations.ApiResponses;

@Api(tags = {"Apps", "Diffusion"})
@Path("/diffusion/v1/")
public class DiffusionResource {

	private SynchronousTaskManager<?> taskManager;
	
	private CyNetworkManager cyNetworkManager;
	private CyNetworkViewManager cyNetworkViewManager;
	
	private DiffusionContextMenuTaskFactory diffusionTaskFactory;
	private DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory;
	private String logLocation;

	private static final String GENERIC_NOTES = "<p>Diffusion will send the selected network view and its selected nodes to "
			+ "a web-based REST service to calculate network propagation. Results are returned and represented by columns "
			+ "in the node table.</p>"
			+ "Columns will named according to the following convention:<br>"
			+" <ul><li><b>_heat</b></li><li>_rank</li></ul>";
	
	public DiffusionResource(SynchronousTaskManager<?> taskManager, CyNetworkManager cyNetworkManager, CyNetworkViewManager cyNetworkViewManager, DiffusionContextMenuTaskFactory diffusionTaskFactory, DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory, String logLocation) {
		this.taskManager = taskManager;
		this.cyNetworkManager = cyNetworkManager;
		this.cyNetworkViewManager = cyNetworkViewManager;
		this.diffusionTaskFactory = diffusionTaskFactory;
		this.diffusionWithOptionsTaskFactory = diffusionWithOptionsTaskFactory;
		this.logLocation = logLocation;
	}

	private static final Logger logger = LoggerFactory.getLogger(DiffusionResource.class);

	private final static String cyRESTErrorRoot = "cy://cytoscape-diffusion-app";

	private DiffusionResponse buildCIErrorResponse(int status, String resourcePath, String code, String message, Exception e)
	{
		DiffusionResponse response = new DiffusionResponse();
		response.data = new StatusMessage();
		response.data.successful = false;
		List<CIError> errors = new ArrayList<CIError>();
		CIError error = new CIError();
		error.code = cyRESTErrorRoot + resourcePath+ "/"+ code;
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

		private DiffusionResponse response;
		private String resourcePath;
		private String errorCode;

		public DiffusionTaskObserver(String resourcePath, String errorCode){
			response = null;
			this.resourcePath = resourcePath;
			this.errorCode = errorCode;
		}

		@Override
		public void allFinished(FinishStatus arg0) {

			if (arg0.getType() == FinishStatus.Type.SUCCEEDED || arg0.getType() == FinishStatus.Type.CANCELLED)
			{
				response = new DiffusionResponse();
				response.data = new StatusMessage();
				response.data.successful = true;
				response.errors = new ArrayList<CIError>();
			}
			else
			{
				response = buildCIErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resourcePath, errorCode, arg0.getException().getMessage(), arg0.getException());
			}
		}

		@Override
		public void taskFinished(ObservableTask arg0) {

		}
	}

	public CyNetworkView getCyNetworkView(String resourcePath, String errorCode, long networkSUID, long networkViewSUID)
	{
		final CyNetwork network = cyNetworkManager.getNetwork(networkSUID);
		if (network == null) {
			String messageString = "Could not find network with SUID: " + networkSUID;
			throw new NotFoundException(messageString, Response.status(Response.Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON)
					.entity(buildCIErrorResponse(404, resourcePath, errorCode, messageString, null)).build());
		}
		final Collection<CyNetworkView> views = cyNetworkViewManager.getNetworkViews(network);
		if (views.isEmpty()) {
			String messageString = "No views are available for network with SUID: " + networkSUID;
			throw new NotFoundException(messageString, Response.status(Response.Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON)
					.entity(buildCIErrorResponse(404, resourcePath, errorCode, messageString, null)).build());
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
					.entity(buildCIErrorResponse(404, resourcePath, errorCode, messageString, null)).build());
		
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("{networkSUID}/views/{networkViewSUID}/diffuse_with_options")
	@ApiOperation(value = "Execute Diffusion Analysis with Options",
	notes = GENERIC_NOTES,
	response = DiffusionResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network does not exist", response = DiffusionResponse.class),
	})

	public Response diffuseWithOptions(@ApiParam(name="Network SUID", value="see GET /v1/networks") @PathParam("networkSUID") long networkSUID, @ApiParam(name="Network View SUID") @PathParam("networkViewSUID") long networkViewSUID, @ApiParam(value = "Options", required = true) DiffusionParameters diffusionParameters) {

		System.out.println("Accessing Diffusion with options via REST");
		CyNetworkView cyNetworkView = getCyNetworkView("{networkSUID}/views/{networkViewSUID}/diffuse_with_options", "1", networkSUID, networkViewSUID);
		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver("{networkSUID}/views/{networkViewSUID}/diffuse_with_options", "2");
		Map<String, Object> tunableMap = new HashMap<String, Object>();
		tunableMap.put("headColumnName", diffusionParameters.heatColumnName);
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
	@Path("{networkSUID}/views/{networkViewSUID}/diffuse")
	@ApiOperation(value = "Execute Diffusion Analysis",
	notes = GENERIC_NOTES 
			+ "<br><br>The nodes you would like to use as input heats should be selected. This will be used to "
			+ " automatically generate the contents of the heat input column.",
			response = DiffusionResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network does not exist", response = DiffusionResponse.class),
	})

	public Response diffuse(@PathParam("networkSUID") long networkSUID, @PathParam("networkViewSUID") long networkViewSUID) {

		System.out.println("Accessing Diffusion via REST");
		CyNetworkView cyNetworkView = getCyNetworkView("/diffuse", "1", networkSUID, networkViewSUID);
		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver("/diffuse", "1");
		TaskIterator taskIterator = diffusionTaskFactory.createTaskIterator(cyNetworkView);
		taskManager.execute(taskIterator, taskObserver);
		return Response.status(taskObserver.response.errors.size() == 0 ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.entity(taskObserver.response).build();

	}
}
