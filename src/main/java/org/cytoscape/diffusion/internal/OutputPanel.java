package org.cytoscape.diffusion.internal;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

class OutputPanel extends JPanel implements CytoPanelComponent {

	private JComboBox columnNameComboBox;
	private JPanel selectionPanel;

	private DiffusionTableFactory diffusionTableFactory;
	private DiffusionNetworkManager networkManager;

	OutputPanel(DiffusionNetworkManager networkManager) {
	    this.networkManager = networkManager;
	    this.diffusionTableFactory = new DiffusionTableFactory(networkManager.getNodeTable());
	    selectionPanel = new JPanel();
	    SubnetCreatorPanel subnetPanel = new SubnetCreatorPanel(networkManager);
        configureColumnNameComboBox();
        //this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(columnNameComboBox);
        this.add(subnetPanel);
        this.add(selectionPanel);
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
            public void popupMenuCanceled(PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                columnNameComboBox.setModel(new DefaultComboBoxModel(diffusionTableFactory.getAvailableOutputColumns()));
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
	        setSelectionPanel(new HeatSelectionPanel(networkManager, diffusionTable));
        } else if (columnName.endsWith("_rank")) {
	        System.out.println("Switching to rank");
            setSelectionPanel(new RankSelectionPanel(networkManager, diffusionTable));
        } else {
            setSelectionPanel(new JPanel());
        }
    }

    private void setSelectionPanel(JPanel panel) {
	    this.remove(selectionPanel);
	    selectionPanel = panel;
	    this.add(selectionPanel);
	    this.validate();
	    this.repaint();
    }

    public void setColumnName(String columnName) {
	    columnNameComboBox.setModel(new DefaultComboBoxModel(diffusionTableFactory.getAvailableOutputColumns()));
	    columnNameComboBox.setSelectedItem(columnName);
	    columnNameSelected(columnName);
    }

    private String getColumnName() {
	    return (String)columnNameComboBox.getSelectedItem();
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
