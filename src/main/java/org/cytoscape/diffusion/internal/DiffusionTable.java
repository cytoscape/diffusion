
package org.cytoscape.diffusion.internal;

import java.util.List;
import java.util.ArrayList;
import java.util.Collection;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;

class DiffusionTable {

  String rankColumnName, heatColumnName;

  CyTable nodeTable;

  DiffusionTable(CyTable nodeTable, String rankColumnName, String heatColumnName) {
    this.nodeTable = nodeTable;
    this.rankColumnName = rankColumnName;
    this.heatColumnName = heatColumnName;
  }

  //Get the heat of the hotest node
  public Double getMaxHeat() {
    return rankToHeat(nodeTable.getRowCount());
  }

  //Get the heat of the coldests node
  public Double getMinHeat() {
    return rankToHeat(0);
  }

  //NOTE: This should always be 0
  //Get the mimimum rank of all the nodes (rank of the coldest node)
  public Integer getMinRank() {
    return 0;
  }

  //NOTE: This should always be equal to the number of nodes
  //Get the maximum rank of all the nodes (rank of the hottest node)
  public Integer getMaxRank() {
    return nodeTable.getRowCount();
  }

  //Get the heat of the node with the given rank
  public Double rankToHeat(Integer rank) {
    return getRowForRank(rank).get(heatColumnName, Double.class);
  }

  //Get the heat of the first node with the given heat
  public Integer heatToRank(Double heat) {
    return getRowForHeat(heat).get(rankColumnName, Integer.class);
  }

  //Get the row for first node with the given heat
  public CyRow getRowForHeat(Double heat) {
    return getFirstMatchingRow(heatColumnName, heat);
  }

  //Get the row for the node with the given rank
  public CyRow getRowForRank(Integer rank) {
    return getFirstMatchingRow(rankColumnName, rank);
  }

  //Get the first row that matches the given value at the given column name
  public CyRow getFirstMatchingRow(String columnName, Object value) {
    Collection<CyRow> matchedRows = nodeTable.getMatchingRows(columnName, value);
    if (matchedRows.size() != 0) {
      List<CyRow> rowList = new ArrayList(matchedRows);
      return rowList.get(0);
    } else {
      return null;
    }
  }

}
