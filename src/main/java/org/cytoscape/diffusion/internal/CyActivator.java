package org.cytoscape.diffusion.internal;

import static org.cytoscape.application.swing.ActionEnableSupport.ENABLE_FOR_SELECTED_NODES;
import static org.cytoscape.work.ServiceProperties.ENABLE_FOR;
import static org.cytoscape.work.ServiceProperties.IN_CONTEXT_MENU;
import static org.cytoscape.work.ServiceProperties.IN_MENU_BAR;
import static org.cytoscape.work.ServiceProperties.MENU_GRAVITY;
import static org.cytoscape.work.ServiceProperties.PREFERRED_MENU;
import static org.cytoscape.work.ServiceProperties.COMMAND;
import static org.cytoscape.work.ServiceProperties.COMMAND_DESCRIPTION;
import static org.cytoscape.work.ServiceProperties.COMMAND_NAMESPACE;

import java.util.Dictionary;
import java.util.Properties;
import java.util.Set;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.rest.DiffusionResource;
import org.cytoscape.diffusion.internal.task.DiffusionContextMenuTaskFactory;
import org.cytoscape.diffusion.internal.task.EdgeContextMenuTaskFactory;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.subnetwork.CyRootNetworkManager;
import org.cytoscape.property.CyProperty;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskFactory;

import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.swing.DialogTaskManager;
import org.osgi.framework.BundleContext;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;

public class CyActivator extends AbstractCyActivator {

	private static final String DIFFUSION_MENU = "Tools.Diffuse[2100]";
	private static final String STYLES = "/styles.xml";

	@Override
	public void start(BundleContext context) throws Exception {

		// OSGi service listener to import dependency dynamically
		final ViewWriterFactoryManager viewWriterManager = new ViewWriterFactoryManager();
		registerServiceListener(context, viewWriterManager, "addFactory", "removeFactory",
				CyNetworkViewWriterFactory.class);


		final TunableSetter tunableSetterServiceRef = getService(context, TunableSetter.class);

		VisualMappingManager vmm = getService(context, VisualMappingManager.class);
		NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory = getService(context,
				NewNetworkSelectedNodesOnlyTaskFactory.class);

		CyApplicationManager cyApplicationManagerService = getService(context, CyApplicationManager.class);

		SynchronousTaskManager<?> synchronousTaskManager = getService(context, SynchronousTaskManager.class);
		final CyNetworkManager cyNetworkManager = getService(context, CyNetworkManager.class);
		final CyNetworkViewManager cyNetworkViewManager = getService(context, CyNetworkViewManager.class);
		
		LoadVizmapFileTaskFactory vizmapLoader = getService(context, LoadVizmapFileTaskFactory.class);
		Set<VisualStyle> styles = vizmapLoader.loadStyles(getClass().getResource(STYLES).openStream());

		// Create service client instance
		@SuppressWarnings("unchecked")
		final CyProperty<Properties> props = getService(context, CyProperty.class, "(cyPropertyName=cytoscape3.props)");

		// Table Manager
		final DiffusionTableManager tableManager = new DiffusionTableManager();
		registerAllServices(context, tableManager, new Properties());
		
		final DiffusionServiceClient client;
		final Object serviceUrlProp = props.getProperties().get("diffusion.url");
		if (serviceUrlProp != null) {
			client = new DiffusionServiceClient(serviceUrlProp.toString());
		} else {
			client = new DiffusionServiceClient();
		}

		OutputPanel outputPanel = new OutputPanel(tableManager, styles, cyApplicationManagerService, vmm, createSubnetworkFactory);
		registerAllServices(context, outputPanel, new Properties());

		final CySwingApplication swingApplication = getService(context, CySwingApplication.class);
		
		DiffusionContextMenuTaskFactory diffusionContextMenuTaskFactory = new DiffusionContextMenuTaskFactory(
				tableManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService,
				client, tunableSetterServiceRef);

		Properties diffusionTaskFactoryProps = new Properties();
		diffusionTaskFactoryProps.setProperty(COMMAND_NAMESPACE, "diffusion");
		diffusionTaskFactoryProps.setProperty(COMMAND, "diffuse");
		diffusionTaskFactoryProps.setProperty(COMMAND_DESCRIPTION, "Execute Diffusion on Selected Nodes");
		diffusionTaskFactoryProps.setProperty(PREFERRED_MENU, "Diffuse");
		diffusionTaskFactoryProps.setProperty(IN_MENU_BAR, "false");
		diffusionTaskFactoryProps.setProperty(IN_CONTEXT_MENU, "true");
		diffusionTaskFactoryProps.setProperty("title", "Selected Nodes");

		diffusionTaskFactoryProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);

		final DiffusionContextMenuTaskFactory withOptionsTaskFactory = new DiffusionContextMenuTaskFactory(
				tableManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService,
				client, tunableSetterServiceRef, true);

