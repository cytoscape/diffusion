package org.cytoscape.diffusion;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.ci.model.CIError;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.client.DiffusionServiceException;
import org.cytoscape.diffusion.internal.task.DiffuseSelectedTask;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.any;
public class DiffusionTaskTest {
	
	public static String columnName = DiffuseSelectedTask.DIFFUSION_INPUT_COL_NAME;
	
	public static String errorServiceResponse = "{"			
 + " \"data\": {}," 
 + " \"errors\": ["
 + "   {"
 + "     \"type\": \"DUMMY_TYPE\", "
 + "     \"message\": \"DUMMY_MESSAGE\""
 + "   }"
 + " ]"
 + "}";
	
	class TestCyWriter implements CyWriter {
		public ListMultipleSelection<String> aspectFilter = new ListMultipleSelection<>();
		public ListMultipleSelection<String> nodeColFilter = new ListMultipleSelection<>();
		public ListMultipleSelection<String> edgeColFilter = new ListMultipleSelection<>();
		public ListMultipleSelection<String> networkColFilter = new ListMultipleSelection<>();
		
		public TestCyWriter() {
			List<String> aspectPossibleValues = Arrays.asList(new String[]{"nodes", "edges", "nodeAttributes"});
			aspectFilter.setPossibleValues(aspectPossibleValues);
			
			List<String> nodeColPossibleValues = Arrays.asList(new String[] {CyNetwork.NAME, columnName});
			nodeColFilter.setPossibleValues(nodeColPossibleValues);
		}
		
		@Override
		public void run(TaskMonitor taskMonitor) throws Exception {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void cancel() {
			// TODO Auto-generated method stub
			
		}
		
	}
	
	DiffusionTableManager diffusionTableManager;
	CyNetwork cyNetwork;
	
	CyTable nodeTable;
	
	DiffusionTable diffusionTable;
	
	CyNetworkViewWriterFactory cyNetworkViewWriterFactory;
	
	OutputPanel outputPanel;
	CySwingApplication cySwingApplication;
	CyApplicationManager cyApplicationManager;
	
	@Before
	public void before() {
		diffusionTableManager = mock(DiffusionTableManager.class);
		cyNetwork = mock(CyNetwork.class);
		
		nodeTable = mock(CyTable.class);
		when(cyNetwork.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS)).thenReturn(nodeTable);
		
		when(cyNetwork.getDefaultNodeTable()).thenReturn(nodeTable);
		when(nodeTable.getMatchingRows(eq(columnName), eq(true))).thenReturn(new ArrayList<CyRow>());
		
		diffusionTable = mock(DiffusionTable.class);
		
		when(diffusionTableManager.createTable(cyNetwork)).thenReturn(diffusionTable);
		
		cyNetworkViewWriterFactory = mock(CyNetworkViewWriterFactory.class);
		
		TestCyWriter cyWriter = new TestCyWriter();
		when(cyNetworkViewWriterFactory.createWriter(any(OutputStream.class), eq(cyNetwork))).thenReturn(cyWriter);
		
		outputPanel = mock(OutputPanel.class);
		cySwingApplication = mock(CySwingApplication.class);
		cyApplicationManager = mock(CyApplicationManager.class);
	}
	
	@Test
	public void testClientError() throws Exception {
		
		DiffusionServiceClient client = mock(DiffusionServiceClient.class);
		
		when(client.diffuse(any(String.class), any(String.class), any(Double.class), any(DiffuseSelectedTask.class))).thenReturn(errorServiceResponse);
		
		TunableSetter tunableSetter = mock(TunableSetter.class);
		DiffuseSelectedTask task = new DiffuseSelectedTask(
				diffusionTableManager, 
				cyNetwork,
				cyNetworkViewWriterFactory, 
				outputPanel, 
				cySwingApplication, 
				cyApplicationManager, 
				client, 
				tunableSetter);
		
		TaskMonitor tm = mock(TaskMonitor.class);
		try {
			task.run(tm);
			fail("DiffuseSelectedTask continued executing even with service errors reported");
		} catch (DiffusionServiceException e) {
			assertEquals(1, e.ciErrors.size());
			CIError ciError = e.getCIErrors().get(0);
			assertEquals("DUMMY_TYPE", ciError.type);
			assertEquals("DUMMY_MESSAGE", ciError.message);
		}
	}
}
