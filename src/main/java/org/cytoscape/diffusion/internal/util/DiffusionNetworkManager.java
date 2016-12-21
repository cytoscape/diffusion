package org.cytoscape.diffusion.internal.util;

import java.util.Collection;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.diffusion.internal.task.EmptyTaskMonitor;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;

public class DiffusionNetworkManager {

	private CyApplicationManager appManager;
	DialogTaskManager taskManager;
	NewNetworkSelectedNodesOnlyTaskFactory networkFactory;
	NewNetworkSelectedNodesAndEdgesTaskFactory nFactory;

	public DiffusionNetworkManager(CyApplicationManager appManager, DialogTaskManager taskManager,
			NewNetworkSelectedNodesOnlyTaskFactory networkFactory,
			NewNetworkSelectedNodesAndEdgesTaskFactory nFactory) {
		this.appManager = appManager;
		this.taskManager = taskManager;
		this.networkFactory = networkFactory;
		this.nFactory = nFactory;
	}

	public CyNetworkView createSubnet() {
		
		TaskIterator taskIterator = this.networkFactory.createTaskIterator(getNetwork());
		// final TaskIterator finalIterator = new TaskIterator();
		AbstractNetworkCollectionTask viewTask = null;
		try {
			while (taskIterator.hasNext()) {
				final Task task = taskIterator.next();
				task.run(new EmptyTaskMonitor());
				// finalIterator.append(task);
				if (task instanceof ObservableTask) {
					viewTask = (AbstractNetworkCollectionTask) task;
				}
			}
		} catch (Exception e) {
		}
		System.out.println(viewTask);
		// taskManager.execute(finalIterator);
		System.out.println(((ObservableTask) viewTask).getResults(Collection.class));
		final Collection<?> result = ((ObservableTask) viewTask).getResults(Collection.class);
		return ((CyNetworkView) result.iterator().next());
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

	public CyNetworkView getNetworkView() {
		return appManager.getCurrentNetworkView();
	}

	public CyNetwork getNetwork() {
		return appManager.getCurrentNetwork();
	}

	public CyTable getNodeTable() {
		return getNetwork().getDefaultNodeTable();
	}

}
