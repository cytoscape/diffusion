package org.cytoscape.diffusion;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionResult;
import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.Invocation;

import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.util.Collection;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Matchers.eq;

public class OutputPanelTest {
	
	/*
	ArgumentCaptor<Object> registerServiceCaptor;
	ArgumentCaptor<Class> registerClassCaptor;
	
	ArgumentCaptor<Object> unRegisterServiceCaptor;
	ArgumentCaptor<Class> unRegisterClassCaptor;
	*/
	OutputPanel outputPanel;
	CyServiceRegistrar serviceRegistrar;
	DiffusionTableManager diffusionTableManager;
	LoadVizmapFileTaskFactory loadVizmapStyleTaskFactory;
	CyApplicationManager cyApplicationManager;
	VisualMappingManager visualMappingManager;
	NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory;
	RenderingEngineManager renderingEngineMgr;
	CySwingApplication cySwingApplication;
	VisualStyle defaultVisualStyle;
	
	@Before
	public void before() {
		serviceRegistrar = mock(CyServiceRegistrar.class);
		diffusionTableManager = mock (DiffusionTableManager.class);
		loadVizmapStyleTaskFactory = mock(LoadVizmapFileTaskFactory.class);
		cyApplicationManager = mock(CyApplicationManager.class);
		visualMappingManager = mock(VisualMappingManager.class);
		createSubnetworkFactory = mock(NewNetworkSelectedNodesOnlyTaskFactory.class);
		renderingEngineMgr = mock(RenderingEngineManager.class);
		cySwingApplication = mock(CySwingApplication.class);
		defaultVisualStyle = mock(VisualStyle.class);
		
		when(defaultVisualStyle.getTitle()).thenReturn("dummyVisualStyleTitle");
		when(visualMappingManager.getDefaultVisualStyle()).thenReturn(defaultVisualStyle);
	/*	
		registerServiceCaptor = ArgumentCaptor.forClass(Object.class);;
		registerClassCaptor = ArgumentCaptor.forClass(Class.class);
		
		unRegisterServiceCaptor = ArgumentCaptor.forClass(Object.class);;
		unRegisterClassCaptor = ArgumentCaptor.forClass(Class.class);
	*/	
		doNothing().when(serviceRegistrar).registerService(anyObject(), any(Class.class), any(Properties.class));
		doNothing().when(serviceRegistrar).unregisterService(anyObject(), any(Class.class));
		
		outputPanel = new OutputPanel(
				serviceRegistrar, 
				diffusionTableManager, 
				loadVizmapStyleTaskFactory, cyApplicationManager, visualMappingManager, 
				createSubnetworkFactory, 
				renderingEngineMgr, 
				cySwingApplication);
	}
	
	public void after() {
		serviceRegistrar = null;
		diffusionTableManager = null;
		loadVizmapStyleTaskFactory = null;
		cyApplicationManager = null;
		visualMappingManager = null;
		createSubnetworkFactory = null;
		renderingEngineMgr = null;
		cySwingApplication = null;
		defaultVisualStyle = null;
	}
	
	private void setNoOutputPanel() {
		CytoPanel cytoPanel = mock(CytoPanel.class);
		
		when(cytoPanel.indexOfComponent(eq(OutputPanel.IDENTIFIER))).thenReturn(-1);
		when(cytoPanel.getThisComponent()).thenReturn(mock(Component.class));
		when(cySwingApplication.getCytoPanel(eq(CytoPanelName.EAST))).thenReturn(cytoPanel);
	}
	
	private void setExistingOutputPanel() {
		CytoPanel cytoPanel = mock(CytoPanel.class);
		
		when(cytoPanel.getCytoPanelComponentCount()).thenReturn(1);
		when(cytoPanel.indexOfComponent(eq(OutputPanel.IDENTIFIER))).thenReturn(0);
		
		when(cytoPanel.getComponentAt(eq(0))).thenReturn(outputPanel);
		when(cySwingApplication.getCytoPanel(eq(CytoPanelName.EAST))).thenReturn(cytoPanel);
	}
	
	@Test
	public void testBase() throws Exception {
		
	}
	
	private void printArgs() {
		Collection<Invocation> invocations = Mockito.mockingDetails(serviceRegistrar).getInvocations();
		System.out.println(invocations.size());
	}
	
	@Test
	public void testNoOutputPanelReceivesNullNetwork() throws Exception {
	
		setNoOutputPanel();
		
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, null));
		
		printArgs();
		//verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
		//verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
	}
	
	@Test
	public void testExistingOutputPanelReceivesNullNetwork() throws Exception {
	
		setExistingOutputPanel();
		
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, null));
		printArgs();
		//verify(serviceRegistrar, times(1)).unregisterService(same(outputPanel), same(CytoPanelComponent.class));
		//verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
	}
	
	@Test
	public void testNoOutputPanelReceivesNetworkWithZeroColumns() throws Exception {
	
		setNoOutputPanel();
		
		long networkSUID = 1l;
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(networkSUID);
		
		DiffusionTable diffusionTable = mock(DiffusionTable.class);
		
		when(diffusionTableManager.getTable(eq(networkSUID))).thenReturn(diffusionTable);
		
		when(diffusionTable.getAvailableOutputColumns()).thenReturn(new String[]{});
		
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, network));
		printArgs();
		//verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
		//verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
	}
	
	@Test
	public void testNoOutputPanelReceivesNetworkWithNonDiffusionColumns() throws Exception {
	
		setNoOutputPanel();
		
		long networkSUID = 1l;
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(networkSUID);
		
		DiffusionTable diffusionTable = mock(DiffusionTable.class);
		
		when(diffusionTableManager.getTable(eq(networkSUID))).thenReturn(diffusionTable);
		
		when(diffusionTable.getAvailableOutputColumns()).thenReturn(new String[]{"notdiffusion"});
		
		when(cyApplicationManager.getCurrentNetwork()).thenReturn(network);
		
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, network));
		printArgs();
		//verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
		//verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
	}
	
	@Test
	public void testNoOutputPanelReceivesNetworkWithDiffusionColumn() throws Exception {
		
		setNoOutputPanel();
		
		long networkSUID = 1l;
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(networkSUID);
		
		
		CyTable table = mock(CyTable.class);
	
		when(network.getTable(same(CyNode.class), eq(CyNetwork.LOCAL_ATTRS))).thenReturn(table);
		
		DiffusionTable diffusionTable = mock(DiffusionTable.class);
		when(diffusionTable.getAssociatedNetwork()).thenReturn(network);
		
		when(diffusionTableManager.getTable(eq(networkSUID))).thenReturn(diffusionTable);
		when(diffusionTable.getAvailableOutputColumns()).thenReturn(new String[]{"diffusion_output_heat"});
		
		DiffusionResult diffusionResult = mock(DiffusionResult.class);
		when(diffusionResult.getMaxHeat()).thenReturn(40d);
		when(diffusionResult.getMinHeat()).thenReturn(1d);
		
		when(diffusionResult.getMinRank()).thenReturn(1);
		when(diffusionResult.getMaxRank()).thenReturn(4);
		
		when(diffusionTable.getCurrentResult()).thenReturn(diffusionResult);
		
		when(cyApplicationManager.getCurrentNetwork()).thenReturn(network);
		
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, network));
		printArgs();
		//verify(serviceRegistrar, times(1)).registerService(same(outputPanel), same(CytoPanelComponent.class), any(Properties.class));
		//verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
	}
}
