package org.cytoscape.diffusion.internal;

import java.util.List;
import java.util.ArrayList;
import java.util.Map;

import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyRow;

class DiffusionTableFactory {

  private String heatSuffix = "_heat";
  private String rankSuffix = "_rank";

  CyTable nodeTable;

  DiffusionTableFactory(CyTable nodeTable) {
    this.nodeTable = nodeTable;
  }

  public DiffusionTable createTable(String base) {
    return new DiffusionTable(nodeTable,
                              formatColumnName(base, rankSuffix),
                              formatColumnName(base, heatSuffix));
  }

  public void writeColumns(String base, Map<String, NodeAttributes> nodes) {
    createColumns(base);
    for (Map.Entry<String, NodeAttributes> entry : nodes.entrySet()) {
      Long suid = Long.parseLong(entry.getKey());
      CyRow row = nodeTable.getRow(suid);
      System.out.println(suid);
      System.out.println(entry.getValue().getHeat());
      System.out.println(entry.getValue().getRank());
      row.set(formatColumnName(base, heatSuffix), entry.getValue().getHeat());
      row.set(formatColumnName(base, rankSuffix), entry.getValue().getRank());
    }
  }

  public String[] getAvailableColumns() {
    List<String> columns = new ArrayList();
    for (CyColumn column : nodeTable.getColumns()) {
      if (column.getType().equals(Double.class) || column.getType().equals(Integer.class)) {
        columns.add(column.getName());
      }
    }
    return columns.toArray(new String[columns.size()]);
  }

  private void createColumns(String base) {
    nodeTable.createColumn(formatColumnName(base, rankSuffix), Integer.class, false, 0);
    nodeTable.createColumn(formatColumnName(base, heatSuffix), Double.class, false, 0.0);
  }

  //Generate an available base name for diffusion output
  public String getNextAvailableColumnName(String baseName) {
    String desiredName = baseName;
    for(int index=1; columnsExist(desiredName); index++) {
      desiredName = formatColumnName(baseName, index);
    }
    return desiredName;
  }

  //Check if either the heat or rank exists for the given
  private Boolean columnsExist(String base) {
    Boolean heatExists = this.nodeTable.getColumn(formatColumnName(base, heatSuffix)) != null;
    Boolean rankExists = this.nodeTable.getColumn(formatColumnName(base, rankSuffix)) != null;
    return (heatExists || rankExists);
  }

  private String formatColumnName(String base, Integer index) {
    return String.format("%s_%d", base, index);
  }

  private String formatColumnName(String base, String typeSuffix) {
    return String.format("%s_%s", base, typeSuffix);
  }

}
