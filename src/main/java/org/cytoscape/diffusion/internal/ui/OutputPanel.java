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
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.diffusion.internal.util.DiffusionTableFactory;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

public class OutputPanel extends JPanel implements CytoPanelComponent {

	private JComboBox columnNameComboBox;
	private JPanel selectionPanel;
	private JPanel bottomPanel;

	private DiffusionTableFactory diffusionTableFactory;
	private DiffusionNetworkManager networkManager;

	public OutputPanel(DiffusionNetworkManager networkManager, Set<VisualStyle> styles,
			final CyApplicationManager appManager, final VisualMappingManager vmm) {
		this.networkManager = networkManager;
		this.diffusionTableFactory = new DiffusionTableFactory(appManager);

		// Basic setup for this parent panel
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBackground(Color.white);

		selectionPanel = new JPanel();
		final SubnetCreatorPanel subnetPanel = new SubnetCreatorPanel(networkManager, styles, vmm, appManager);
		subnetPanel.setOpaque(false);
		subnetPanel.setMaximumSize(new Dimension(1200, 56));
		
		bottomPanel = new JPanel();
		bottomPanel.setBackground(Color.white);
		bottomPanel.setLayout(new BorderLayout());
		bottomPanel.add(selectionPanel, BorderLayout.CENTER);

		final JPanel columnSelectorPanel = createSelector();

		JPanel mainPanel = new JPanel();
		mainPanel.setMaximumSize(new Dimension(1200, 160));
		final Border padding = BorderFactory.createEmptyBorder(0, 5, 0, 5);
		mainPanel.setBackground(Color.WHITE);
		mainPanel.setLayout(new BorderLayout());
		mainPanel.setBorder(BorderFactory.createTitledBorder("Node Selector"));
		
		columnSelectorPanel.setBorder(padding);
		bottomPanel.setBorder(padding);
		mainPanel.add(columnSelectorPanel, BorderLayout.NORTH);
		mainPanel.add(bottomPanel, BorderLayout.CENTER);

		this.add(mainPanel);
		this.add(subnetPanel);
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

		columnNameComboBox = new JComboBox(diffusionTableFactory.getAvailableOutputColumns());

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
				System.out.println("Looking for columns");
				System.out.println(diffusionTableFactory.getAvailableOutputColumns());
				columnNameComboBox
						.setModel(new DefaultComboBoxModel(diffusionTableFactory.getAvailableOutputColumns()));
			}
		});

		if (diffusionTableFactory.getAvailableOutputColumns().length != 0) {
			columnNameSelected(getColumnName());
		}
	}

	private void columnNameSelected(String columnName) {
		System.out.println("Column name selected");

		System.out.println(columnName);
		DiffusionTable diffusionTable = diffusionTableFactory.createTable(columnName);
		if (columnName.endsWith("_heat")) {
			System.out.println("Switching to heat");
			setSelectionPanel(new HeatSelectionPanel(networkManager, diffusionTable, "Heat Threshold"));
		} else if (columnName.endsWith("_rank")) {
			System.out.println("Switching to rank");
			setSelectionPanel(new RankSelectionPanel(networkManager, diffusionTable, "Rank Threshold"));
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
}
