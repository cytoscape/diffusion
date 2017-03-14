package org.cytoscape.diffusion.internal;

import static org.cytoscape.work.ServiceProperties.*;
import static org.cytoscape.work.ServiceProperties.PREFERRED_ACTION;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;

import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.task.DiffusionContextMenuTaskFactory;
import org.cytoscape.diffusion.internal.task.DiffusionTaskFactory;
import org.cytoscape.diffusion.internal.task.EdgeContextMenuTaskFactory;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.TaskFactory;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	private static final String DIFFUSION_MENU = "Tools.Diffuse[2100]";
    private static final String STYLES = "/styles.xml";

	@Override
	public void start(BundleContext context) throws Exception {

		// OSGi service listener to import dependency dynamically
		final ViewWriterFactoryManager viewWriterManager = new ViewWriterFactoryManager();
		registerServiceListener(context, viewWriterManager, "addFactory", "removeFactory",
				CyNetworkViewWriterFactory.class);
		
		final TunableSetter tunableSetterServiceRef = getService(context,TunableSetter.class);
		
		VisualMappingManager vmm = getService(context, VisualMappingManager.class);
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
        
        // Create service client instance
		@SuppressWarnings("unchecked")
		final CyProperty<Properties> props = getService(context, CyProperty.class,
				"(cyPropertyName=cytoscape3.props)");
		
		final DiffusionServiceClient client;
		final Object serviceUrlProp = props.getProperties().get("diffusion.url");
		if(serviceUrlProp != null) {
        		client = new DiffusionServiceClient(serviceUrlProp.toString());
        } else {
        		client = new DiffusionServiceClient();
        }

		OutputPanel outputPanel = new OutputPanel(diffusionNetworkManager, styles, cyApplicationManagerService, vmm);
		registerAllServices(context, outputPanel, new Properties());
		
		final CySwingApplication swingApplication = getService(context, CySwingApplication.class);
		
		DiffusionContextMenuTaskFactory diffusionContextMenuTaskFactory = new DiffusionContextMenuTaskFactory(diffusionNetworkManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService, client, tunableSetterServiceRef);
		Properties diffusionTaskFactoryProps = new Properties();
		diffusionTaskFactoryProps.setProperty(PREFERRED_MENU, "Diffuse");
		diffusionTaskFactoryProps.setProperty(IN_MENU_BAR, "false");
		diffusionTaskFactoryProps.setProperty(IN_CONTEXT_MENU, "true");
	    diffusionTaskFactoryProps.setProperty("title", "Selected Nodes");
	    
		DiffusionContextMenuTaskFactory withOptionsTaskFactory = 
				new DiffusionContextMenuTaskFactory(diffusionNetworkManager, outputPanel, 
						viewWriterManager, swingApplication, cyApplicationManagerService, 
						client, tunableSetterServiceRef, true);
		Properties wOptsProps = new Properties();
		wOptsProps.setProperty(PREFERRED_MENU, "Diffuse");
		wOptsProps.setProperty(IN_MENU_BAR, "false");
		wOptsProps.setProperty(IN_CONTEXT_MENU, "true");
		wOptsProps.setProperty("title", "Selected Nodes with Options");
	    
		EdgeContextMenuTaskFactory diffusionContextMenuTaskFactory2 = new EdgeContextMenuTaskFactory(diffusionNetworkManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService, client, tunableSetterServiceRef);
		Properties diffusionTaskFactoryProps2 = new Properties();
		diffusionTaskFactoryProps2.setProperty(PREFERRED_MENU, "Diffuse");
		diffusionTaskFactoryProps2.setProperty(IN_MENU_BAR, "false");
//		diffusionTaskFactoryProps2.setProperty(IN_CONTEXT_MENU, "true");
	    diffusionTaskFactoryProps2.setProperty("title", "Selected Nodes");
	    
		DiffusionTaskFactory diffusionTaskFactory = new DiffusionTaskFactory(diffusionNetworkManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService, client, tunableSetterServiceRef);
		Properties diffusionTaskFactoryPropsTool = new Properties();
		diffusionTaskFactoryPropsTool.setProperty(PREFERRED_MENU, DIFFUSION_MENU);
		diffusionTaskFactoryPropsTool.setProperty(MENU_GRAVITY,"1.0");
	    diffusionTaskFactoryPropsTool.setProperty("title", "Selected Nodes");
	    
	    registerAllServices(context, diffusionContextMenuTaskFactory, diffusionTaskFactoryProps);
	    registerAllServices(context, withOptionsTaskFactory, wOptsProps);

	    registerAllServices(context, diffusionContextMenuTaskFactory2, diffusionTaskFactoryProps2);
		registerService(context, diffusionTaskFactory, TaskFactory.class, diffusionTaskFactoryPropsTool);
	}


}
