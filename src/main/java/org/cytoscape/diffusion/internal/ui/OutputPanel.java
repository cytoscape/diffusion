package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.Border;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.diffusion.internal.util.DiffusionResult;
import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.session.events.SessionAboutToBeLoadedEvent;
import org.cytoscape.session.events.SessionAboutToBeLoadedListener;
import org.cytoscape.session.events.SessionLoadedEvent;
import org.cytoscape.session.events.SessionLoadedListener;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

@SuppressWarnings("serial")
public class OutputPanel extends JPanel implements CytoPanelComponent2, SetCurrentNetworkListener,
		ColumnCreatedListener, NetworkAddedListener, SessionAboutToBeLoadedListener, SessionLoadedListener {

	private JComboBox<String> columnNameComboBox;
	private JPanel selectionPanel;
	private JPanel bottomPanel;

	private final CyApplicationManager appManager;

	private final DiffusionTableManager tableManager;

	private final VisualMappingManager vmm;
	private final Set<VisualStyle> styles;

	private final NoResultPanel emptyPanel;
	private JPanel mainPanel;
	private SubnetCreatorPanel subnetPanel;

	final NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory;
	private boolean loadingSession;

	@Override

	public void handleEvent(final SessionAboutToBeLoadedEvent e) {
		loadingSession = true;
		swapPanel(false);
	}

	@Override

	public void handleEvent(final SessionLoadedEvent e) {
		loadingSession = false;
		final CyNetwork network = this.appManager.getCurrentNetwork();
		DiffusionTable table = tableManager.getTable(network.getSUID());
		if (table == null) {
			table = this.tableManager.createTable(network);

		}
		String defaultCol = null;
		for (String column : table.getAvailableOutputColumns()) {
			String base = column.substring(0, column.length() - 5);
			if (defaultCol == null)
				defaultCol = column;
			DiffusionResult result = new DiffusionResult(network, base + "_rank", base + "_heat");
			table.setDiffusionResult(base, result);
		}
		if (defaultCol != null) {
			tableManager.setCurrentTable(table);
			configureColumnNameComboBox();
			setColumnName(defaultCol);
		}
		swapPanel(true);

	}

	public OutputPanel(DiffusionTableManager tableManager, Set<VisualStyle> styles,
			final CyApplicationManager appManager, final VisualMappingManager vmm,
			final NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory) {
		this.appManager = appManager;
		this.styles = styles;
		this.vmm = vmm;
		this.tableManager = tableManager;
		this.createSubnetworkFactory = createSubnetworkFactory;

		this.emptyPanel = new NoResultPanel();

		this.setBackground(Color.white);

		initPanel();
		this.add(emptyPanel);

	}

	private final void initPanel() {
		// Basic setup for this parent panel

		selectionPanel = new JPanel();
		mainPanel = new JPanel();

		subnetPanel = new SubnetCreatorPanel(tableManager, styles, vmm, createSubnetworkFactory, appManager);
		subnetPanel.setOpaque(false);
		subnetPanel.setMaximumSize(new Dimension(5000, 56));
		subnetPanel.setMinimumSize(new Dimension(380, 56));

		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.white);
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(selectionPanel, BorderLayout.CENTER);

		final JPanel columnSelectorPanel = createSelector();
		columnSelectorPanel.setOpaque(false);

		mainPanel.setMinimumSize(new Dimension(380, 155));
		mainPanel.setMaximumSize(new Dimension(5000, 155));

		final Border padding = BorderFactory.createEmptyBorder(0, 5, 0, 5);
		mainPanel.setOpaque(false);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createTitledBorder("Node Selector"));

		columnSelectorPanel.setBorder(padding);
		bottomPanel.setBorder(padding);
		mainPanel.add(columnSelectorPanel, BorderLayout.NORTH);
		mainPanel.add(bottomPanel, BorderLayout.CENTER);
	}

	public void swapPanel(boolean showResult) {
		if (showResult) {
			this.removeAll();
			this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			this.add(mainPanel);
			this.add(subnetPanel);
		} else {
			this.removeAll();
			this.setLayout(new BorderLayout());
			this.add(emptyPanel, BorderLayout.CENTER);
		}
		this.updateUI();
	}

	private final JPanel createSelector() {
		final JPanel selectorPanel = new JPanel();
		selectorPanel.setBackground(Color.white);
		selectorPanel.setLayout(new BorderLayout());

		configureColumnNameComboBox();

		final JLabel selectorLabel = new JLabel("Range Column: ");
		selectorPanel.add(selectorLabel, BorderLayout.LINE_START);
		selectorPanel.add(columnNameComboBox, BorderLayout.CENTER);
		return selectorPanel;
	}

	private void configureColumnNameComboBox() {

		DiffusionTable currentTable = tableManager.getCurrentTable();
		if (currentTable == null) {
			columnNameComboBox = new JComboBox<String>(new String[0]);
		} else {
			final String[] cols = currentTable.getAvailableOutputColumns();
			columnNameComboBox.removeAllItems();
			for (String col : cols)
				columnNameComboBox.addItem(col);
			//columnNameComboBox = new JComboBox<String>(cols);
		}

		columnNameComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				if (columnNameComboBox.getSelectedIndex() == -1)
					return;
				final String columnName = getColumnName();
				columnNameComboBox.setSelectedItem(columnName);
				columnNameSelected(columnName);
			}
		});
	}

	private void columnNameSelected(String columnName) {

		final CyNetwork network = this.appManager.getCurrentNetwork();
		final DiffusionTable diffusionTable = tableManager.getTable(network.getSUID());

		if (columnName.endsWith("_rank")) {
			final String base = columnName.replace("_rank", "");

			diffusionTable.setCurrentDiffusionResult(base);
			setSelectionPanel(new RankSelectionPanel(diffusionTable, "Rank"));
		} else if (columnName.endsWith("_heat")) {
			final String base = columnName.replace("_heat", "");

			diffusionTable.setCurrentDiffusionResult(base);
			setSelectionPanel(new HeatSelectionPanel(diffusionTable, "Heat"));
		} else {
			setSelectionPanel(new JPanel());
		}
	}

	private void setSelectionPanel(JPanel panel) {
		bottomPanel.remove(selectionPanel);
		selectionPanel = panel;
		bottomPanel.add(selectionPanel, BorderLayout.CENTER);
		this.validate();
		this.repaint();
	}

	public void setColumnName(String columnName) {
		columnNameComboBox
				.setModel(new DefaultComboBoxModel(tableManager.getCurrentTable().getAvailableOutputColumns()));
		columnNameComboBox.setSelectedItem(columnName);
		columnNameSelected(columnName);
	}

	private String getColumnName() {
		return (String) columnNameComboBox.getSelectedItem();
	}

	@Override
	public Component getComponent() {
		return this;
	}

	@Override
	public CytoPanelName getCytoPanelName() {
		return CytoPanelName.EAST;
	}

	@Override
	public String getTitle() {
		return "Diffusion Output";
	}

	@Override
	public Icon getIcon() {
		return null;
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent evt) {
		if (loadingSession)
			return;

		if (this.appManager.getCurrentNetwork() == null) {
			swapPanel(false);
			return;
		}

		final CyNetwork network = evt.getNetwork();
		final DiffusionTable table = tableManager.getTable(network.getSUID());

		// No result yet.
		if (table == null) {
			// Disable UI
			swapPanel(false);
			return;
		}

		tableManager.setCurrentTable(table);

		final String[] cols = table.getAvailableOutputColumns();
		final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(cols);
		columnNameComboBox.setModel(model);

		if (cols == null || cols.length == 0) {
			columnNameComboBox.setEnabled(false);
			swapPanel(false);
		} else {
			columnNameComboBox.setEnabled(true);

			// Select first item
			columnNameSelected(cols[0]);
			swapPanel(true);
		}
	}

	@Override
	public void handleEvent(ColumnCreatedEvent evt) {
		if (this.appManager.getCurrentNetwork() == null)
			return;
		final CyTable sourceTable = evt.getSource();
		final String columnName = evt.getColumnName();

		if (!sourceTable.equals(this.appManager.getCurrentNetwork().getTable(CyNode.class, CyNetwork.LOCAL_ATTRS))) {
			return;
		}

		if (columnName.startsWith("diffusion_")) {
			columnNameComboBox.setEnabled(true);
			this.setEnabled(true);
		}
	}

	@Override
	public void handleEvent(NetworkAddedEvent nae) {
		if (loadingSession)
			return;
		final CyNetwork net = nae.getNetwork();

		// Remove all local columns copied from original one.
		final DiffusionTable newTable = this.tableManager.createTable(net);
		final String[] cols = newTable.getAvailableOutputColumns();
		final CyTable localTable = net.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);

		

		for (final String colName : cols) {
			final CyColumn col = localTable.getColumn(colName);
			if (col != null) {
				localTable.deleteColumn(colName);
			}
		}

		// Disable UI
		swapPanel(false);
	}

	@Override
	public String getIdentifier() {
		return "diffusion";
	}
}
