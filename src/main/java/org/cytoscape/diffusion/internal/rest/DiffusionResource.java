package org.cytoscape.diffusion.internal.rest;

import java.io.File;
import java.io.IOException;
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
import org.cytoscape.work.json.JSONResult;
import org.cytoscape.work.util.ListSingleSelection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.swagger.annotations.Api;
import io.swagger.annotations.ApiModel;
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

	
	private static final String GENERIC_SWAGGER_NOTES = "Diffusion will send the selected network view and its selected nodes to "
			+ "a web-based REST service to calculate network propagation. Results are returned and represented by columns "
			+ "in the node table.\r\n"
			+ "Columns are created for each execution of Diffusion and their names are returned in the response.\r\n\r\n";

	/*
	 * Replicated from CyREST.
	 */
	public final static String NETWORK_GET_LINK = "[/v1/networks](#!/Networks/getNetworksAsSUID)";
	public final static String NETWORK_VIEWS_LINK = "[/v1/networks/{networkId}/views](#!/Network32Views/getAllNetworkViews)";
	
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

	private DiffusionResponse<Object> buildCIErrorResponse(int status, String resourcePath, String code, String message, Exception e)
	{
		DiffusionResponse<Object> response = new DiffusionResponse<Object>();
		response.data = new Object();
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

		private DiffusionResponse<?> response;
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
				response = new DiffusionResponse<DiffusionResultColumns>();
				((DiffusionResponse<DiffusionResultColumns>)response).data = diffusionResultColumns;
				response.errors = new ArrayList<CIError>();
			}
			else {
				response = buildCIErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resourcePath, errorCode, arg0.getException().getMessage(), arg0.getException());
			}
		}

		@Override
		public void taskFinished(ObservableTask arg0) {
			JSONResult jsonResult = arg0.getResults(JSONResult.class);
			ObjectMapper objectMapper = new ObjectMapper();
			try {
				diffusionResultColumns = objectMapper.readValue(jsonResult.getJSON(), DiffusionResultColumns.class);	
			} catch (JsonParseException e) {
				e.printStackTrace();
			} catch (JsonMappingException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
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
	
	@ApiModel(value="Diffusion Successful Response", description="Diffusion Analysis Results in CI Format", parent=DiffusionResponse.class)
	public static class SuccessfulDiffusionResponse extends DiffusionResponse<DiffusionResultColumns>{
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("{networkSUID}/views/{networkViewSUID}/diffuse_with_options")
	@ApiOperation(value = "Execute Diffusion Analysis with Options",
	notes = GENERIC_SWAGGER_NOTES,
	response = SuccessfulDiffusionResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network does not exist", response = DiffusionResponse.class)
	})

	public Response diffuseWithOptions(@ApiParam(value="Network SUID (see GET /v1/networks)") @PathParam("networkSUID") long networkSUID, @ApiParam(value="Network View SUID (see GET /v1/networks/{networkId}/views)") @PathParam("networkViewSUID") long networkViewSUID, @ApiParam(value = "Diffusion Parameters", required = true) DiffusionParameters diffusionParameters) {

		System.out.println("Accessing Diffusion with options via REST");
		CyNetworkView cyNetworkView = getCyNetworkView("{networkSUID}/views/{networkViewSUID}/diffuse_with_options", "1", networkSUID, networkViewSUID);
		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver("{networkSUID}/views/{networkViewSUID}/diffuse_with_options", "2");
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
	@Path("{networkSUID}/views/{networkViewSUID}/diffuse")
	@ApiOperation(value = "Execute Diffusion Analysis",
	notes = GENERIC_SWAGGER_NOTES 
			+ "The nodes you would like to use as input should be selected. This will be used to "
			+ "generate the contents of the **diffusion\\_input** column, which represents the query vector and corresponds to h in the diffusion equation.\r\n",
			response = SuccessfulDiffusionResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network does not exist", response = DiffusionResponse.class),
	})

	public Response diffuse(@ApiParam(value="Network SUID (see GET /v1/networks)") @PathParam("networkSUID") long networkSUID, @ApiParam(value="Network View SUID (see GET /v1/networks/{networkId}/views)") @PathParam("networkViewSUID") long networkViewSUID) {

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
