package org.cytoscape.diffusion.internal.task;

import javax.swing.SwingUtilities;
import javax.xml.ws.http.HTTPException;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.diffusion.internal.client.DiffusionJSON;
import org.cytoscape.diffusion.internal.client.DiffusionResponse;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTableFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffuseSelectedTask extends AbstractTask {

	private DiffusionNetworkManager diffusionNetworkManager;
	private DiffusionTableFactory diffusionTableFactory;
	private DiffusionJSON diffusionJSON;
	private OutputPanel outputPanel;
	private final CySwingApplication swingApplication;
	private final DiffusionServiceClient client;
	private final static Logger logger = LoggerFactory.getLogger(DiffuseSelectedTask.class);

	public DiffuseSelectedTask(DiffusionNetworkManager networkManager, CyNetworkViewWriterFactory writerFactory,
			OutputPanel outputPanel, final CySwingApplication swingApplication, final CyApplicationManager appManager,
			final DiffusionServiceClient client) {
		this.diffusionNetworkManager = networkManager;
		this.diffusionTableFactory = new DiffusionTableFactory(appManager);
		this.diffusionJSON = new DiffusionJSON(writerFactory);
		this.outputPanel = outputPanel;
		this.swingApplication = swingApplication;
		this.client = client;
	}

	public void run(TaskMonitor tm) throws Exception {
		diffusionNetworkManager.setNodeHeats();
		System.out.println("Getting cx");
		String cx = diffusionJSON.encode(diffusionNetworkManager.getNetwork());
		String subnetId = Long.toString(diffusionNetworkManager.getNetwork().getSUID());
		System.out.println("Getting suid");
		System.out.println(diffusionNetworkManager.getNetwork().getSUID());
		System.out.println("Getting response");
		String responseJSON = client.diffuse(cx, subnetId);
		System.out.println("Got response");
		System.out.println(responseJSON);
		DiffusionResponse response = diffusionJSON.decode(responseJSON);
		System.out.println("Repsponse decoded");
		if (response.getErrors().size() != 0) {
			System.out.println("Called error");
			System.out.println(response.getErrors().get(0).toString());
			logger.error("Problem with the remote diffusion service!");
			logger.error(response.getErrors().get(0).toString(), new Exception());
			throw new Exception(createServiceError(response.getErrors().get(0).toString()));
		} else {
			String columnBaseName = diffusionTableFactory.getNextAvailableColumnName("diffusion_output");
			diffusionTableFactory.writeColumns(columnBaseName, response.getData());
			System.out.println(columnBaseName);
			outputPanel.setColumnName(String.format("%s_rank", columnBaseName));
			// Show the panel
			SwingUtilities.invokeLater(new Runnable() {
				@Override
				public void run() {
					swingApplication.getCytoPanel(CytoPanelName.EAST).setState(CytoPanelState.DOCK);
				}
			});
		}
	}

	private String createServiceError(String errorMessage) {
		return String.format("Oops! Could not complete diffusion. The heat diffusion service in the cloud told us something went wrong while processing your request.\n" +
				"Here is the error message we received from the service, email the service author with this message if you need assistance.\n\n" +
		        "Error:\n %s", errorMessage);
	}
}
