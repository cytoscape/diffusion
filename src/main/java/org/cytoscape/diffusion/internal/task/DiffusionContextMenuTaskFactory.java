package org.cytoscape.diffusion.internal.task;

import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.diffusion.internal.ViewWriterFactoryManager;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyEdge;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTableUtil;
import org.cytoscape.task.AbstractNetworkViewTaskFactory;
import org.cytoscape.task.AbstractNodeViewTaskFactory;
import org.cytoscape.task.EdgeViewTaskFactory;
import org.cytoscape.task.NetworkViewTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.View;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TunableSetter;

public class DiffusionContextMenuTaskFactory extends AbstractNodeViewTaskFactory 
	implements NetworkViewTaskFactory {

	private final ViewWriterFactoryManager factoryManager;
	private final DiffusionNetworkManager networkManager;
	private final OutputPanel outputPanel;
	private final CySwingApplication swingApplication;
	private final CyApplicationManager appManager;
	private final DiffusionServiceClient client;
	private final TunableSetter setter;
	
	private Boolean withOptions = false;

	public DiffusionContextMenuTaskFactory(DiffusionNetworkManager networkManager, OutputPanel outputPanel,
			final ViewWriterFactoryManager factoryManager, final CySwingApplication swingApplication,
			final CyApplicationManager appManager, final DiffusionServiceClient client, final TunableSetter setter) {
		this(networkManager, outputPanel, factoryManager, swingApplication, appManager, client, setter, false);
	}

	public DiffusionContextMenuTaskFactory(DiffusionNetworkManager networkManager, OutputPanel outputPanel,
			final ViewWriterFactoryManager factoryManager, final CySwingApplication swingApplication,
			final CyApplicationManager appManager, final DiffusionServiceClient client, final TunableSetter setter,
			final Boolean withOptions) {
		this.networkManager = networkManager;
		this.outputPanel = outputPanel;
		this.factoryManager = factoryManager;
		this.swingApplication = swingApplication;
		this.appManager = appManager;
		this.client = client;
		this.setter = setter;
		this.withOptions = withOptions;
	}


	@Override
	public TaskIterator createTaskIterator(CyNetworkView networkView) {
		return create();
	}

	@Override
	public TaskIterator createTaskIterator(View<CyNode> nodeView, CyNetworkView networkView) {
		return create();
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

	private final TaskIterator create() {
		final CyNetworkViewWriterFactory writerFactory = this.factoryManager.getCxFactory();

		if (writerFactory == null) {
			throw new IllegalStateException(
					"CXWriterFactory is not available.  " + "Please make sure you have proper dependencies");
		}

		if(withOptions) {
			return new TaskIterator(new DiffuseSelectedWithOptionsTask(networkManager, writerFactory, outputPanel, swingApplication,
				appManager, client, setter));
		}
		
		return new TaskIterator(new DiffuseSelectedTask(networkManager, writerFactory, outputPanel, swingApplication,
				appManager, client, setter));

	}



}
