package org.cytoscape.diffusion.internal.ui;

import java.beans.PropertyChangeEvent;

import javax.swing.JLabel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTable;


@SuppressWarnings("serial")
public class RankSelectionPanel extends AbstractSliderPanel {

	RankSelectionPanel(DiffusionNetworkManager networkManager, DiffusionTable diffusionTable, final String title) {
		super(networkManager, diffusionTable, title, "Node ", "");
	}
	
	private final void setThreshold(final Integer index) {
		final Double threshold = diffusionTable.rankToHeat(index);
		networkManager.selectNodesOverThreshold(diffusionTable.getHeatColumnName(), threshold);
	}

	@Override
	protected JSlider createSlider() {
		final JSlider slider = new JSlider(1, diffusionTable.getMaxRank());
		slider.setMajorTickSpacing(50);
		slider.setMinorTickSpacing(10);
		slider.setPaintTicks(true);
		slider.setLabelTable(slider.createStandardLabels(50));
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

		final Integer ninteithPercentile = (diffusionTable.getMaxRank() / 100) * 10;
		setThreshold(ninteithPercentile);
		slider.setValue(ninteithPercentile);
		valuePanel.setValue(ninteithPercentile);

		return slider;
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SliderValueSetterPanel.SET_VALUE_EVENT) == false) {
			return;
		}

		final Double value = (Double) evt.getNewValue();
		final Integer max = diffusionTable.getMaxRank();
		if (1 <= value && max >= value) {
			this.thresholdSlider.setValue(value.intValue());
		} else if (max < value) {
			this.thresholdSlider.setValue(max);
		} else {
			this.thresholdSlider.setValue(1);
		}

	}

}
