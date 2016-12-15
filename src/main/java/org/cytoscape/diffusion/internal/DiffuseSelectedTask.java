package org.cytoscape.diffusion.internal;

import javax.swing.SwingUtilities;

import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class DiffuseSelectedTask extends AbstractTask {
	
	private DiffusionNetworkManager diffusionNetworkManager;
	private DiffusionTableFactory diffusionTableFactory;
	private DiffusionJSON diffusionJSON;
	private OutputPanel outputPanel;
	private final CySwingApplication swingApplication;

	public DiffuseSelectedTask(DiffusionNetworkManager networkManager, CyNetworkViewWriterFactory writerFactory,
			OutputPanel outputPanel, final CySwingApplication swingApplication) {
		this.diffusionNetworkManager = networkManager;
		this.diffusionTableFactory = new DiffusionTableFactory(diffusionNetworkManager.appManager);
		this.diffusionJSON = new DiffusionJSON(writerFactory);
		this.outputPanel = outputPanel;
		this.swingApplication = swingApplication;
	}

	public void run(TaskMonitor tm) throws Exception {
		diffusionNetworkManager.setNodeHeats();
		System.out.println("Getting cx");
		String cx = diffusionJSON.encode(diffusionNetworkManager.getNetwork());
		String subnetId = Long.toString(diffusionNetworkManager.getNetwork().getSUID());
		System.out.println("Getting suid");
		System.out.println(diffusionNetworkManager.getNetwork().getSUID());
		System.out.println("Getting response");
		String responseJSON = DiffusionService.diffuse(cx, subnetId);
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

		// Create an output column with output heats
		// String outputColumnName = nodeTable.setOutputHeats(heatMap);
		// Calculate the perentile threshold for the output heats
		// Double threshold = nodeTable.getThreshold(outputColumnName, 90);
		// Select all nodes with heat greater than the percentile
		// nodeTable.selectNodesOverThreshold(outputColumnName, threshold);
		// Write output column name to panel
		// panel.setOutputColumn(outputColumnName);
		// Write theshold to panel
		// panel.updateThreshold();
	}

}
