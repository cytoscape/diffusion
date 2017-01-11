package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;

import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTable;


/**
 * Base class for the threshold slider panel 
 *
 */
@SuppressWarnings("serial")
public abstract class AbstractSliderPanel extends JPanel implements PropertyChangeListener {

	protected final JSlider thresholdSlider;
	protected final SliderValueSetterPanel valuePanel;
	protected final DiffusionTable diffusionTable;
	protected final DiffusionNetworkManager networkManager;

	AbstractSliderPanel(final DiffusionNetworkManager networkManager, final DiffusionTable diffusionTable,
			final String title, final String prefix, final String suffix) {

		this.diffusionTable = diffusionTable;
		this.networkManager = networkManager;

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

	protected abstract JSlider createSlider();

}
