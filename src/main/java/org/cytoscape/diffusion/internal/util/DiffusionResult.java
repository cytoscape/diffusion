package org.cytoscape.diffusion.internal.util;

import java.util.Collection;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

public class DiffusionResult {
	
	private final String rankName;
	private final String heatName;
	
	private final CyNetwork network;

	
	public DiffusionResult(final CyNetwork network, final String rankName, final String heatName) {
		this.rankName = rankName;
		this.heatName = heatName;
		this.network = network;
	}

	public String getHeatColumnName() {
		return this.heatName;
	}
	
	public String getRankColumnName() {
		return this.rankName;
	}

	// Get the heat of the hotest node
	public Double getMaxHeat() {
		return rankToHeat(1);
	}

	// Get the heat of the coldests node
	public Double getMinHeat() {
		return rankToHeat(getMaxRank());
	}

	// NOTE: This should always be 1
	// Get the mimimum rank of all the nodes (rank of the coldest node)
	public Integer getMinRank() {
		return 1;
	}

	// NOTE: This should always be equal to the number of nodes
	// Get the maximum rank of all the nodes (rank of the hottest node)
	public Integer getMaxRank() {
		return this.network.getNodeCount();
	}

	// Get the heat of the node with the given rank
	public Double rankToHeat(Integer rank) {
		return getRowForRank(rank).get(heatName, Double.class);
	}

	// Get the heat of the first node with the given heat
	public Integer heatToRank(Double heat) {
		return getRowForHeat(heat).get(rankName, Integer.class);
	}

	// Get the row for first node with the given heat
	public CyRow getRowForHeat(Double heat) {
		return getFirstMatchingRow(heatName, heat);
	}

	// Get the row for the node with the given rank
	public CyRow getRowForRank(Integer rank) {
		return getFirstMatchingRow(rankName, rank);
	}
	
	public CyRow getFirstMatchingRow(String columnName, Object value) {
		final CyTable table = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		final Collection<CyRow> matchedRows = table.getMatchingRows(columnName, value);
		if (matchedRows.isEmpty()) {
			return null;
		} else {
			return matchedRows.iterator().next();
		}
	}
	
}
