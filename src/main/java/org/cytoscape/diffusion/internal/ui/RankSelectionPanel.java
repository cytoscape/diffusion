package org.cytoscape.diffusion.internal.ui;

import java.beans.PropertyChangeEvent;
import java.util.Hashtable;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTable;

@SuppressWarnings("serial")
public class RankSelectionPanel extends AbstractSliderPanel {

	private static final int MIN = 0;
	private static final int MAX = 1000;

	RankSelectionPanel(DiffusionNetworkManager networkManager, DiffusionTable diffusionTable, final String title) {
		super(networkManager, diffusionTable, title);
	}

	@Override
	protected void setThreshold(Integer index) {
		final Double threshold = (diffusionTable.getMaxHeat() / 1000) * index;
		networkManager.selectNodesOverThreshold(diffusionTable.getHeatColumnName(), threshold);
	}

	@Override
	protected JSlider createSlider() {

		final JSlider slider = new JSlider(MIN, MAX);

		final Hashtable labelTable = new Hashtable();
		labelTable.put(0, new JLabel("0.0"));

		System.out.println("Max heat");
		System.out.println(diffusionTable.getMaxRank());

		final String maxHeat = String.format("%.2f", diffusionTable.getMaxHeat());

		labelTable.put(1000, new JLabel(maxHeat));
		slider.setLabelTable(labelTable);
		slider.setPaintLabels(true);

		slider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				if (!slider.getValueIsAdjusting()) {
					final int newVal = slider.getValue();
					setThreshold(newVal);
					valuePanel.setValue(newVal);
				}
			}
		});

		Integer ninteithPercentile = 900;
		System.out.println("Percentile was");
		System.out.println(ninteithPercentile);
		setThreshold(ninteithPercentile);
		valuePanel.setValue(ninteithPercentile);

		slider.setValue(ninteithPercentile);
		slider.setInverted(true);

		return slider;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SliderValueSetterPanel.SET_VALUE_EVENT) == false) {
			return;
		}
		final Double original = (Double) evt.getNewValue();
		Double value = (MAX * original) / diffusionTable.getMaxHeat();
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
