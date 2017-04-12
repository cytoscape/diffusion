
package org.cytoscape.diffusion.internal.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
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
	
	public void setCurrentDiffusionResult(DiffusionResult result) {
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
					&& hasDiffusionSuffix(column.getName())) {
				columns.add(column.getName());
			}
		}
		return columns.toArray(new String[columns.size()]);
	}
	
	private Boolean hasDiffusionSuffix(String columnName) {
		return columnName.endsWith("_heat") || columnName.endsWith("_rank");
	}

	// Get the first row that matches the given value at the given column name
	private final CyRow getFirstMatchingRow(final String columnName, Object value) {
		final CyTable localTable = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		final CyColumn column = localTable.getColumn(columnName);
		if(column == null) {
			throw new IllegalArgumentException("Local column does not exist: " + columnName);
		}
		
		final Collection<CyRow> matchedRows = localTable.getMatchingRows(columnName, value);
		if (!matchedRows.isEmpty()) {
			return matchedRows.iterator().next();
		} else {
			return null;
		}
	}
}
