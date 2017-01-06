package org.cytoscape.diffusion.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.diffusion.internal.client.NodeAttributes;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class DiffusionTableFactory {

	private String heatSuffix = "_heat";
	private String rankSuffix = "_rank";

	CyApplicationManager manager;

	public DiffusionTableFactory(final CyApplicationManager manager) {
		this.manager = manager;
	}

	public DiffusionTable createTable(String base) {
		System.out.println(base);
		System.out.println(base.length());
		if (hasDiffusionSuffix(base)) {
			base = base.substring(0, base.length() - heatSuffix.length());
			System.out.println("New base");
			System.out.println(base);
		}
		return new DiffusionTable(manager, formatColumnName(base, rankSuffix), formatColumnName(base, heatSuffix));
	}

	public void writeColumns(String base, Map<String, NodeAttributes> nodes) {
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

	private void createColumns(String base) {
		System.out.println("Creating columns");
		System.out.println(formatColumnName(base, rankSuffix));

		final CyTable table = this.getNodeTable();
		if (table == null) {
			return;
		}

		table.createColumn(formatColumnName(base, rankSuffix), Integer.class, false, 0);
		table.createColumn(formatColumnName(base, heatSuffix), Double.class, false, 0.0);
		System.out.println(getNodeTable().getColumn(formatColumnName(base, rankSuffix)));
		System.out.println(getNodeTable().getColumns());
	}

	// Generate an available base name for diffusion output
	public String getNextAvailableColumnName(String baseName) {
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
		final CyNetwork curNet = manager.getCurrentNetwork();
		if (curNet != null) {
			return curNet.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		} else {
			return null;
		}
	}

	private String formatColumnName(String base, Integer index) {
		return String.format("%s_%d", base, index);
	}

	private String formatColumnName(String base, String typeSuffix) {
		return String.format("%s%s", base, typeSuffix);
	}

}
