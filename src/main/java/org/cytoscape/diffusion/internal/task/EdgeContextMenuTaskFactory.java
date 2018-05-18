package org.cytoscape.diffusion.internal.task;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.diffusion.internal.ViewWriterFactoryManager;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractEdgeViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class EdgeContextMenuTaskFactory extends AbstractEdgeViewTaskFactory {

	
	private final ViewWriterFactoryManager factoryManager;
	private final OutputPanel outputPanel;
	private final CySwingApplication swingApplication;
	private final CyApplicationManager appManager;
	private final DiffusionServiceClient client;
	private final TunableSetter setter;
	private final Boolean withOptions;
	private final DiffusionTableManager tableManager;

	public EdgeContextMenuTaskFactory(CyServiceRegistrar registrar, DiffusionTableManager tableManager, OutputPanel outputPanel,
			final ViewWriterFactoryManager factoryManager, final CySwingApplication swingApplication,
			final CyApplicationManager appManager, final DiffusionServiceClient client, final TunableSetter setter,
			final Boolean withOptions) {
		this.outputPanel = outputPanel;
		this.factoryManager = factoryManager;
		this.swingApplication = swingApplication;
		this.appManager = appManager;
		this.client = client;
		this.setter = setter;
		this.withOptions = withOptions;
		this.tableManager = tableManager;
	}

	@Override
	public boolean isReady(View<CyEdge> edgeView, CyNetworkView networkView) {
		if (!super.isReady(edgeView, networkView))
			return false;

		// Make sure we've got something selected
		List<CyNode> selNodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		if (selNodes != null && selNodes.size() > 0) {
			return true;
		}

		return false;
	}

	@Override
	public TaskIterator createTaskIterator(View<CyEdge> edgeView, CyNetworkView networkView) {
		return create(networkView.getModel());
	}

	private final TaskIterator create(final CyNetwork network) {
		final CyNetworkViewWriterFactory writerFactory = this.factoryManager.getCxFactory();

		if (writerFactory == null) {
			throw new IllegalStateException(
					"CXWriterFactory is not available.  " + "Please make sure you have proper dependencies");
		}

		if (withOptions) {
			return new TaskIterator(new DiffuseSelectedWithOptionsTask(tableManager, network, writerFactory, outputPanel,
					swingApplication, appManager, client, setter));
		}

		return new TaskIterator(new DiffuseSelectedTask(tableManager, network, writerFactory, outputPanel, swingApplication,
				appManager, client, setter));

	}
}
