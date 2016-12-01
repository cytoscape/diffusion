
package org.cytoscape.diffusion.internal;

import java.util.List;
import java.util.Map;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;

public class NodeTable {

  CyTable nodeTable;

  public NodeTable(CyTable nodeTable) {
    this.nodeTable = nodeTable;
  }

  //Ensure any previous inputs are deleted and create a new input table
  private void ensureEmptyTableExists(String tableName) {
    this.nodeTable.deleteColumn(tableName);
    this.nodeTable.createColumn(tableName, Double.class, false, 0.0);
  }

  //Set every selected node to true in the input table
  public void setInitialHeats(List<CyNode> selectedNodes) {
    this.ensureEmptyTableExists("diffusion_input");
    for (CyNode node : selectedNodes) {
      Long suid = node.getSUID();
      CyRow row = this.nodeTable.getRow(suid);
      row.set("diffusion_input", 1.0);
    }
  }

  //Sets the output heats on a column and returns the name of that column
  public String setOutputHeats(HeatMap heatMap) {
    String columnName = this.getAvailableColumnName("diffusion_output");
    this.ensureEmptyTableExists(columnName);
    for (Map.Entry<String, Double> entry : heatMap.getHeats()) {
    Long suid = Long.parseLong(entry.getKey());
    CyRow row = this.nodeTable.getRow(suid);
    row.set(columnName, entry.getValue());
    }
    return columnName;
  }

  public String getAvailableColumnName(String baseName) {
    String desiredName = baseName;
    Integer numericSuffix = 1;
    while(this.nodeTable.getColumn(desiredName) != null) {
      desiredName = String.format("%s_%d", baseName, numericSuffix);
      numericSuffix += 1;
    }
    return desiredName;
  }

  public void selectNodesOverThreshold(Double threshold) {
    for (CyRow row : nodeTable.getAllRows()) {
      Double heatValue = row.get("diffusion_output", Double.class);
      if (heatValue >= threshold) {
        row.set(CyNetwork.SELECTED, Boolean.TRUE);
      } else {
        row.set(CyNetwork.SELECTED, Boolean.FALSE);
      }

    }
  }

}
