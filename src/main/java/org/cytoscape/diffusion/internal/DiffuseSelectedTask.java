package org.cytoscape.diffusion.internal;

import java.util.List;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;

public class DiffuseSelectedTask extends AbstractTask {

	private CyNetwork network;
	private View<CyNode> nodeView;
	private CyNetworkView networkView;
	private	CyNetworkViewWriterFactory writerFactory;

	public DiffuseSelectedTask(View<CyNode> nodeView, CyNetworkView networkView, CyNetworkViewWriterFactory writerFactory) {
		this.network = networkView.getModel();
		this.nodeView = nodeView;
		this.networkView = networkView;
		this.writerFactory = writerFactory;
	}

	public void run(TaskMonitor tm) throws Exception {
    final List<CyNode> selectedNodes = CyTableUtil.getNodesInState(this.network, CyNetwork.SELECTED, true);
		CyTable nodeTable = this.network.getDefaultNodeTable();
		this.ensureEmptyInputTableExists(nodeTable);
		this.setSelectedNodesInInputTable(nodeTable, selectedNodes);
		String cx = getNetworkViewsAsCX(this.network);
		System.out.println(cx);
    //TODO: Send CX and reload here
	}

  //Ensure any previous inputs are deleted and create a new input table
	private void ensureEmptyInputTableExists(CyTable nodeTable) {
		nodeTable.deleteColumn("diffusion_input");
		nodeTable.createColumn("diffusion_input", Boolean.class, false, false);
	}

  //Set every selected node to true in the input table
	private void setSelectedNodesInInputTable(CyTable nodeTable, List<CyNode> selectedNodes) {
		for (CyNode node : selectedNodes) {
			Long suid = node.getSUID();
			CyRow row = nodeTable.getRow(suid);
			row.set("diffusion_input", true);
		}
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
