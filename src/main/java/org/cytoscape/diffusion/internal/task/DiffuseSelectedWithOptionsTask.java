package org.cytoscape.diffusion.internal.task;

import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.diffusion.internal.DiffusionDocumentation;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.Tunable;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.TunableValidator;
import org.cytoscape.work.util.ListSingleSelection;

/**
 * Diffusion service caller with optional parameters.
 *
 */
public class DiffuseSelectedWithOptionsTask extends DiffuseSelectedTask implements TunableValidator {

	private static final String FROM_SELECTION_MENU = "(Use selected nodes)";

	public static final Double DEFAULT_TIME = 0.1;
	
	@Tunable(description = "Time:", longDescription=DiffusionDocumentation.TIME_LONG_DESCRIPTION, exampleStringValue="0.1")
	public Double time = DEFAULT_TIME;

	@Tunable(description = "Heat Column:", longDescription=DiffusionDocumentation.HEAT_COLUMN_NAME_LONG_DESCRIPTION, exampleStringValue=DIFFUSION_INPUT_COL_NAME)
	public ListSingleSelection<String> heatColumnName;

	public DiffuseSelectedWithOptionsTask(DiffusionTableManager tableManager, CyNetwork network,
			CyNetworkViewWriterFactory writerFactory, OutputPanel outputPanel, CySwingApplication swingApplication,
			CyApplicationManager appManager, DiffusionServiceClient client, TunableSetter setter) {

		super(tableManager, network, writerFactory, outputPanel, swingApplication, appManager, client, setter);

		initColumnList();
	}

	private final void initColumnList() {
		// Get available columns

		final CyNetwork targetNetwork = network;
		final CyTable localTbl = targetNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		final Collection<CyColumn> cols = localTbl.getColumns();

		final List<String> colNames = cols.stream().filter(col -> col.getType() == Double.class)
				.map(col -> col.getName()).collect(Collectors.toList());

		// Add extra column for input heats from new selection
		colNames.add(FROM_SELECTION_MENU);

		heatColumnName = new ListSingleSelection<>(colNames);
		heatColumnName.setSelectedValue(FROM_SELECTION_MENU);
	}

	@Override
	public void run(TaskMonitor tm) throws Exception {
		tm.setTitle("Running Heat Diffusion");
		tm.setStatusMessage("Running heat diffusion service.  Please wait...");
		
		final String selectedColumnName = this.heatColumnName.getSelectedValue();

		// Special case: new heat column
		if (selectedColumnName.equals(FROM_SELECTION_MENU)) {
			diffuse(null, time);
		} else {
			// Initialize the selected column with input heat parameter
			diffuse(selectedColumnName, time);
		}
	}

	@Override
	public ValidationState getValidationState(Appendable message) {
		if (time < 0) {
			
            try {
				message.append("Please enter positive value for time parameter.");
			} catch (IOException e) {
				e.printStackTrace();
			}
            return TunableValidator.ValidationState.INVALID;
        } else {
            return TunableValidator.ValidationState.OK;
        }
	}
}
