package org.cytoscape.diffusion.internal.task;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;


/**
 * Diffusion service caller with optional parameters.
 *
 */
public class DiffuseSelectedWithOptionsTask extends DiffuseSelectedTask {

	@Tunable(description = "Time:")
	public Double time = 0.1;

	@Tunable(description = "Heat Column:")
	public ListSingleSelection<String> heatColumnName;

	public DiffuseSelectedWithOptionsTask(DiffusionNetworkManager networkManager,
			CyNetworkViewWriterFactory writerFactory, OutputPanel outputPanel, CySwingApplication swingApplication,
			CyApplicationManager appManager, DiffusionServiceClient client, TunableSetter setter) {
		
		super(networkManager, writerFactory, outputPanel, swingApplication, appManager, client, setter);

		initColumnList();
	}

	private final void initColumnList() {
		// Get available columns
		final CyNetwork targetNetwork = diffusionNetworkManager.getNetwork();
		final CyTable localTbl = targetNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		final Collection<CyColumn> cols = localTbl.getColumns();

		final List<String> colNames = cols.stream().filter(col -> col.getType() == Double.class)
				.map(col -> col.getName()).collect(Collectors.toList());
		heatColumnName = new ListSingleSelection<>(colNames);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		final String selectedColumnName = this.heatColumnName.getSelectedValue();
		// Initialize the selected column with input heat parameter
		diffuse(selectedColumnName, time);
	}
}
