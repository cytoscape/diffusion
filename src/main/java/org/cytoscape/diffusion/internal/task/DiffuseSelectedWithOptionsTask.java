package org.cytoscape.diffusion.internal.task;

import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import javax.swing.table.DefaultTableCellRenderer;

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
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListSingleSelection;

public class DiffuseSelectedWithOptionsTask extends DiffuseSelectedTask {
	
	@Tunable(description="Time:")
	public Double time = 0.1;
	
	@Tunable(description="Heat Column:")
	public ListSingleSelection<String> heatColumnName;
	

	public DiffuseSelectedWithOptionsTask(DiffusionNetworkManager networkManager,
			CyNetworkViewWriterFactory writerFactory, OutputPanel outputPanel, CySwingApplication swingApplication,
			CyApplicationManager appManager, DiffusionServiceClient client, TunableSetter setter) {
		super(networkManager, writerFactory, outputPanel, swingApplication, appManager, client, setter);
		
		initColumnList(networkManager);
	}
	
	private final void initColumnList(final DiffusionNetworkManager networkManager) {
		// Get available columns
		final CyNetwork targetNetwork = networkManager.getNetwork();
		final CyTable localTbl = targetNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		final Collection<CyColumn> cols = localTbl.getColumns();
		
		final List<String> colNames = cols.stream().map(col->col.getName()).collect(Collectors.toList());
		heatColumnName = new ListSingleSelection<>(colNames);
	}
	
	
}
