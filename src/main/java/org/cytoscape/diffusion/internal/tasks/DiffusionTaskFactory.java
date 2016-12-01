package org.cytoscape.diffusion.internal;

import org.cytoscape.io.write.CyWriter;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.work.TaskIterator;

public class DiffusionTaskFactory extends AbstractNodeViewTaskFactory {

  private CyNetworkViewWriterFactory writerFactory;
  private OutputPanel panel;

	public DiffusionTaskFactory(CyNetworkViewWriterFactory writerFactory, OutputPanel panel) {
		this.writerFactory = writerFactory;
    this.panel = panel;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return new TaskIterator( new DiffuseSelectedTask(nodeView, networkView, writerFactory, panel) );
	}

}
