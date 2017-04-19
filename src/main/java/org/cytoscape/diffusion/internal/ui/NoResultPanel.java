package org.cytoscape.diffusion.internal.ui;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;

import javax.swing.JLabel;
import javax.swing.JPanel;

public class NoResultPanel extends JPanel {

	public NoResultPanel() {
		final GridLayout layout = new GridLayout(3, 1);
		this.setOpaque(false);
		this.setLayout(layout);
		final Dimension size = new Dimension(420,  400);
		this.setPreferredSize(size);
		this.setMinimumSize(size);
		this.setSize(size);
		
		JLabel label = new JLabel("No result yet");
		label.setOpaque(false);
		label.setHorizontalAlignment(JLabel.CENTER);
		label.setFont(new Font("sansserif", Font.PLAIN, 18));
		
		JLabel dummy1 = new JLabel();
		dummy1.setOpaque(false);
		JLabel dummy2 = new JLabel();
		dummy2.setOpaque(false);
		
		this.add(dummy1);
		this.add(label);
		this.add(dummy2);
		this.setVisible(true);
		this.repaint();
		this.updateUI();
	}
	
}
