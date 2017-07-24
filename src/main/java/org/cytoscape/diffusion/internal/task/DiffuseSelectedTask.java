package org.cytoscape.diffusion.internal.task;

import java.awt.Component;
import java.awt.Dimension;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.swing.SwingUtilities;

import org.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.core.interfaces.AspectElement;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.diffusion.internal.client.DiffusionResultParser;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.client.DiffusionServiceException;
import org.cytoscape.diffusion.internal.client.NodeAttributes;
import org.cytoscape.diffusion.internal.rest.DiffusionResultColumns;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionResult;
import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;

import org.cytoscape.work.ObservableTask;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkTask;

import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.json.ExampleJSONString;
import org.cytoscape.work.json.JSONResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

public class DiffuseSelectedTask extends AbstractNetworkTask implements ObservableTask {

	private static final String heatSuffix = "_heat";
	private static final String rankSuffix = "_rank";

	private static final String ORIGINAL_HEAT_ATTR_NAME = "diffusion_output_heat";
	private static final String ORIGINAL_RANK_ATTR_NAME = "diffusion_output_rank";

	public static final String DIFFUSION_INPUT_COL_NAME = "diffusion_input";

	private static final String DIFFUSION_OUTPUT_COL_NAME = "diffusion_output";

	protected DiffusionResultParser resultParser;
	protected OutputPanel outputPanel;
	protected final CySwingApplication swingApplication;
	protected final DiffusionServiceClient client;
	protected final CyApplicationManager appManager;
	protected final DiffusionTableManager tableManager;
	protected TaskMonitor tm;

	private final static Logger logger = LoggerFactory.getLogger(DiffuseSelectedTask.class);

	public DiffuseSelectedTask(DiffusionTableManager tableManager, CyNetwork network, CyNetworkViewWriterFactory writerFactory, OutputPanel outputPanel,
			final CySwingApplication swingApplication, final CyApplicationManager appManager,
			final DiffusionServiceClient client, final TunableSetter setter) {
		super(network);

		this.tableManager = tableManager;

		this.resultParser = new DiffusionResultParser(writerFactory, setter);
		this.outputPanel = outputPanel;
		this.swingApplication = swingApplication;
		this.client = client;
		this.appManager = appManager;
	}

	private DiffusionResultColumns diffusionResultColumns = null;

	public void run(TaskMonitor tm) throws Exception {
		this.tm = tm;
		tm.setTitle("Running Heat Diffusion");
		tm.setStatusMessage("Running heat diffusion service.  Please wait...");
		diffuse(null, null);

	}

	protected void diffuse(final String columnName, final Double time) throws Exception {

		String inputCol = columnName;		
		tm.setStatusMessage("Creating heat columns");
		// Case 1: create new input heat column with default values
		if (columnName == null) {
			inputCol = DIFFUSION_INPUT_COL_NAME;
			setInputHeatValues(inputCol);
		}
		
		// Case 2: Use existing column as-is
		final String cx = resultParser.encode(network, inputCol);

//		System.out.println("\n\n" + cx);
		tm.setStatusMessage("Running diffusion");
		
		// Call the service
		final String responseJSONString = client.diffuse(cx, columnName, time);
		
		tm.setStatusMessage("Decoding response");
//		System.out.println("\n\n" + responseJSONString);
		
		// Parse the result
		Map<String, List<AspectElement>> response = null;

		// Parse the result and catch exeption if there is an error in it.
		try {
			response = resultParser.decode(responseJSONString);
		} catch (DiffusionServiceException e) {
			//If the DiffusionServiceException is thrown, the service returned some errors.
			throw e;
		}
		catch (Exception e) {
			
			logger.error("Could not parse the following Diffusion service response: " + responseJSONString);
			throw new IllegalStateException("Could not parse the Diffusion service response", e);
		}
		tm.setStatusMessage("Loading Result");
		
		final String outputColumnName = getNextAvailableColumnName(DIFFUSION_OUTPUT_COL_NAME);
		final List<AspectElement> nodeAttributes = response.get(NodeAttributesElement.ASPECT_NAME);

		// Write values to the local table.
		setResult(outputColumnName, nodeAttributes);
		
		// This is hacky, like the rest of column naming.
		diffusionResultColumns = new DiffusionResultColumns();
		diffusionResultColumns.heatColumn = String.format("%s_heat", outputColumnName);
		diffusionResultColumns.rankColumn = String.format("%s_rank", outputColumnName);
		
		appManager.setCurrentNetwork(network);

		outputPanel.setColumnName(String.format("%s_rank", outputColumnName));
		outputPanel.swapPanel(true);
		showResult();
		tm.setStatusMessage("Cleaning up");
	}

