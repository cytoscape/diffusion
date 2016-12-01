package org.cytoscape.diffusion.internal;

import java.util.Properties;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
		DialogTaskManager dialogTaskManager = getService(context, DialogTaskManager.class);
		NewNetworkSelectedNodesAndEdgesTaskFactory networkFactory = getService(context, NewNetworkSelectedNodesAndEdgesTaskFactory.class);
		CyApplicationManager cyApplicationManagerService = getService(context, CyApplicationManager.class);
		OutputPanel panel = new OutputPanel(cyApplicationManagerService, dialogTaskManager, networkFactory);
    registerService(context, panel, CytoPanelComponent.class, new Properties());
    CyNetworkViewWriterFactory writerFactory =  getService(context, CyNetworkViewWriterFactory.class, "(id=cxNetworkWriterFactory)");
		DiffusionTaskFactory diffusionTaskFactory = new DiffusionTaskFactory(writerFactory, panel);
		Properties diffusionTaskFactoryProps = new Properties();
	  diffusionTaskFactoryProps.setProperty("title", "Diffuse Selected Nodes");
	  registerService(context, diffusionTaskFactory, NodeViewTaskFactory.class, diffusionTaskFactoryProps);
	}


}
