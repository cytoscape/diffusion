package org.cytoscape.diffusion.internal.ui;

import java.beans.PropertyChangeEvent;
import java.util.Hashtable;

import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.diffusion.internal.util.DiffusionTable;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.model.CyNode;
import org.cytoscape.model.CyRow;
import org.cytoscape.model.CyTable;

@SuppressWarnings("serial")
public class RankSelectionPanel extends AbstractSliderPanel {

	RankSelectionPanel(DiffusionTable diffusionTable, final String title) {
		super(diffusionTable, title, "Node ", "");
	}

	private final void setThreshold(final Integer index) {

		final String rankColName = diffusionTable.getCurrentResult().getRankColumnName();
		final CyNetwork network = this.diffusionTable.getAssociatedNetwork();
		final CyTable localTable = network.getTable(CyNode.class, CyNetwork.LOCAL_ATTRS);
		
		for (final CyRow row : localTable.getAllRows()) {
			final Integer rank = row.get(rankColName, Integer.class);
			
			if (rank == null) { // ignore phantom nodes
				continue;
			}
			
			if (rank <= index) {
				row.set(CyNetwork.SELECTED, Boolean.TRUE);
			} else {
				row.set(CyNetwork.SELECTED, Boolean.FALSE);
			}
		}
		
		
	}

	@Override
	protected JSlider createSlider() {
		final Integer rankMax = diffusionTable.getCurrentResult().getMaxRank();
		final JSlider slider = new JSlider(1, rankMax);
		slider.setOpaque(false);

		int delta = rankMax / 4;

		final Hashtable labels = slider.createStandardLabels(delta);
		slider.setLabelTable(labels);

		slider.setMajorTickSpacing(delta);
		slider.setPaintTicks(true);
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

		final Double maxRank = diffusionTable.getCurrentResult().getMaxRank().doubleValue();
		final Integer ninteithPercentile = ((Double) (maxRank * 0.1d)).intValue();
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
		final Integer max = diffusionTable.getCurrentResult().getMaxRank();
		if (1 <= value && max >= value) {
			this.thresholdSlider.setValue(value.intValue());
		} else if (max < value) {
			this.thresholdSlider.setValue(max);
		} else {
			this.thresholdSlider.setValue(1);
		}

	}

}
