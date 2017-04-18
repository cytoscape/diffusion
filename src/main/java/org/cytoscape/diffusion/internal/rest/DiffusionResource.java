package org.cytoscape.diffusion.internal.rest;

import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.NotFoundException;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.cytoscape.diffusion.internal.client.CIError;
import org.cytoscape.diffusion.internal.task.DiffusionContextMenuTaskFactory;
import org.cytoscape.diffusion.internal.task.DiffusionTaskFactory;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
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
	private DiffusionNetworkManager diffusionNetworkManager;
	private DiffusionTaskFactory diffusionTaskFactory;
	private DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory;
	private String logLocation;

	public DiffusionResource(SynchronousTaskManager<?> taskManager, DiffusionNetworkManager diffusionNetworkManager, DiffusionTaskFactory diffusionTaskFactory, DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory, String logLocation) {
		this.taskManager = taskManager;
		this.diffusionNetworkManager = diffusionNetworkManager;
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

	public void checkNetworkExistsPrecondition(String resourcePath, String errorCode)
	{
		if (diffusionNetworkManager.getNetwork() == null)
		{
			String messageString = "Network does not exist";
			throw new NotFoundException(messageString, Response.status(Response.Status.NOT_FOUND)
					.type(MediaType.APPLICATION_JSON)
					.entity(buildCIErrorResponse(404, resourcePath, errorCode, messageString, null)).build());
		}
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/diffuse_with_options")
	@ApiOperation(value = "Execute Diffusion Analysis with Options",
	notes = "",
	response = DiffusionResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network does not exist", response = DiffusionResponse.class),
	})

	public Response diffuseWithOptions(@ApiParam(value = "Options", required = true) DiffusionParameters diffusionParameters) {

		System.out.println("Accessing Diffusion with options via REST");
		checkNetworkExistsPrecondition("/diffuse_with_options", "1");
		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver("/diffuse_with_options", "2");
		Map<String, Object> tunableMap = new HashMap<String, Object>();
		tunableMap.put("headColumnName", diffusionParameters.heatColumnName);
		tunableMap.put("time", diffusionParameters.time);
		TaskIterator taskIterator = diffusionWithOptionsTaskFactory.createTaskIterator(null);
		taskManager.setExecutionContext(tunableMap);
		taskManager.execute(taskIterator, taskObserver);
		return Response.status(taskObserver.response.errors.size() == 0 ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
				.type(MediaType.APPLICATION_JSON)
				.entity(taskObserver.response).build();
	}
	
	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/diffuse")
	@ApiOperation(value = "Execute Diffusion Analysis",
	notes = "Ensure that the nodes you would like to use as input heats are selected in order to generate the heat "
			+ "input column.",
	response = DiffusionResponse.class)
	@ApiResponses(value = { 
			@ApiResponse(code = 404, message = "Network does not exist", response = DiffusionResponse.class),
	})

	public Response diffuse() {
		{
			System.out.println("Accessing Diffusion via REST");
			checkNetworkExistsPrecondition("/diffuse", "1");
			DiffusionTaskObserver taskObserver = new DiffusionTaskObserver("/diffuse", "1");
			TaskIterator taskIterator = diffusionTaskFactory.createTaskIterator();
			taskManager.execute(taskIterator, taskObserver);
			return Response.status(taskObserver.response.errors.size() == 0 ? Response.Status.OK : Response.Status.INTERNAL_SERVER_ERROR)
					.type(MediaType.APPLICATION_JSON)
					.entity(taskObserver.response).build();
		}
	}
}
