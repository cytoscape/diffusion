package org.cytoscape.diffusion.internal;

import java.io.File;
import java.util.List;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskManager;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.swing.DialogTaskManager;
import org.cytoscape.application.CyApplicationManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

    private static final String STYLES = "/styles.xml";

	@Override
	public void start(BundleContext context) throws Exception {
		DialogTaskManager taskManager = getService(context, DialogTaskManager.class);
		NewNetworkSelectedNodesAndEdgesTaskFactory nFactory = getService(context, NewNetworkSelectedNodesAndEdgesTaskFactory.class);
		NewNetworkSelectedNodesOnlyTaskFactory networkFactory = getService(context, NewNetworkSelectedNodesOnlyTaskFactory.class);
		CyApplicationManager cyApplicationManagerService = getService(context, CyApplicationManager.class);
		DiffusionNetworkManager diffusionNetworkManager = new DiffusionNetworkManager(cyApplicationManagerService, taskManager, networkFactory, nFactory);

        LoadVizmapFileTaskFactory vizmapLoader = getService(context, LoadVizmapFileTaskFactory.class);
        System.out.println(getClass());
        System.out.println(getClass().getResource("/styles.xml"));
        Set<VisualStyle> styles = vizmapLoader.loadStyles(getClass().getResource(STYLES).openStream());
        System.out.println(styles);

		OutputPanel outputPanel = new OutputPanel(diffusionNetworkManager, styles);
		registerService(context, outputPanel, CytoPanelComponent.class, new Properties());
        CyNetworkViewWriterFactory writerFactory =  getService(context, CyNetworkViewWriterFactory.class, "(id=cxNetworkWriterFactory)");
		DiffusionTaskFactory diffusionTaskFactory = new DiffusionTaskFactory(diffusionNetworkManager, writerFactory, outputPanel);
		Properties diffusionTaskFactoryProps = new Properties();
	    diffusionTaskFactoryProps.setProperty("title", "Diffuse Selected Nodes");
	    registerService(context, diffusionTaskFactory, NodeViewTaskFactory.class, diffusionTaskFactoryProps);
	}


}
