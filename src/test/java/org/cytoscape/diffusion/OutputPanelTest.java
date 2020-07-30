package org.cytoscape.diffusion;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
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
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import static org.mockito.Mockito.doAnswer;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.awt.Component;
import java.util.Properties;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.SwingUtilities;

import static org.mockito.Matchers.anyObject;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.same;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

public class OutputPanelTest {
	
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
	
	boolean serviceRegistered;
	
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
	
		doAnswer( new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				if (invocation.getArguments()[0] == outputPanel && invocation.getArguments()[1] == CytoPanelComponent.class && invocation.getArguments()[2] instanceof Properties)
				{
					serviceRegistered = true;
				} else {
                                        System.err.println("Failing call");
                                        System.err.flush();
					fail(); 
				}
				return null;
			}
		}).when(serviceRegistrar).registerService(anyObject(), any(Class.class), any(Properties.class));
		
		doAnswer( new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				if (invocation.getArguments()[0] == outputPanel && invocation.getArguments()[1] == CytoPanelComponent.class)
				{
					serviceRegistered = false;
				} else {
                                        System.err.println("Failing call");
                                        System.err.flush();
					fail();
				}
				return null;
			}
		}).when(serviceRegistrar).unregisterService(anyObject(), any(Class.class));
		
		
		//doNothing().when(serviceRegistrar).registerService(anyObject(), any(Class.class), any(Properties.class));
		//doNothing().when(serviceRegistrar).unregisterService(anyObject(), any(Class.class));
		
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
		
		serviceRegistered = false;
	}
	
	private void setExistingOutputPanel() {
		CytoPanel cytoPanel = mock(CytoPanel.class);
		
		when(cytoPanel.getCytoPanelComponentCount()).thenReturn(1);
		when(cytoPanel.indexOfComponent(eq(OutputPanel.IDENTIFIER))).thenReturn(0);
		
		when(cytoPanel.getComponentAt(eq(0))).thenReturn(outputPanel);
		when(cySwingApplication.getCytoPanel(eq(CytoPanelName.EAST))).thenReturn(cytoPanel);
	
		serviceRegistered = true;
	}
	
	@Test
	public void testBase() throws Exception {
		
	}
	
        /**
         * Adds a trivial task to UI thread and waits for it to be
         * run which implies all other tasks have been completed. 
         * Had to switch this cause the notify() logic earlier was failing
         * on Ubuntu boxes (and only Ubuntu boxes) which was very
         * weird
         */
	private void waitForSwingEDT() {
            final AtomicBoolean swingUpdated = new AtomicBoolean(false);
		final Runnable runnable = new Runnable() {
			@Override
			public void run() {
                                swingUpdated.set(true);
			};
		};
                SwingUtilities.invokeLater(runnable);

                // wait until 
                while(swingUpdated.get() == false){
                    try{
                        Thread.sleep(100);
                    } catch(InterruptedException ie){
                        
                    } 
                }
	}
	
	@Test
	public void testExistingOutputPanelReceivesNullNetwork() throws Exception {
	
		setExistingOutputPanel();
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, null));
		waitForSwingEDT();
		
		assertEquals(false, serviceRegistered);
		verify(serviceRegistrar, times(1)).unregisterService(same(outputPanel), same(CytoPanelComponent.class));
		verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
	}
	
	@Test
	public void testExistingOutputPanelReceivesNetworkWithZeroColumns() throws Exception {
	
		setExistingOutputPanel();
		long networkSUID = 1l;
		
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(networkSUID);
		
		DiffusionTable diffusionTable = mock(DiffusionTable.class);
		
		when(diffusionTableManager.getTable(eq(networkSUID))).thenReturn(diffusionTable);
		when(diffusionTable.getAvailableOutputColumns()).thenReturn(new String[]{});
		
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, network));
		waitForSwingEDT();
		
		assertEquals(false, serviceRegistered);
		verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
		verify(serviceRegistrar, times(1)).unregisterService(any(Object.class), any(Class.class));
	}
	
	@Test 
	public void testExistingOutputPanelReceivesNetworkWithNonDiffusionColumns() throws Exception {
	
		setExistingOutputPanel();
		
		long networkSUID = 1l;
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(networkSUID);
		
		DiffusionTable diffusionTable = mock(DiffusionTable.class);
		when(diffusionTableManager.getTable(eq(networkSUID))).thenReturn(diffusionTable);
		
		when(diffusionTable.getAvailableOutputColumns()).thenReturn(new String[]{});
		when(cyApplicationManager.getCurrentNetwork()).thenReturn(network);
		
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, network));
		waitForSwingEDT();
		
		assertEquals(false, serviceRegistered);
		verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
		verify(serviceRegistrar, times(1)).unregisterService(any(Object.class), any(Class.class));
	}
	
	@Test 
	public void testExistingOutputPanelReceivesNetworkWithDiffusionColumn() throws Exception {
		
		setExistingOutputPanel();
		
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
		waitForSwingEDT();
	
		assertEquals(true, serviceRegistered);
		verify(serviceRegistrar, never()).registerService(same(outputPanel), same(CytoPanelComponent.class), any(Properties.class));
		verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
		
		network.dispose();
		when(diffusionTableManager.getTable(eq(networkSUID))).thenReturn(null);
		when(diffusionTableManager.getCurrentTable()).thenReturn(null);
	}
	
	@Test
	public void testNoOutputPanelReceivesNullNetwork() throws Exception {
		setNoOutputPanel();
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, null));
		waitForSwingEDT();
		
		assertEquals(false, serviceRegistered);
		verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
		verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
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
		waitForSwingEDT();
	
		assertEquals(false, serviceRegistered);
		verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
		verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
	}
	
	@Test
	public void testNoOutputPanelReceivesNetworkWithNonDiffusionColumns() throws Exception {
	
		setNoOutputPanel();
		
		long networkSUID = 1l;
		CyNetwork network = mock(CyNetwork.class);
		when(network.getSUID()).thenReturn(networkSUID);
		
		DiffusionTable diffusionTable = mock(DiffusionTable.class);
		when(diffusionTableManager.getTable(eq(networkSUID))).thenReturn(diffusionTable);
		
		when(diffusionTable.getAvailableOutputColumns()).thenReturn(new String[]{});
		when(cyApplicationManager.getCurrentNetwork()).thenReturn(network);
		
		outputPanel.handleEvent(new SetCurrentNetworkEvent(cyApplicationManager, network));
		waitForSwingEDT();
		
	
		assertEquals(false, serviceRegistered);
		verify(serviceRegistrar, never()).registerService(any(Object.class), any(Class.class), any(Properties.class));
		verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
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
		waitForSwingEDT();
		
		assertEquals(true, serviceRegistered);
		verify(serviceRegistrar, times(1)).registerService(same(outputPanel), same(CytoPanelComponent.class), any(Properties.class));
		verify(serviceRegistrar, never()).unregisterService(any(Object.class), any(Class.class));
	}
}
