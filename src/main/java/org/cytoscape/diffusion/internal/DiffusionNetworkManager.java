package org.cytoscape.diffusion.internal;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;

class DiffusionNetworkManager {

  CyApplicationManager appManager;
  DialogTaskManager taskManager;
  NewNetworkSelectedNodesOnlyTaskFactory networkFactory;

  DiffusionNetworkManager(
    CyApplicationManager appManager,
    DialogTaskManager taskManager,
    NewNetworkSelectedNodesOnlyTaskFactory networkFactory
  ) {
    this.appManager = appManager;
    this.taskManager = taskManager;
    this.networkFactory = networkFactory;
  }

  public void createSubnet() {
    taskManager.execute(this.networkFactory.createTaskIterator(getNetwork()));
  }

  public List<CyNode> getSelectedNodes() {
    return CyTableUtil.getNodesInState(getNetwork(), CyNetwork.SELECTED, true);
  }

  public void setNodeHeats() {
    this.ensureEmptyTableExists("diffusion_input");
    for (CyNode node : getSelectedNodes()) {
      Long suid = node.getSUID();
      CyRow row = getNodeTable().getRow(suid);
      row.set("diffusion_input", 1.0);
    }
  }

  public void selectNodesOverThreshold(String heatColumn, Double threshold) {
   for (CyRow row : getNodeTable().getAllRows()) {
     Double heatValue = row.get(heatColumn, Double.class);
     if (heatValue >= threshold) {
       row.set(CyNetwork.SELECTED, Boolean.TRUE);
     } else {
       row.set(CyNetwork.SELECTED, Boolean.FALSE);
     }
   }
 }

 private void ensureEmptyTableExists(String tableName) {
  getNodeTable().deleteColumn(tableName);
  getNodeTable().createColumn(tableName, Double.class, false, 0.0);
}

 public CyNetwork getNetwork() {
   return appManager.getCurrentNetwork();
 }

 public CyTable getNodeTable() {
   return getNetwork().getDefaultNodeTable();
 }

}
