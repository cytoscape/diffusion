package org.cytoscape.diffusion.internal;

import java.util.List;
import java.util.Map;

import java.io.IOException;
import java.io.ByteArrayOutputStream;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class DiffuseSelectedTask extends AbstractTask {

	private CyNetwork network;
	private	CyNetworkViewWriterFactory writerFactory;

	private DiffusionNetworkManager diffusionNetworkManager;
	private DiffusionTableFactory diffusionTableFactory;
	private DiffusionJSON diffusionJSON;
	private OutputPanel outputPanel;

	public DiffuseSelectedTask(DiffusionNetworkManager networkManager, CyNetworkViewWriterFactory writerFactory, OutputPanel outputPanel) {
		this.diffusionNetworkManager = networkManager;
		this.diffusionTableFactory = new DiffusionTableFactory(diffusionNetworkManager.appManager);
		this.diffusionJSON = new DiffusionJSON(writerFactory);
		this.outputPanel = outputPanel;
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
		//Create an output column with output heats
    //String outputColumnName = nodeTable.setOutputHeats(heatMap);
    //Calculate the perentile threshold for the output heats
	  //Double threshold = nodeTable.getThreshold(outputColumnName, 90);
		//Select all nodes with heat greater than the percentile
		//nodeTable.selectNodesOverThreshold(outputColumnName, threshold);
		//Write output column name to panel
		//panel.setOutputColumn(outputColumnName);
		//Write theshold to panel
		//panel.updateThreshold();
	}


}
