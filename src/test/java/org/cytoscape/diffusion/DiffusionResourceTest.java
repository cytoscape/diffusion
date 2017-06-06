package org.cytoscape.diffusion;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.diffusion.internal.ViewWriterFactoryManager;
import org.cytoscape.ci.CIErrorFactory;
import org.cytoscape.ci.model.CIError;
import org.cytoscape.diffusion.internal.client.DiffusionServiceClient;
import org.cytoscape.diffusion.internal.client.DiffusionServiceException;
import org.cytoscape.diffusion.internal.rest.DiffusionResource;
import org.cytoscape.diffusion.internal.rest.DiffusionTaskObserver;
import org.cytoscape.diffusion.internal.task.DiffuseSelectedTask;
import org.cytoscape.diffusion.internal.task.DiffusionContextMenuTaskFactory;
import org.cytoscape.diffusion.internal.ui.OutputPanel;
import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.io.write.CyNetworkViewWriterFactory;
import org.cytoscape.io.write.CyWriter;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNetworkManager;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.model.CyNetworkViewManager;
import org.cytoscape.work.FinishStatus;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.SynchronousTaskManager;
import org.cytoscape.work.TaskIterator;
import org.cytoscape.work.TaskMonitor;
import org.cytoscape.work.TaskObserver;
import org.cytoscape.work.TunableSetter;
import org.cytoscape.work.util.ListMultipleSelection;
import org.junit.Before;
import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class DiffusionResourceTest {
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
	CyNetworkView cyNetworkView;

	long cyNetworkSUID = 52l;
	long cyNetworkViewSUID = 770l;

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
		when(cyNetwork.getSUID()).thenReturn(cyNetworkSUID);
		cyNetworkView = mock(CyNetworkView.class);
		when(cyNetworkView.getSUID()).thenReturn(cyNetworkViewSUID);
		when(cyNetworkView.getModel()).thenReturn(cyNetwork);
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

		when(cyApplicationManager.getCurrentNetwork()).thenReturn(cyNetwork);
		when(cyApplicationManager.getCurrentNetworkView()).thenReturn(cyNetworkView);


	}
	
	@Test
	public void testResourceClientError() throws Exception {

		DiffusionServiceClient client = mock(DiffusionServiceClient.class);

		when(client.diffuse(any(String.class), any(String.class), any(Double.class))).thenReturn(errorServiceResponse);

		TunableSetter tunableSetter = mock(TunableSetter.class);

		SynchronousTaskManager<?> syncTaskManager = mock(SynchronousTaskManager.class);

		CyNetworkManager cyNetworkManager = mock(CyNetworkManager.class);

		when(cyNetworkManager.getNetwork(any(Long.class))).thenReturn(cyNetwork);

		CyNetworkViewManager cyNetworkViewManager = mock(CyNetworkViewManager.class);

		when(cyNetworkViewManager.getNetworkViews(cyNetwork)).thenReturn(Arrays.asList(cyNetworkView));

		ViewWriterFactoryManager viewWriterFactoryManager = mock(ViewWriterFactoryManager.class);
		when(viewWriterFactoryManager.getCxFactory()).thenReturn(cyNetworkViewWriterFactory);
		DiffusionContextMenuTaskFactory diffusionContextMenuTaskFactory = new DiffusionContextMenuTaskFactory(diffusionTableManager, outputPanel, viewWriterFactoryManager, cySwingApplication, cyApplicationManager, client, tunableSetter);

		ObservableTask dummyJsonTask = mock(ObservableTask.class);
		when(dummyJsonTask.getResults(any(Class.class))).thenReturn(null);
		
		CIErrorFactory ciErrorFactory = mock(CIErrorFactory.class);
		
		doAnswer(new Answer<CIError>() {
			public CIError answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				CIError ciError = new CIError();
					ciError.status = (Integer) args[0];
					ciError.type = (String) args[1];
					ciError.message = (String) args[2];
					
				return ciError;
			}
		}).when(ciErrorFactory).getCIError(any(Integer.class), any(String.class), any(String.class));
		
		doAnswer(new Answer<Void>() {
			public Void answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				if (args[1] instanceof TaskObserver) {
					//TODO make this execute closer to how Commands are actually executed.
					((TaskObserver) args[1]).taskFinished(dummyJsonTask);
					CIError ciError = new CIError();
					ciError.message = "service error";
					((TaskObserver) args[1]).allFinished(FinishStatus.newFailed(dummyJsonTask, new DiffusionServiceException("dummy failure", Arrays.asList(ciError))));
				}
				return null;
			}
		}).when(syncTaskManager).execute(any(TaskIterator.class), any(TaskObserver.class));

		DiffusionResource diffusionResource = new DiffusionResource(
				cyApplicationManager, 
				syncTaskManager, 
				cyNetworkManager, 
				cyNetworkViewManager, 
				diffusionContextMenuTaskFactory, 
				null, 
				ciErrorFactory);

		DiffusionTaskObserver taskObserver = new DiffusionTaskObserver(diffusionResource, "dummy_urn", "dummy_error_code");
	
		diffusionResource.executeDiffuse(cyNetworkSUID, cyNetworkViewSUID, taskObserver);
		
		assertNotNull(taskObserver.getResponse().data);
		assertNotNull(taskObserver.getResponse().errors);
		assertEquals(2, taskObserver.getResponse().errors.size());
		assertEquals("service error", taskObserver.getResponse().errors.get(0).message);
		assertEquals("dummy failure", taskObserver.getResponse().errors.get(1).message);
	}
}
