package org.cytoscape.diffusion.internal;

import org.cytoscape.model.CyNetwork;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualStyle;

import javax.swing.*;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**
 * Created by sage on 12/9/2016.
 */

public class SubnetCreatorPanel extends JPanel {

    private JComboBox styleComboBox;
    private Set<VisualStyle> visualStyles;

    private JButton makeSubnetButton = new JButton("Turn Selection into Subnet");

    SubnetCreatorPanel(DiffusionNetworkManager networkManager, Set<VisualStyle> styles) {

        visualStyles = styles;

        List<String> styleNames = new ArrayList<String>();
        for (VisualStyle style : styles) {
            styleNames.add(style.getTitle());
        }

        styleComboBox = new JComboBox(styleNames.toArray());

        makeSubnetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                CyNetworkView subnetwork = networkManager.createSubnet();
                getSelectedStyle().apply(subnetwork);
                subnetwork.updateView();

            }
        });
        this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
        styleComboBox.setMaximumSize(new Dimension(Short.MAX_VALUE, 55));
        this.add(styleComboBox);
        makeSubnetButton.setMaximumSize(new Dimension(Short.MAX_VALUE, 55));
        this.add(makeSubnetButton);
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
