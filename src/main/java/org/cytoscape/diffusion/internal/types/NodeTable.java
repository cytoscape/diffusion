
package org.cytoscape.diffusion.internal;

import java.util.List;
import java.util.Map;
import java.util.ArrayList;
import java.io.ByteArrayOutputStream;
import java.io.IOException;

import java.util.Collection;
import java.util.Collections;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyColumn;

public class NodeTable {

  CyTable nodeTable;

  public NodeTable(CyTable nodeTable) {
    this.nodeTable = nodeTable;
  }

  public CyTable getTable() {
    return this.nodeTable;
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

  public String[] getAvaiableOutputColumns() {
     ArrayList<String> columns = new ArrayList();
     for (CyColumn column : this.nodeTable.getColumns()) {
       if (column.getType().equals(Double.class)) {
         columns.add(column.getName());
       }
     }
     return columns.toArray(new String[columns.size()]);
  }

  public void selectNodesOverThreshold(String outputColumn, Double threshold) {
    for (CyRow row : nodeTable.getAllRows()) {
      Double heatValue = row.get(outputColumn, Double.class);
      if (heatValue >= threshold) {
        row.set(CyNetwork.SELECTED, Boolean.TRUE);
      } else {
        row.set(CyNetwork.SELECTED, Boolean.FALSE);
      }

    }
  }

  public Double getThreshold(String columnName, Integer percentile) {
    double percentage = percentile / 100.0;
    List<Double> heats = this.nodeTable.getColumn(columnName).getValues(Double.class);
    Integer percentileIndex = new Double(heats.size() * percentage).intValue();
    Collections.sort(heats);
    return heats.get(getFirstNonZeroHeat(heats, percentileIndex));
  }

  private Integer getFirstNonZeroHeat(List<Double> heats, Integer index) {
    while (heats.get(index) == 0) {
      index += 1;
    }
    return index;
  }

  public Integer getThresholdIndex(String columnName, Integer percentile) {
    double percentage = percentile / 100.0;
    List<Double> heats = this.nodeTable.getColumn(columnName).getValues(Double.class);
    Integer percentileIndex = new Double(heats.size() * percentage).intValue();
    Collections.sort(heats);
    return getFirstNonZeroHeat(heats, percentileIndex);
  }



}
