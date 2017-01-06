package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeListener;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;

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
			final String title) {

		this.diffusionTable = diffusionTable;
		this.networkManager = networkManager;

		// Setup base panel
		final Border titleBorder = BorderFactory.createTitledBorder(title);
		this.setBorder(titleBorder);
		this.setLayout(new BorderLayout());

		// Value text field panel
		this.valuePanel = new SliderValueSetterPanel();
		valuePanel.addPropertyChangeListener(this);

		// Create Slider
		this.thresholdSlider = createSlider();

		final JPanel contentsPanel = new JPanel();
		contentsPanel.setLayout(new GridLayout(2, 1));

		contentsPanel.add(thresholdSlider);
		contentsPanel.add(valuePanel);

		this.add(contentsPanel, BorderLayout.NORTH);
	}

	protected abstract JSlider createSlider();

	protected void setThreshold(final Integer index) {
		final Double threshold = diffusionTable.rankToHeat(index);
		networkManager.selectNodesOverThreshold(diffusionTable.getHeatColumnName(), threshold);
	}
}
