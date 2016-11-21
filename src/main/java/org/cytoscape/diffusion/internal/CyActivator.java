package org.cytoscape.diffusion.internal;

import java.util.Properties;

import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.service.util.AbstractCyActivator;
import org.cytoscape.task.NodeViewTaskFactory;
import org.osgi.framework.BundleContext;

public class CyActivator extends AbstractCyActivator {

	@Override
	public void start(BundleContext context) throws Exception {
    CyNetworkViewWriterFactory writerFactory =  getService(context, CyNetworkViewWriterFactory.class, "(id=cxNetworkWriterFactory)");
		DiffusionTaskFactory diffusionTaskFactory = new DiffusionTaskFactory(writerFactory);
		Properties diffusionTaskFactoryProps = new Properties();
	  diffusionTaskFactoryProps.setProperty("title", "Diffuse Selected Nodes");
	  registerService(context, diffusionTaskFactory, NodeViewTaskFactory.class, diffusionTaskFactoryProps);
	}


}