	private final void setInputHeatValues(final String columnName) {

		this.ensureEmptyTableExists(columnName);

		final List<CyNode> nodes = CyTableUtil.getNodesInState(network, CyNetwork.SELECTED, true);
		for (CyNode node : nodes) {
			Long suid = node.getSUID();
			CyRow row = getNodeTable().getRow(suid);
			row.set(columnName, 1.0);
		}
	}

	private void ensureEmptyTableExists(final String columnName) {
		getNodeTable().deleteColumn(columnName);
		getNodeTable().createColumn(columnName, Double.class, false, 0.0);
	}

	private void createColumns(String base) {
		final CyTable table = this.getNodeTable();
		if (table == null) {
			return;
		}

		table.createColumn(formatColumnName(base, rankSuffix), Integer.class, false);
		table.createColumn(formatColumnName(base, heatSuffix), Double.class, false, 0.0);
	}

	private final void setResult(final String baseColumnName, final List<AspectElement> nodeAttrs) {
		final CyTable table = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		if (table == null) {
			throw new IllegalStateException("Table does not exists yet.");
		}

		// This is a HACK. Create pre-defined columns first.
		createColumns(baseColumnName);

		final String rankColumnName = formatColumnName(baseColumnName, rankSuffix);
		final String heatColumnName = formatColumnName(baseColumnName, heatSuffix);

		// Create result object
		DiffusionTable diffTable = tableManager.getTable(network.getSUID());
		if(diffTable == null) {
			diffTable = tableManager.createTable(network);
		}

		final DiffusionResult result = new DiffusionResult(network, rankColumnName, heatColumnName);
		diffTable.setDiffusionResult(baseColumnName, result);

		for (final AspectElement attr : nodeAttrs) {

			final NodeAttributesElement nodeAttr = (NodeAttributesElement) attr;
			final Long suid = nodeAttr.getPropertyOf().get(0);
			final CyRow row = getNodeTable().getRow(suid);

			final String attrName = nodeAttr.getName();
			final String valueStr = nodeAttr.getValue();

			if (attrName.equals(ORIGINAL_HEAT_ATTR_NAME)) {
				row.set(heatColumnName, parseValue(ATTRIBUTE_DATA_TYPE.DOUBLE, valueStr));
			} else if (attrName.equals(ORIGINAL_RANK_ATTR_NAME)) {
				row.set(rankColumnName, parseValue(ATTRIBUTE_DATA_TYPE.INTEGER, valueStr));
			} else {
				continue;
			}
		}

	}

	private final Object parseValue(final ATTRIBUTE_DATA_TYPE type, final String valueStr) {
		if (type.equals(ATTRIBUTE_DATA_TYPE.STRING)) {
			return valueStr;
		} else if (type.equals(ATTRIBUTE_DATA_TYPE.INTEGER)) {
			return Integer.parseInt(valueStr);
		} else if (type.equals(ATTRIBUTE_DATA_TYPE.LONG)) {
			return Long.parseLong(valueStr);
		} else if (type.equals(ATTRIBUTE_DATA_TYPE.DOUBLE) || type.equals(ATTRIBUTE_DATA_TYPE.FLOAT)) {
			return Double.parseDouble(valueStr);
		} else if (type.equals(ATTRIBUTE_DATA_TYPE.BOOLEAN)) {
			return Boolean.parseBoolean(valueStr);
		} else {
			throw new IllegalArgumentException("don't know how to deal with type '" + type.toString() + "'");
		}
	}

	public void setResults(String base, Map<String, NodeAttributes> nodes) {

		final CyTable table = this.getNodeTable();

		if (table == null) {
			throw new IllegalStateException("Table does not exists yet.");
		}

		createColumns(base);
		for (Map.Entry<String, NodeAttributes> entry : nodes.entrySet()) {
			Long suid = Long.parseLong(entry.getKey());
			CyRow row = getNodeTable().getRow(suid);
			row.set(formatColumnName(base, heatSuffix), entry.getValue().getHeat());
			row.set(formatColumnName(base, rankSuffix), entry.getValue().getRank());
		}
	}

