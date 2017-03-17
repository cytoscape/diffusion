package org.cytoscape.diffusion.internal.task;

import java.awt.Component;
import java.awt.Dimension;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cxio.core.interfaces.AspectElement;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.diffusion.internal.client.DiffusionResultParser;
import org.cytoscape.diffusion.internal.client.DiffusionResponse;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTableFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class DiffuseSelectedTask extends AbstractTask {
	
	private static final String DIFFUSION_INPUT_COL_NAME = "diffusion_input";
	private static final String DIFFUSION_OUTPUT_COL_NAME = "diffusion_output";

	protected DiffusionNetworkManager diffusionNetworkManager;
	protected DiffusionTableFactory diffusionTableFactory;
	protected DiffusionResultParser diffusionJSON;
	protected OutputPanel outputPanel;
	protected final CySwingApplication swingApplication;
	protected final DiffusionServiceClient client;
	
	
	private final static Logger logger = LoggerFactory.getLogger(DiffuseSelectedTask.class);

	public DiffuseSelectedTask(DiffusionNetworkManager networkManager, CyNetworkViewWriterFactory writerFactory,
			OutputPanel outputPanel, final CySwingApplication swingApplication, final CyApplicationManager appManager,
			final DiffusionServiceClient client, final TunableSetter setter) {
		this.diffusionNetworkManager = networkManager;
		this.diffusionTableFactory = new DiffusionTableFactory(appManager);
		this.diffusionJSON = new DiffusionResultParser(writerFactory, setter);
		this.outputPanel = outputPanel;
		this.swingApplication = swingApplication;
		this.client = client;
	}

	public void run(TaskMonitor tm) throws Exception {
		diffuse(null, null);
	}
	
	protected void diffuse(final String columnName, final Double time) throws Exception {
		
		String inputCol = columnName;
		
		// Case 1: create new input heat column with default values
		if(columnName == null) {
			inputCol = DIFFUSION_INPUT_COL_NAME;
			diffusionNetworkManager.setNodeHeats(inputCol);
		}
		
		// Case 2: Use existing column as-is

		final CyNetwork network = diffusionNetworkManager.getNetwork();		
		final String cx = diffusionJSON.encode(network, inputCol);
		
		
		// Call the service
		final String responseJSON = client.diffuse(cx, columnName, time);
		
		// Parse the result
		final Map<String, List<AspectElement>> response = diffusionJSON.decode(responseJSON);
	
		final List<AspectElement> errors = response.get("errors");
		
		// Error should be empty, otherwise, it's failed.
		if (errors.size() != 0) {
			throw new IllegalStateException(createServiceError(errors.get(0).toString()));
		}

		final String columnBaseName = diffusionTableFactory.getNextAvailableColumnName(DIFFUSION_OUTPUT_COL_NAME);
		
		
		final List<AspectElement> dataAspect = response.get("data");
		AspectElement nodeAttributes = null;
		
		for(final AspectElement a: dataAspect) {
			final String name = a.getAspectName();
			if(name.equals("nodeAttributes")) {
				nodeAttributes = a;
			}
				
		}
		diffusionTableFactory.writeColumns(columnBaseName, response.getData());
		
		outputPanel.setColumnName(String.format("%s_rank", columnBaseName));
	
		showResult();
	}

	protected void showResult() {
		// Show the result in a CytoPanel
		SwingUtilities.invokeLater(new Runnable() {
			@Override
			public void run() {
				final CytoPanel panel = swingApplication.getCytoPanel(CytoPanelName.EAST);
				panel.setState(CytoPanelState.DOCK);
				final Component panelComponent = panel.getThisComponent();
				final Dimension defSize = new Dimension(380, 300);
				panelComponent.setPreferredSize(defSize);
				panelComponent.setMinimumSize(defSize);
				panelComponent.setSize(defSize);
			}
		});
	}
	
	
	protected String createServiceError(String errorMessage) {
		return String.format("Oops! Could not complete diffusion. The heat diffusion service in the cloud told us something went wrong while processing your request.\n" +
				"Here is the error message we received from the service, email the service author with this message if you need assistance.\n\n" +
		        "Error:\n %s", errorMessage);
	}
}
