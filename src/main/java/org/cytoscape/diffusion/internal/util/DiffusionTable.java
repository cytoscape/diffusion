
package org.cytoscape.diffusion.internal.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;

public class DiffusionTable {

	private final CyNetwork network;
	private final CyTable table;
	
	private final Map<String, DiffusionResult> runs;
	
	private DiffusionResult currentResult;
	

	public DiffusionTable(final CyNetwork network) {
		if(network == null) {
			throw new NullPointerException("Network cannot be null.");
		}
		
		this.network = network;
		this.table = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		this.runs = new HashMap<>();
	}
	
	public CyNetwork getAssociatedNetwork() {
		return network;
	}

	
	public DiffusionResult getCurrentResult() {
		return currentResult;
	}
	
	public void setCurrentDiffusionResult(final String runId) {
		final DiffusionResult result = runs.get(runId);
		if(result == null) {
			throw new IllegalArgumentException("No result is available for " + runId);
		}
		this.currentResult = result;
	}
	
	public void setDiffusionResult(String runId, DiffusionResult result) {
		this.currentResult = result;
		this.runs.put(runId, result);
	}
	
	public String[] getAvailableOutputColumns() {
		final List<String> columns = new ArrayList<>();

		if (table == null) {
			return new String[0];
		}

		for (CyColumn column : table.getColumns()) {
			if ((column.getType().equals(Double.class) || column.getType().equals(Integer.class))
					&& column.getName().startsWith("diffusion_") && hasDiffusionSuffix(column.getName())) {
				columns.add(column.getName());
			}
		}
		return columns.toArray(new String[columns.size()]);
	}
	
	private Boolean hasDiffusionSuffix(String columnName) {
		return columnName.endsWith("_heat") || columnName.endsWith("_rank");
	}
}
