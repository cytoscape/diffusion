package org.cytoscape.diffusion.internal.task;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.diffusion.internal.ViewWriterFactoryManager;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class DiffusionContextMenuTaskFactory extends AbstractNodeViewTaskFactory 
	implements NetworkViewTaskFactory {

	private final ViewWriterFactoryManager factoryManager;
	private final OutputPanel outputPanel;
	private final CySwingApplication swingApplication;
	private final CyApplicationManager appManager;
	private final DiffusionServiceClient client;
	private final TunableSetter setter;
	private final DiffusionTableManager tableManager;
	
	private Boolean withOptions = false;

	public DiffusionContextMenuTaskFactory(CyServiceRegistrar registrar, DiffusionTableManager tableManager, OutputPanel outputPanel,
			final ViewWriterFactoryManager factoryManager, final CySwingApplication swingApplication,
			final CyApplicationManager appManager, final DiffusionServiceClient client, final TunableSetter setter) {
		this(registrar, tableManager, outputPanel, factoryManager, swingApplication, appManager, client, setter, false);
	}

	public DiffusionContextMenuTaskFactory(CyServiceRegistrar registrar, DiffusionTableManager tableManager, OutputPanel outputPanel,
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
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return create(networkView.getModel());
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return create(networkView.getModel());
	}

	@Override
	public boolean isReady(View<CyNode> nodeView, CyNetworkView networkView) {
		return isReady(networkView);
	}
	
	@Override
	public boolean isReady(CyNetworkView networkView) {
		if (networkView == null) {
			return false;
		}

		// Make sure we've got something selected
		List<CyNode> selNodes = CyTableUtil.getNodesInState(networkView.getModel(), CyNetwork.SELECTED, true);
		if (selNodes != null && selNodes.size() > 0) {
			return true;
		}

		return false;
	}

	private final TaskIterator create(CyNetwork network) {
		
		final CyNetworkViewWriterFactory writerFactory = this.factoryManager.getCxFactory();

		if (writerFactory == null) {
			throw new IllegalStateException(
					"CXWriterFactory is not available.  " + "Please make sure you have proper dependencies");
		}

		if(withOptions) {
			return new TaskIterator(new DiffuseSelectedWithOptionsTask(tableManager, network, writerFactory, outputPanel, swingApplication,
				appManager, client, setter));
		}
		
		return new TaskIterator(new DiffuseSelectedTask(tableManager, network, writerFactory, outputPanel, swingApplication,
				appManager, client, setter));

	}



}
