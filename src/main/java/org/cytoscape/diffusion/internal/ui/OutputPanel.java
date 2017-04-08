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
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.events.SetCurrentNetworkEvent;
import org.cytoscape.application.events.SetCurrentNetworkListener;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.diffusion.internal.util.DiffusionTableFactory;
import org.cytoscape.model.CyColumn;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyTable;
import org.cytoscape.model.events.ColumnCreatedEvent;
import org.cytoscape.model.events.ColumnCreatedListener;
import org.cytoscape.model.events.NetworkAddedEvent;
import org.cytoscape.model.events.NetworkAddedListener;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

@SuppressWarnings("serial")
public class OutputPanel extends JPanel implements CytoPanelComponent, SetCurrentNetworkListener, 
	ColumnCreatedListener, NetworkAddedListener {

	private JComboBox<String> columnNameComboBox;
	private JPanel selectionPanel;
	private JPanel bottomPanel;
	
	private final CyApplicationManager appManager;

	private DiffusionTableFactory diffusionTableFactory;
	private DiffusionNetworkManager networkManager;
	private final VisualMappingManager vmm;
	private final Set<VisualStyle> styles;
	
	private final NoResultPanel emptyPanel;
	private JPanel mainPanel;
	private SubnetCreatorPanel subnetPanel;

	public OutputPanel(DiffusionNetworkManager networkManager, Set<VisualStyle> styles,
			final CyApplicationManager appManager, final VisualMappingManager vmm) {
		this.appManager = appManager;
		this.networkManager = networkManager;
		this.styles = styles;
		this.vmm = vmm;
		
		this.emptyPanel = new NoResultPanel();
		this.diffusionTableFactory = new DiffusionTableFactory(appManager);
		
		this.setBackground(Color.white);
		
		initPanel();
		this.add(emptyPanel);

	}
	
	private final void initPanel() {
		// Basic setup for this parent panel

		selectionPanel = new JPanel();
		mainPanel = new JPanel();
		
		subnetPanel = new SubnetCreatorPanel(networkManager, styles, vmm, appManager);
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
		if(showResult) {
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

		final String[] cols = diffusionTableFactory.getAvailableOutputColumns();

		columnNameComboBox = new JComboBox<String>(cols);

		columnNameComboBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String columnName = getColumnName();
				columnNameComboBox.setSelectedItem(columnName);
				columnNameSelected(columnName);
			}
		});

		columnNameComboBox.addPopupMenuListener(new PopupMenuListener() {
			public void popupMenuCanceled(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
			}

			public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
				columnNameComboBox
						.setModel(new DefaultComboBoxModel(diffusionTableFactory.getAvailableOutputColumns()));
			}
		});

		if (diffusionTableFactory.getAvailableOutputColumns().length != 0) {
			columnNameSelected(getColumnName());
		}
	}

	private void columnNameSelected(String columnName) {

		final DiffusionTable diffusionTable = diffusionTableFactory.createTable(columnName);
		
		if (columnName.endsWith("_rank")) {
			System.out.println("Switching to heat");
			setSelectionPanel(new RankSelectionPanel(networkManager, diffusionTable, "Rank"));
		} else if (columnName.endsWith("_heat")) {
			System.out.println("Switching to rank");
			setSelectionPanel(new HeatSelectionPanel(networkManager, diffusionTable, "Heat"));
		} else {
			setSelectionPanel(new JPanel());
		}
	}

	private void setSelectionPanel(JPanel panel) {
		bottomPanel.remove(selectionPanel);
		selectionPanel = panel;
		bottomPanel.add(selectionPanel, BorderLayout.CENTER);
		// this.add(selectionPanel);
		this.validate();
		this.repaint();
	}

	public void setColumnName(String columnName) {
		columnNameComboBox.setModel(new DefaultComboBoxModel(diffusionTableFactory.getAvailableOutputColumns()));
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

		final String[] cols = diffusionTableFactory.getAvailableOutputColumns();
		final DefaultComboBoxModel<String> model = new DefaultComboBoxModel<>(cols);
		columnNameComboBox.setModel(model);

		if (cols == null || cols.length == 0) {
			columnNameComboBox.setEnabled(false);
			swapPanel(false);
		} else {
			columnNameComboBox.setEnabled(true);
			swapPanel(true);
		}
	}

	@Override
	public void handleEvent(ColumnCreatedEvent evt) {
		
		final CyTable sourceTable = evt.getSource();
		final String columnName = evt.getColumnName();
		
		if(!sourceTable.equals(
				this.appManager.getCurrentNetwork()
				.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS))) {
			return;
		}
		
		if(columnName.startsWith("diffusion_")) {
			columnNameComboBox.setEnabled(true);
			this.setEnabled(true);
		}
	}

	@Override
	public void handleEvent(NetworkAddedEvent nae) {
		// Remove all local columns copied from original one.
		
		final String[] cols = diffusionTableFactory.getAvailableOutputColumns();
		
		final CyNetwork net = nae.getNetwork();
		final CyTable localTable = net.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		for(final String colName: cols) {
			final CyColumn col = localTable.getColumn(colName);
			if(col != null) {
				localTable.deleteColumn(colName);
			}
		}
		
		// Disable UI
		swapPanel(false);
	}
}
