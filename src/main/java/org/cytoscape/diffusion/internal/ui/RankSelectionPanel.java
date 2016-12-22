package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.GridLayout;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.util.Hashtable;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.border.Border;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTable;

public class RankSelectionPanel extends JPanel implements PropertyChangeListener {

	private static final long serialVersionUID = 7664910367871726699L;

	private static final Border TITLE = BorderFactory.createTitledBorder("Rank threshold");

	private final JSlider thresholdSlider;
	private final SliderValueSetterPanel valuePanel;

	private final DiffusionTable diffusionTable;
	private final DiffusionNetworkManager networkManager;

	private static final int MIN = 0;
	private static final int MAX = 1000;

	RankSelectionPanel(DiffusionNetworkManager networkManager, DiffusionTable diffusionTable) {
		this.diffusionTable = diffusionTable;
		this.networkManager = networkManager;

		this.thresholdSlider = new JSlider(MIN, MAX);
		this.valuePanel = new SliderValueSetterPanel();

		final Hashtable labelTable = new Hashtable();
		labelTable.put(0, new JLabel("0.0"));

		System.out.println("Max heat");
		System.out.println(diffusionTable.getMaxRank());

		final String maxHeat = String.format("%.2f", diffusionTable.getMaxHeat());

		labelTable.put(1000, new JLabel(maxHeat));
		thresholdSlider.setLabelTable(labelTable);
		thresholdSlider.setPaintLabels(true);
		
		thresholdSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int newVal = thresholdSlider.getValue();
				setThreshold(newVal);
				valuePanel.setValue(newVal);
			}
		});
		Integer ninteithPercentile = 900;
		System.out.println("Percentile was");
		System.out.println(ninteithPercentile);
		setThreshold(ninteithPercentile);
		valuePanel.setValue(ninteithPercentile);

		thresholdSlider.setValue(ninteithPercentile);
		thresholdSlider.setInverted(true);
		this.setLayout(new BorderLayout());
		this.setBorder(TITLE);

		valuePanel.addPropertyChangeListener(this);

		JPanel contentsPanel = new JPanel();
		contentsPanel.setLayout(new GridLayout(2, 1));
		
		contentsPanel.add(thresholdSlider);
		contentsPanel.add(valuePanel);

		this.add(contentsPanel, BorderLayout.NORTH);
	}

	private void setThreshold(Integer index) {
		System.out.println("Thresh set you");
		System.out.println(index);
		Double threshold = (diffusionTable.getMaxHeat() / 1000) * index;
		System.out.println(threshold);
		networkManager.selectNodesOverThreshold(diffusionTable.getHeatColumnName(), threshold);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SliderValueSetterPanel.SET_VALUE_EVENT) == false) {
			return;
		}
		final Double original = (Double) evt.getNewValue();
		Double value = (1000 * original) / diffusionTable.getMaxHeat();
		System.out.println("New value ===>  " + value);
		
		if (0 <= value && MAX >= value) {
			this.thresholdSlider.setValue(value.intValue());
		} else if (MAX < value) {
			this.thresholdSlider.setValue(MAX);
		} else {
			this.thresholdSlider.setValue(0);
		}

	}

}
