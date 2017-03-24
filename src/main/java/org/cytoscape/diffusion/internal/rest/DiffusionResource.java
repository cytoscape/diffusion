package org.cytoscape.diffusion.internal.rest;

import java.util.HashMap;
import java.util.Map;

import javax.ws.rs.Consumes;
import javax.ws.rs.POST;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;

import org.cytoscape.diffusion.internal.client.CIResponse;
import org.cytoscape.diffusion.internal.task.DiffuseSelectedTask;
import org.cytoscape.diffusion.internal.task.DiffusionContextMenuTaskFactory;
import org.cytoscape.diffusion.internal.task.DiffusionTaskFactory;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskObserver;

@Path("/diffusion/v1/")
public class DiffusionResource {
	private SynchronousTaskManager<?> taskManager;
	private DiffusionTaskFactory diffusionTaskFactory;
	private DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory;

	public DiffusionResource(SynchronousTaskManager<?> taskManager, DiffusionTaskFactory diffusionTaskFactory, DiffusionContextMenuTaskFactory diffusionWithOptionsTaskFactory) {
		this.taskManager = taskManager;
		this.diffusionTaskFactory = diffusionTaskFactory;
		this.diffusionWithOptionsTaskFactory = diffusionWithOptionsTaskFactory;
	}

	class DiffusionTaskObserver implements TaskObserver{

		private CIResponse ciResponse;

		public DiffusionTaskObserver(){
			ciResponse = null;
		}

		@Override
		public void allFinished(FinishStatus arg0) {

		}

		@Override
		public void taskFinished(ObservableTask arg0) {
			if (arg0 instanceof DiffuseSelectedTask) {
				ciResponse = arg0.getResults(CIResponse.class);
			}
		}
	}

	@POST
	@Produces("application/json")
	@Consumes("application/json")
	@Path("/diffuse")
	public CIResponse diffuse(DiffusionParameters diffusionParameters) {
		if (diffusionParameters == null) {
			System.out.println("Accessing Diffusion via REST");
			DiffusionTaskObserver taskObserver = new DiffusionTaskObserver();
			TaskIterator taskIterator = diffusionTaskFactory.createTaskIterator();
			taskManager.execute(taskIterator, taskObserver);
			return taskObserver.ciResponse;
		}
		else {
			System.out.println("Accessing Diffusion with options via REST");
			DiffusionTaskObserver taskObserver = new DiffusionTaskObserver();
			Map<String, Object> tunableMap = new HashMap<String, Object>();
			tunableMap.put("headColumnName", diffusionParameters.heatColumnName);
			tunableMap.put("time", diffusionParameters.time);
			TaskIterator taskIterator = diffusionWithOptionsTaskFactory.createTaskIterator(null);
			taskManager.setExecutionContext(tunableMap);
			taskManager.execute(taskIterator, taskObserver);
			return taskObserver.ciResponse;
		}
	}
}
