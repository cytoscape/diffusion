package org.cytoscape.diffusion.internal.rest;

import java.util.ArrayList;

import javax.ws.rs.core.Response;

import org.cytoscape.diffusion.internal.client.CIError;
import org.cytoscape.diffusion.internal.client.CIResponse;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskObserver;

public class DiffusionTaskObserver implements TaskObserver {

	/**
	 * 
	 */
	private final DiffusionResource diffusionResource;
	CIResponse<?> response;
	public CIResponse<?> getResponse() {
		return response;
	}

	DiffusionResultColumns diffusionResultColumns;
	private String resourcePath;
	private String errorCode;

	public DiffusionTaskObserver(DiffusionResource diffusionResource, String resourcePath, String errorCode){
		this.diffusionResource = diffusionResource;
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
			response = this.diffusionResource.buildCIErrorResponse(Response.Status.INTERNAL_SERVER_ERROR.getStatusCode(), resourcePath, errorCode, arg0.getException().getMessage(), arg0.getException());
		}
	}

	@Override
	public void taskFinished(ObservableTask arg0) {
		DiffusionResultColumns jsonResult = arg0.getResults(DiffusionResultColumns.class);
		diffusionResultColumns = jsonResult;	
	}
}