	public String[] getAvailableOutputColumns() {
		final List<String> columns = new ArrayList<>();

		final CyTable table = this.getNodeTable();
		if (table == null) {
			return new String[0];
		}

		for (CyColumn column : getNodeTable().getColumns()) {
			if ((column.getType().equals(Double.class) || column.getType().equals(Integer.class))
					&& hasDiffusionSuffix(column.getName())) {
				columns.add(column.getName());
			}
		}
		return columns.toArray(new String[columns.size()]);
	}

	private Boolean hasDiffusionSuffix(String columnName) {
		return columnName.endsWith("_heat") || columnName.endsWith("_rank");
	}

	// Generate an available base name for diffusion output
	public String getNextAvailableColumnName(final String baseName) {

		String desiredName = baseName;
		for (int index = 1; columnsExist(desiredName); index++) {
			desiredName = formatColumnName(baseName, index);
		}
		return desiredName;
	}

	// Check if either the heat or rank exists for the given
	private Boolean columnsExist(String base) {
		final CyTable table = this.getNodeTable();
		if (table == null) {
			return false;
		}

		Boolean heatExists = this.getNodeTable().getColumn(formatColumnName(base, heatSuffix)) != null;
		Boolean rankExists = this.getNodeTable().getColumn(formatColumnName(base, rankSuffix)) != null;
		return (heatExists || rankExists);
	}

	private final CyTable getNodeTable() {
		return network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
	}

	private String formatColumnName(String base, Integer index) {
		return String.format("%s_%d", base, index);
	}

	private String formatColumnName(String base, String typeSuffix) {
		return String.format("%s%s", base, typeSuffix);
	}

	protected void showResult() {
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {
				// 1. Dock the EAST Panel
				final CytoPanel panel = swingApplication.getCytoPanel(CytoPanelName.EAST);
				panel.setState(CytoPanelState.DOCK);

				// 2. Find Diffusion Output Panel
				final int componentCount = panel.getCytoPanelComponentCount();

				int targetPanelIdx = 0;

				for(int i=0; i<componentCount; i++) {
					final Component panelComponent = panel.getComponentAt(i);
					if(panelComponent instanceof CytoPanelComponent2) {
						final CytoPanelComponent2 cp2 = (CytoPanelComponent2) panelComponent;
						final String panelId = cp2.getIdentifier();
						if(panelId != null && panelId.equals("diffusion")) {
							// Found target panel.  Force to update
							final Dimension defSize = new Dimension(300, 400);
							panelComponent.setPreferredSize(defSize);
							panelComponent.setSize(defSize);
							targetPanelIdx = i;
							break;
						}
					}
				}
				panel.setSelectedIndex(targetPanelIdx);
				panel.getThisComponent().repaint();
			}
		});
	}

	protected String createServiceError(String errorMessage) {
		return String
				.format("Oops! Could not complete diffusion. The heat diffusion service in the cloud told us something went wrong while processing your request.\n"
						+ "Here is the error message we received from the service, email the service author with this message if you need assistance.\n\n"
						+ "Error:\n %s", errorMessage);
	}

	private static final String getResultString(DiffusionResultColumns diffusionResultColumns) {
		return (diffusionResultColumns != null ? "Created result columns: ("+diffusionResultColumns.heatColumn+"),("+diffusionResultColumns.rankColumn+")" : "No result columns available");
	}

	@Override
	public <R> R getResults(Class<? extends R> type) {
		if (type.equals(String.class)){
			return (R) getResultString(diffusionResultColumns);
		}
		else if (type.equals(DiffusionResultColumns.class)){
			return (R) diffusionResultColumns;
		} else if (type.equals(DiffusionJSONResult.class)) {
			return (R) new DiffusionJSONResult(diffusionResultColumns);
		}
		return null;
	}

	public final class DiffusionJSONResult implements JSONResult{
		private final DiffusionResultColumns diffusionResultColumns;
		
		public DiffusionJSONResult(DiffusionResultColumns diffusionResultColumns) {
			this.diffusionResultColumns = diffusionResultColumns;
		}
		
		@Override
		@ExampleJSONString(value="{"
				 +  "\"heatColumn\": \"diffusion_output_heat\","
				 + " \"rankColumn\": \"diffusion_output_rank\""
				 +" }")
		public String getJSON() {
			ObjectMapper mapper = new ObjectMapper();
			try {
				return mapper.writeValueAsString(diffusionResultColumns);
			} catch (JsonProcessingException e) {
				e.printStackTrace();
				return null;
			}
		}
	}
	
	@Override
	public List<Class<?>> getResultClasses() {
		return Collections.unmodifiableList(Arrays.asList(String.class, DiffusionResultColumns.class, DiffusionJSONResult.class));
	}

}

