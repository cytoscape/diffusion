package org.cytoscape.diffusion.internal;

import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

    private static final String STYLES = "/styles.xml";

	@Override
	public void start(BundleContext context) throws Exception {

		// OSGi service listener to import dependency dynamically
		final ViewWriterFactoryManager viewWriterManager = new ViewWriterFactoryManager();
		registerServiceListener(context, viewWriterManager, "addFactory", "removeFactory",
				CyNetworkViewWriterFactory.class);
		
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
		
		final CySwingApplication swingApplication = getService(context, CySwingApplication.class);
		
		DiffusionTaskFactory diffusionTaskFactory = new DiffusionTaskFactory(diffusionNetworkManager, outputPanel, viewWriterManager, swingApplication);
		Properties diffusionTaskFactoryProps = new Properties();
	    diffusionTaskFactoryProps.setProperty("title", "Diffuse Selected Nodes");
	    registerService(context, diffusionTaskFactory, NodeViewTaskFactory.class, diffusionTaskFactoryProps);
	}


}
