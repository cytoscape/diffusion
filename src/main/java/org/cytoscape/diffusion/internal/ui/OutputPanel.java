package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Properties;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.border.Border;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CySwingApplication;
import org.cytoscape.application.swing.CytoPanel;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelComponent2;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.swing.CytoPanelState;
import org.cytoscape.diffusion.internal.util.DiffusionResult;
import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedEvent;
import org.cytoscape.model.events.NetworkAboutToBeDestroyedListener;
import org.cytoscape.service.util.CyServiceRegistrar;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.presentation.RenderingEngineManager;
import org.cytoscape.view.vizmap.VisualMappingManager;

@SuppressWarnings("serial")
public class OutputPanel extends JPanel
		implements CytoPanelComponent2, SetCurrentNetworkListener, NetworkAboutToBeDestroyedListener {

	public static String IDENTIFIER = "diffusion";

	private JComboBox<String> columnNameComboBox;
	private JPanel selectionPanel;
	private JPanel bottomPanel;

	private final CyApplicationManager appManager;

	private final DiffusionTableManager tableManager;
	private final RenderingEngineManager renderingEngineMgr;
	private final CySwingApplication swingApplication;
	private final VisualMappingManager vmm;
	private final LoadVizmapFileTaskFactory vizmapLoader;
	private final CyServiceRegistrar registrar;
	// private final Set<VisualStyle> styles;

	private final NoResultPanel emptyPanel;
	private JPanel mainPanel;
	private SubnetCreatorPanel subnetPanel;

	final NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory;

	public OutputPanel(CyServiceRegistrar registrar, DiffusionTableManager tableManager,
			LoadVizmapFileTaskFactory vizmapLoader, final CyApplicationManager appManager,
			final VisualMappingManager vmm, final NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory,
			final RenderingEngineManager renderingEngineMgr, final CySwingApplication swingApplication) {
		this.registrar = registrar;
		this.appManager = appManager;
		this.vizmapLoader = vizmapLoader;
		// this.styles = styles;
		this.vmm = vmm;
		this.tableManager = tableManager;
		this.createSubnetworkFactory = createSubnetworkFactory;
		this.renderingEngineMgr = renderingEngineMgr;
		this.emptyPanel = new NoResultPanel();
		this.swingApplication = swingApplication;

		this.setBackground(Color.white);

		initPanel();

		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.add(mainPanel);
		this.add(subnetPanel);

	}

	private final void initPanel() {
		// Basic setup for this parent panel
		selectionPanel = new JPanel();
		mainPanel = new JPanel();

		subnetPanel = new SubnetCreatorPanel(tableManager, vizmapLoader, vmm, createSubnetworkFactory, appManager,
				renderingEngineMgr);
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

	public void setPanelVisible(boolean visible) {
		final CytoPanelComponent2 outputPanel = this;
		SwingUtilities.invokeLater(new Runnable() {

			@Override
			public void run() {

				CytoPanel cytoPanel = swingApplication.getCytoPanel(CytoPanelName.EAST);

				// If the panel is not already registered, create it
				if (visible && cytoPanel.indexOfComponent(OutputPanel.IDENTIFIER) < 0) {

					// Register it
					registrar.registerService(outputPanel, CytoPanelComponent.class, new Properties());

					if (cytoPanel.getState() == CytoPanelState.HIDE)
						cytoPanel.setState(CytoPanelState.DOCK);
					final int componentCount = cytoPanel.getCytoPanelComponentCount();
					// 2. Find Diffusion Output Panel
					int targetPanelIdx = 0;

					for (int i = 0; i < componentCount; i++) {
						final Component panelComponent = cytoPanel.getComponentAt(i);
						if (panelComponent instanceof CytoPanelComponent2) {
							final CytoPanelComponent2 cp2 = (CytoPanelComponent2) panelComponent;
							final String panelId = cp2.getIdentifier();
							if (panelId != null && panelId.equals(OutputPanel.IDENTIFIER)) {
								// Found target panel. Force to update
								final Dimension defSize = new Dimension(300, 400);
								panelComponent.setPreferredSize(defSize);
								panelComponent.setSize(defSize);
								targetPanelIdx = i;
								break;
							}
						}
					}

					cytoPanel.setSelectedIndex(targetPanelIdx);
					cytoPanel.getThisComponent().repaint();
				} else if (!visible && cytoPanel.indexOfComponent(OutputPanel.IDENTIFIER) >= 0) {
					int compIndex = cytoPanel.indexOfComponent(OutputPanel.IDENTIFIER);
					Component panel = cytoPanel.getComponentAt(compIndex);
					if (panel == outputPanel) {
						// Unregister it
						registrar.unregisterService(outputPanel, CytoPanelComponent.class);
					} else {

					}
				}
				((JComponent) outputPanel).updateUI();
			}
		});

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
			// columnNameComboBox = new JComboBox<String>(cols);
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

	public DiffusionTable loadNetworkResults(CyNetwork network) {
		subnetPanel.updateStyles();
		DiffusionTable table = tableManager.createTable(network);
		HashSet<String> bases = new HashSet<String>();
		for (String column : table.getAvailableOutputColumns()) {
			String base = column.substring(0, column.length() - 5);
			if (bases.contains(base))
				continue;
			bases.add(base);
			DiffusionResult result = new DiffusionResult(network, base + "_rank", base + "_heat");
			table.setDiffusionResult(base, result);
		}

		return table;
	}

	@Override
	public void handleEvent(SetCurrentNetworkEvent evt) {
		// setPanelVisible(false);

		final CyNetwork network = evt.getNetwork();
		if (network == null) {
//			setSelectionPanel(emptyPanel);
			setPanelVisible(false);
			return;
		}

		// final CyNetwork network = evt.getNetwork();
		DiffusionTable table = null;
		table = tableManager.getTable(network.getSUID());

		// No result yet.
		if (table == null) {
			table = loadNetworkResults(network);
		}

		if (table.getAvailableOutputColumns().length == 0) {
			setPanelVisible(false);
			return;
		}
		subnetPanel.updateStyles();
		tableManager.setCurrentTable(table);

		final String[] cols = table.getAvailableOutputColumns();

		if (cols == null || cols.length == 0) {
			columnNameComboBox.setEnabled(false);
			setPanelVisible(false);
		} else {
			final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(cols);
			columnNameComboBox.setModel(model);
			columnNameComboBox.setEnabled(true);
			// Select first item
			columnNameSelected(cols[0]);
			setPanelVisible(true);
		}
	}

	@Override
	public String getIdentifier() {
		return IDENTIFIER;
	}

	@Override
	public void handleEvent(NetworkAboutToBeDestroyedEvent arg0) {
		if (arg0.getNetwork() != null) {
			tableManager.removeNetwork(arg0.getNetwork());

			if (selectionPanel instanceof AbstractSliderPanel && arg0.getNetwork() == ((AbstractSliderPanel)selectionPanel).getNetwork()) {
				setSelectionPanel(emptyPanel);
			}
		}
	}

}
