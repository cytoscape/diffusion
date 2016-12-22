package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * Created by sage on 12/9/2016.
 */

public class SubnetCreatorPanel extends JPanel {

    private JComboBox styleComboBox;
    private Set<VisualStyle> visualStyles;

    private JButton makeSubnetButton = new JButton("Create Subnetwork");

    SubnetCreatorPanel(DiffusionNetworkManager networkManager, 
    		Set<VisualStyle> styles, final VisualMappingManager vmm, CyApplicationManager appManager) {
    	
    		final VisualStyle defStyle = vmm.getDefaultVisualStyle();
    		
    		this.setMaximumSize(new Dimension(1000, 100));
    		this.setBorder(BorderFactory.createTitledBorder("Subnetwork Creator"));
        
    		visualStyles = styles;

    		visualStyles.add(defStyle);
    		
        final List<String> styleNames = 
        		styles.stream()
        		.map(style->style.getTitle())
        		.collect(Collectors.toList());

        styleComboBox = new JComboBox(styleNames.toArray());
        styleComboBox.setSelectedItem(defStyle.getTitle());
        
        makeSubnetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
            		final VisualStyle style = getSelectedStyle();
            		System.out.println(style.getTitle());
            		
                final CyNetworkView view = networkManager.createSubnet();
                appManager.setCurrentNetwork(view.getModel());
                
                vmm.setVisualStyle(style, view);
                style.apply(view);
                view.fitContent();
                view.updateView();

            }
        });
        this.setLayout(new BorderLayout());
        this.add(styleComboBox, BorderLayout.WEST);
        this.add(makeSubnetButton, BorderLayout.CENTER);
    }


    private VisualStyle getSelectedStyle() {
        for (VisualStyle style: visualStyles) {
            if (style.getTitle() == (String)styleComboBox.getSelectedItem()) {
                return style;
            }
        }
        return null;
    }


}