		Properties wOptsProps = new Properties();
		wOptsProps.setProperty(COMMAND_NAMESPACE, "diffusion");
		wOptsProps.setProperty(COMMAND, "diffuse_advanced");
		wOptsProps.setProperty(COMMAND_DESCRIPTION, "Execute Diffusion with Options");
		wOptsProps.setProperty(PREFERRED_MENU, "Diffuse");
		wOptsProps.setProperty(IN_MENU_BAR, "false");
		wOptsProps.setProperty(IN_CONTEXT_MENU, "true");
		wOptsProps.setProperty("title", "Selected Nodes with Options");

		final String logLocation;

		// Extract Karaf's log file location for DiffusionResource's error reporting.
		ConfigurationAdmin configurationAdmin = getService(context, ConfigurationAdmin.class);
		if (configurationAdmin != null) {
			Configuration config = configurationAdmin.getConfiguration("org.ops4j.pax.logging");

			Dictionary<?,?> dictionary = config.getProperties();
			Object logObject = dictionary.get("log4j.appender.file.File");
			if (logObject != null && logObject instanceof String) {
				logLocation = (String) logObject;
			}
			else {
				logLocation = null;
			}
		}
		else {
			logLocation = null;
		}

		DiffusionResource diffusionResource = new DiffusionResource(synchronousTaskManager, cyNetworkManager, cyNetworkViewManager, diffusionContextMenuTaskFactory, withOptionsTaskFactory, logLocation);
		registerService(context, diffusionResource, DiffusionResource.class, new Properties());

		wOptsProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);

		EdgeContextMenuTaskFactory edgeContextMenuTaskFactory = new EdgeContextMenuTaskFactory(
				tableManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService,
				client, tunableSetterServiceRef, false);
		Properties edgeProps = new Properties();
		edgeProps.setProperty(PREFERRED_MENU, "Diffuse");
		edgeProps.setProperty(IN_MENU_BAR, "false");
		edgeProps.setProperty(IN_CONTEXT_MENU, "true");
		edgeProps.setProperty("title", "Selected Nodes");
		edgeProps.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);
		
		EdgeContextMenuTaskFactory edgeContextMenuTaskFactoryOpt = new EdgeContextMenuTaskFactory(
				tableManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService,
				client, tunableSetterServiceRef, true);
		Properties edgePropsOpt = new Properties();
		edgePropsOpt.setProperty(PREFERRED_MENU, "Diffuse");
		edgePropsOpt.setProperty(IN_MENU_BAR, "false");
		edgePropsOpt.setProperty(IN_CONTEXT_MENU, "true");
		edgePropsOpt.setProperty("title", "Selected Nodes with Options");
		edgePropsOpt.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);

		/////////////////////// For TOOLS menu//////////////////////////////


		final DiffusionContextMenuTaskFactory noOptionsTaskFactoryTool = new DiffusionContextMenuTaskFactory(
				tableManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService,
				client, tunableSetterServiceRef, false);
		Properties diffusionTaskFactoryPropsTool1 = new Properties();
		diffusionTaskFactoryPropsTool1.setProperty(PREFERRED_MENU, DIFFUSION_MENU);
		diffusionTaskFactoryPropsTool1.setProperty(MENU_GRAVITY, "1.0");
		diffusionTaskFactoryPropsTool1.setProperty(IN_CONTEXT_MENU, "false");
		diffusionTaskFactoryPropsTool1.setProperty("title", "Selected Nodes");
		diffusionTaskFactoryPropsTool1.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);

		final DiffusionContextMenuTaskFactory withOptionsTaskFactoryTool = new DiffusionContextMenuTaskFactory(
				tableManager, outputPanel, viewWriterManager, swingApplication, cyApplicationManagerService,
				client, tunableSetterServiceRef, true);
		Properties diffusionTaskFactoryPropsTool2 = new Properties();
		diffusionTaskFactoryPropsTool2.setProperty(PREFERRED_MENU, DIFFUSION_MENU);
		diffusionTaskFactoryPropsTool2.setProperty(MENU_GRAVITY, "1.0");
		diffusionTaskFactoryPropsTool2.setProperty(IN_CONTEXT_MENU, "false");
		diffusionTaskFactoryPropsTool2.setProperty("title", "Selected Nodes with Options");
		diffusionTaskFactoryPropsTool2.setProperty(ENABLE_FOR, ENABLE_FOR_SELECTED_NODES);

		// For Context
		registerAllServices(context, diffusionContextMenuTaskFactory, diffusionTaskFactoryProps);
		registerAllServices(context, withOptionsTaskFactory, wOptsProps);

		registerAllServices(context, edgeContextMenuTaskFactory, edgeProps);
		registerAllServices(context, edgeContextMenuTaskFactoryOpt, edgePropsOpt);

		// For Tools
		registerAllServices(context, noOptionsTaskFactoryTool, diffusionTaskFactoryPropsTool1);
		registerAllServices(context, withOptionsTaskFactoryTool, diffusionTaskFactoryPropsTool2);
	}

}
