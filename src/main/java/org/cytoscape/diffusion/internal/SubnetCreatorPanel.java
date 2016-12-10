package org.cytoscape.diffusion.internal;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by sage on 12/9/2016.
 */

public class SubnetCreatorPanel extends JPanel {

    private JButton makeSubnetButton = new JButton("Turn Selection into Subnet");

    SubnetCreatorPanel(DiffusionNetworkManager networkManager) {
        makeSubnetButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                networkManager.createSubnet();
            }
        });
        this.add(makeSubnetButton);
    }


}
