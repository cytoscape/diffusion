package org.cytoscape.diffusion.internal;

import org.cytoscape.diffusion.internal.NodeTable;
import org.cytoscape.diffusion.internal.HeatMap;
import org.cytoscape.diffusion.internal.OutputPanel;
import org.cytoscape.diffusion.internal.DiffusionService;

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
	private OutputPanel panel;

	public DiffuseSelectedTask(View<CyNode> nodeView, CyNetworkView networkView, CyNetworkViewWriterFactory writerFactory, OutputPanel panel) {
		this.network = networkView.getModel();
		this.writerFactory = writerFactory;
		this.panel = panel;
	}

	public void run(TaskMonitor tm) throws Exception {
		//Get the selected nodes in the current view
    final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(this.network, CyNetwork.SELECTED, true);
	  //Get the default node table where we will attach input and output columns
		NodeTable nodeTable = new NodeTable(this.network.getDefaultNodeTable());
		//Create an input heats table based on the selected nodes
		nodeTable.setInitialHeats(selectedNodes);
		//Get the current network as CX (Which includes the base network)
		String cx = getNetworkViewsAsCX(this.network);
	  //Get the subnet Id of the current network
		String subnetId = Long.toString(this.network.getSUID());
		//Diffuse the current subnet and parse into  an output heatmap
		HeatMap heatMap = HeatMap.parseJSON(DiffusionService.diffuse(cx, subnetId));
		//Create an output column with output heats
    String outputColumnName = nodeTable.setOutputHeats(heatMap);
    //Calculate the perentile threshold for the output heats
		Double threshold = nodeTable.getThreshold(outputColumnName, 90);
		//Select all nodes with heat greater than the percentile
		nodeTable.selectNodesOverThreshold(outputColumnName, threshold);
		//Write output column name to panel
		panel.setOutputColumn(outputColumnName);
		//Write theshold to panel
		panel.updateThreshold();
	}

  //Convert the network and it's associated tables to CX for transport to the service
	private String getNetworkViewsAsCX(CyNetwork network) throws IOException {
		final ByteArrayOutputStream stream = new ByteArrayOutputStream();
		CyWriter writer = this.writerFactory.createWriter(stream, network);
		String jsonString = null;
		try {
			writer.run(null);
			jsonString = stream.toString("UTF-8");
			stream.close();
		} catch (Exception e) {
			throw new IOException();
		}
		return jsonString;
	}


}
