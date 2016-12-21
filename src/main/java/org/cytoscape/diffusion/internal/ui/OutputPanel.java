package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Set;

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

	private DiffusionTableFactory diffusionTableFactory;
	private DiffusionNetworkManager networkManager;

	public OutputPanel(DiffusionNetworkManager networkManager, Set<VisualStyle> styles, 
			final CyApplicationManager appManager, final VisualMappingManager vmm) {
	    this.networkManager = networkManager;
	    this.diffusionTableFactory = new DiffusionTableFactory(appManager);
	    
	    selectionPanel = new JPanel();
	    SubnetCreatorPanel subnetPanel = new SubnetCreatorPanel(networkManager, styles, vmm, appManager);
        
	    
	    this.setLayout(new BorderLayout());
//	    this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        
        
        this.add(createSelector(), BorderLayout.NORTH);
        this.add(subnetPanel, BorderLayout.CENTER);
        this.add(selectionPanel, BorderLayout.SOUTH);
	}
	
	private final JPanel createSelector() {
		final JPanel selectorPanel = new JPanel();
		selectorPanel.setLayout(new BorderLayout());
		
        configureColumnNameComboBox();
//        columnNameComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 60));
        
		final JLabel selectorLabel = new JLabel("Select Column");
		selectorPanel.add(selectorLabel, BorderLayout.WEST);
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
            public void popupMenuCanceled(PopupMenuEvent e) {}
            public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {}
            public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
                System.out.println("Looking for columns");
                System.out.println(diffusionTableFactory.getAvailableOutputColumns());
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
