package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.beans.PropertyChangeListener;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;


/**
 * Base class for the threshold slider panel 
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractSliderPanel extends JPanel implements PropertyChangeListener {

	// This is the data model for this GUI object
	protected final DiffusionTable diffusionTable;
	
	// GUI components
	protected final JSlider thresholdSlider;
	protected final SliderValueSetterPanel valuePanel;

	
	AbstractSliderPanel(
			final DiffusionTable diffusionTable,
			final String title, final String prefix, final String suffix) {

		this.diffusionTable = diffusionTable;

		// Setup base panel
		this.setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
		this.setBackground(Color.white);
		
		// Value text field panel
		JLabel label = new JLabel(prefix + title + suffix + ":");
		label.setFont(new Font("SansSerif", Font.BOLD, 14));
		JPanel labelPanel = new JPanel();
		labelPanel.setMaximumSize(new Dimension(1000, 20));
		labelPanel.setOpaque(false);
		labelPanel.setLayout(new BorderLayout());
		labelPanel.add(label, BorderLayout.LINE_START);

		this.valuePanel = new SliderValueSetterPanel(title);
		this.valuePanel.setOpaque(false);
		this.valuePanel.setMaximumSize(new Dimension(1000, 30));
		valuePanel.addPropertyChangeListener(this);

		// Create Slider
		this.thresholdSlider = createSlider();

		this.add(labelPanel);
		this.add(thresholdSlider);
		this.add(valuePanel);
	}
	
	protected void selectNodesOverThreshold(String heatColumn, Double threshold) {
		final CyNetwork network = this.diffusionTable.getAssociatedNetwork();
		final CyTable localTable = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		for (CyRow row : localTable.getAllRows()) {
			final Double heatValue = row.get(heatColumn, Double.class);
			
			if (heatValue >= threshold) {
				row.set(CyNetwork.SELECTED, Boolean.TRUE);
			} else {
				row.set(CyNetwork.SELECTED, Boolean.FALSE);
			}
		}
	}

	protected abstract JSlider createSlider();

	public CyNetwork getNetwork() {
		return diffusionTable.getAssociatedNetwork();
	}

}
