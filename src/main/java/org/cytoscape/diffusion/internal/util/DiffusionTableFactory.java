package org.cytoscape.diffusion.internal.util;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.cxio.aspects.datamodels.ATTRIBUTE_DATA_TYPE;
import org.cxio.aspects.datamodels.NodeAttributesElement;
import org.cxio.core.interfaces.AspectElement;
import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.diffusion.internal.client.NodeAttributes;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class DiffusionTableFactory {

	private static final String heatSuffix = "_heat";
	private static final String rankSuffix = "_rank";
	
	private static final String ORIGINAL_HEAT_ATTR_NAME = "diffusion_output_heat";
	private static final String ORIGINAL_RANK_ATTR_NAME = "diffusion_output_rank";

	private final CyApplicationManager manager;

	
	public DiffusionTableFactory(final CyApplicationManager manager) {
		this.manager = manager;
	}


	public DiffusionTable createTable(final String baseColumnName) {
		String base = baseColumnName;
		if (hasDiffusionSuffix(baseColumnName)) {
			base = baseColumnName.substring(0, baseColumnName.length() - heatSuffix.length());
		}
		
		// TODO: avoid calling formatColumnName everywhere...
		return new DiffusionTable(manager, formatColumnName(base, rankSuffix), formatColumnName(base, heatSuffix));
	}


	public void writeColumns(final String baseColumnName, final List<AspectElement> nodeAttrs) {
		
		final CyTable table = this.getNodeTable();
		if (table == null) {
			throw new IllegalStateException("Table does not exists yet.");
		}
	
		// This is a HACK.  Create pre-defined columns first.
		createColumns(baseColumnName);
		
		final String rankColumnName = formatColumnName(baseColumnName, rankSuffix);
		final String heatColumnName = formatColumnName(baseColumnName, heatSuffix);

		for (final AspectElement attr : nodeAttrs) {
			
			final NodeAttributesElement nodeAttr = (NodeAttributesElement) attr;
			final Long suid = nodeAttr.getPropertyOf().get(0);
			final CyRow row = getNodeTable().getRow(suid);
			
			final String attrName = nodeAttr.getName();
			final String valueStr = nodeAttr.getValue();
			
			if(attrName.equals(ORIGINAL_HEAT_ATTR_NAME)) {
				row.set(heatColumnName, parseValue(ATTRIBUTE_DATA_TYPE.DOUBLE, valueStr));
			} else if(attrName.equals(ORIGINAL_RANK_ATTR_NAME)) {
				row.set(rankColumnName, parseValue(ATTRIBUTE_DATA_TYPE.INTEGER, valueStr));				
			} else {
				continue;
			}
		}
	}
	
	private final Class<?> toClass(final ATTRIBUTE_DATA_TYPE type) {
        if (type.equals(ATTRIBUTE_DATA_TYPE.STRING)) {
            return String.class;
        } else if (type.equals(ATTRIBUTE_DATA_TYPE.INTEGER)) {
            return Integer.class;
        } else if (type.equals(ATTRIBUTE_DATA_TYPE.LONG)) {
            return Long.class;
        } else if (type.equals(ATTRIBUTE_DATA_TYPE.DOUBLE) || type.equals(ATTRIBUTE_DATA_TYPE.FLOAT)) {
            return Double.class;
        } else if (type.equals(ATTRIBUTE_DATA_TYPE.BOOLEAN)) {
            return Boolean.class;
        } else {
            throw new IllegalArgumentException("don't know how to deal with type '" + type.toString() + "'");
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

	private void createColumns(String base) {
		System.out.println(formatColumnName(base, rankSuffix));

		final CyTable table = this.getNodeTable();
		if (table == null) {
			return;
		}

		table.createColumn(formatColumnName(base, rankSuffix), Integer.class, false);
		table.createColumn(formatColumnName(base, heatSuffix), Double.class, false, 0.0);
//		System.out.println(getNodeTable().getColumn(formatColumnName(base, rankSuffix)));
//		System.out.println(getNodeTable().getColumns());
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
