package org.cytoscape.diffusion.internal.task;

import java.awt.Component;
import java.awt.Dimension;
import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;

import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.core.interfaces.AspectElement;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.diffusion.internal.client.CIError;
import org.cytoscape.diffusion.internal.client.CIResponse;
import org.cytoscape.diffusion.internal.client.DiffusionResultParser;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTableFactory;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.work.AbstractTask;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;

public class DiffuseSelectedTask extends AbstractTask implements ObservableTask {
	
	private static final String DIFFUSION_INPUT_COL_NAME = "diffusion_input";
	private static final String DIFFUSION_OUTPUT_COL_NAME = "diffusion_output";

	protected DiffusionNetworkManager diffusionNetworkManager;
	protected DiffusionTableFactory diffusionTableFactory;
	protected DiffusionResultParser resultParser;
	protected OutputPanel outputPanel;
	protected final CySwingApplication swingApplication;
	protected final DiffusionServiceClient client;
	
	private String responseJSONString;
	
	private final static Logger logger = LoggerFactory.getLogger(DiffuseSelectedTask.class);

	public DiffuseSelectedTask(DiffusionNetworkManager networkManager, CyNetworkViewWriterFactory writerFactory,
			OutputPanel outputPanel, final CySwingApplication swingApplication, final CyApplicationManager appManager,
			final DiffusionServiceClient client, final TunableSetter setter) {
		this.diffusionNetworkManager = networkManager;
		this.diffusionTableFactory = new DiffusionTableFactory(appManager);
		this.resultParser = new DiffusionResultParser(writerFactory, setter);
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
		final String cx = resultParser.encode(network, inputCol);
		
		// Call the service
		responseJSONString = client.diffuse(cx, columnName, time);
		
		// Parse the result
		Map<String, List<AspectElement>> response = null;
				
		// Parse the result and catch exeption if there is an error in it. 
		try {
			response = resultParser.decode(responseJSONString);
		} catch(Exception e) {
			throw new IllegalStateException("Error occured when parsing result.", e);
		}
		
		final String outputColumnName = diffusionTableFactory.getNextAvailableColumnName(DIFFUSION_OUTPUT_COL_NAME);
		
		final List<AspectElement> nodeAttributes = response.get(NodeAttributesElement.ASPECT_NAME);
		
		diffusionTableFactory.writeColumns(outputColumnName, nodeAttributes);
		outputPanel.setColumnName(String.format("%s_rank", outputColumnName));
	
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

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (type.isAssignableFrom(CIResponse.class)) {
			ObjectMapper objectMapper = new ObjectMapper();
			CIResponse res = null;
			try {
				res = objectMapper.readValue(responseJSONString, CIResponse.class);
			} catch (IOException e) {
				e.printStackTrace();
				res = new CIResponse();
				res.data = new ArrayList<>();
				res.errors = new ArrayList<CIError>();
				CIError ciError = new CIError();
				//TODO Replace this with a more meaningful code.
				ciError.code = e.getClass().getName();
				ciError.message = "App JSON Processing error.";
				//Technically, this is a client side issue, but since we're going through CyREST, it's a 500 error.
				ciError.status = 500;
				//TODO Replace this with log URL if it exists, or set to null.
				ciError.link = new File("").toURI();
				res.errors.add(new CIError());
			}
			return (R) res;
		} else if (type.isAssignableFrom(String.class)) {
			return (R) "Completed.";
		}
		else {
			return null;
		}
	}
}
