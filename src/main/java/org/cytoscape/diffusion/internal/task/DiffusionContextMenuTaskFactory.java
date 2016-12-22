package org.cytoscape.diffusion.internal.task;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.diffusion.internal.ViewWriterFactoryManager;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNode;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;

public class DiffusionContextMenuTaskFactory extends AbstractNodeViewTaskFactory {

	private final ViewWriterFactoryManager factoryManager;
	private final DiffusionNetworkManager networkManager;
	private final OutputPanel outputPanel;
	private final CySwingApplication swingApplication;
	private final CyApplicationManager appManager;
	private final DiffusionServiceClient client;

	public DiffusionContextMenuTaskFactory(DiffusionNetworkManager networkManager, OutputPanel outputPanel,
			final ViewWriterFactoryManager factoryManager, final CySwingApplication swingApplication,
			final CyApplicationManager appManager, final DiffusionServiceClient client) {
		this.networkManager = networkManager;
		this.outputPanel = outputPanel;
		this.factoryManager = factoryManager;
		this.swingApplication = swingApplication;
		this.appManager = appManager;
		this.client = client;
	}

	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		final CyNetworkViewWriterFactory writerFactory = this.factoryManager.getCxFactory();

		if (writerFactory == null) {
			throw new IllegalStateException(
					"CXWriterFactory is not available.  " + "Please make sure you have proper dependencies");
		}

		return new TaskIterator(new DiffuseSelectedTask(networkManager, writerFactory, outputPanel, swingApplication, appManager, client));
	}

}
