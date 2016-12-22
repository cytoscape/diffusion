package org.cytoscape.diffusion.internal.task;

import javax.swing.SwingUtilities;

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

public class DiffuseSelectedTask extends AbstractTask {

	private DiffusionNetworkManager diffusionNetworkManager;
	private DiffusionTableFactory diffusionTableFactory;
	private DiffusionJSON diffusionJSON;
	private OutputPanel outputPanel;
	private final CySwingApplication swingApplication;
	private final DiffusionServiceClient client;

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
		DiffusionResponse response = diffusionJSON.decode(responseJSON);
		System.out.println("Repsponse decoded");
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
