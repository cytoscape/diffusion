package org.cytoscape.diffusion.internal.util;

import java.util.HashMap;
import java.util.Map;

import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.model.CyNetwork;

public class DiffusionTableManager implements SetCurrentNetworkListener {
	
	private DiffusionTable currentTable;
	
	private final Map<Long, DiffusionTable> tables;
	
	
	public DiffusionTableManager() {
		this.tables = new HashMap<>();
	}
	
	
	public DiffusionTable getCurrentTable() {
		return currentTable;
	}
	
	public DiffusionTable createTable(CyNetwork network) {
		final DiffusionTable table = new DiffusionTable(network);
		this.tables.put(network.getSUID(), table);
		this.currentTable = table;
		return table;
	}
	
	
	
	public DiffusionTable getTable(final Long suid) {
		if(suid == null) {
			throw new NullPointerException("Currnet network's SUID is required to get the table.");
		}
		
		return this.tables.get(suid);
	}
	
	public final void setTable(Long suid, DiffusionTable table) {
		if(suid == null) {
			throw new NullPointerException("Currnet network's SUID is required to register a table.");
		}
		if(table == null) {
			throw new NullPointerException("Tbale object is required.");
		}
		
		this.tables.put(suid, table);
		this.currentTable = table;
	}


	@Override
	public void handleEvent(SetCurrentNetworkEvent cne) {
		final CyNetwork currentNetwork = cne.getNetwork();
		
		if(this.tables.get(currentNetwork.getSUID()) != null) {
			this.currentTable = tables.get(currentNetwork.getSUID());
		}
	}
}
