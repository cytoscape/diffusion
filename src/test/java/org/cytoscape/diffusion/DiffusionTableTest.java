package org.cytoscape.diffusion;

import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.List;

import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.junit.Test;

import static org.mockito.Mockito.mock;
import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.eq;

public class DiffusionTableTest {
	@Test
	public void getAvailableColumnsTestNotEmptyNoDiffusion() {
		CyNetwork network = mock(CyNetwork.class);
		CyTable table = mock(CyTable.class);
		List<CyColumn> columnList = new ArrayList<CyColumn>();
		CyColumn nonDiffusionColumn = mock(CyColumn.class);
		when(nonDiffusionColumn.getType()).thenReturn((Class)Double.class);
		when(nonDiffusionColumn.getName()).thenReturn("not_diffusion");
		columnList.add(nonDiffusionColumn);
		when(table.getColumns()).thenReturn(columnList);
		when(network.getTable(eq(CyNode.class), eq(CyNetwork.LOCAL_ATTRS))).thenReturn(table);
		
		DiffusionTable diffusionTable = new DiffusionTable(network);
		String[] diffusionColumns = diffusionTable.getAvailableOutputColumns();
		
		assertEquals(0, diffusionColumns.length);
	}
	
	@Test
	public void getAvailableColumnsTestNotEmptyDiffusion() {
		CyNetwork network = mock(CyNetwork.class);
		CyTable table = mock(CyTable.class);
		List<CyColumn> columnList = new ArrayList<CyColumn>();
		CyColumn diffusionColumn = mock(CyColumn.class);
		when(diffusionColumn.getType()).thenReturn((Class)Double.class);
		when(diffusionColumn.getName()).thenReturn("diffusion_output_1_heat");
		columnList.add(diffusionColumn);
		when(table.getColumns()).thenReturn(columnList);
		when(network.getTable(eq(CyNode.class), eq(CyNetwork.LOCAL_ATTRS))).thenReturn(table);
		
		DiffusionTable diffusionTable = new DiffusionTable(network);
		String[] diffusionColumns = diffusionTable.getAvailableOutputColumns();
		
		assertEquals(1, diffusionColumns.length);
		assertEquals("diffusion_output_1_heat", diffusionColumns[0]);
	}
}
