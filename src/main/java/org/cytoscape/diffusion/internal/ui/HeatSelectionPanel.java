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
import org.cytoscape.task.create.NewEmptyNetworkViewFactory;

/**
 * Created by sage on 12/9/2016.
 */
public class HeatSelectionPanel extends JPanel implements PropertyChangeListener {

	private static final Border TITLE = BorderFactory.createTitledBorder("Heat threshold");

	private final JSlider thresholdSlider;
	private SliderValueSetterPanel valuePanel;

	private DiffusionTable diffusionTable;
	private DiffusionNetworkManager networkManager;

	HeatSelectionPanel(DiffusionNetworkManager networkManager, DiffusionTable diffusionTable) {
		this.diffusionTable = diffusionTable;
		this.networkManager = networkManager;

		this.thresholdSlider = new JSlider(0, diffusionTable.getMaxRank());
		this.thresholdSlider.setMajorTickSpacing(50);
	    this.thresholdSlider.setMinorTickSpacing(10);
	    this.thresholdSlider.setPaintTicks(true);

		this.valuePanel = new SliderValueSetterPanel();
		valuePanel.addPropertyChangeListener(this);

//		Hashtable labelTable = new Hashtable();
//		labelTable.put(0, new JLabel("0"));
//		labelTable.put(diffusionTable.getMaxRank(), new JLabel(diffusionTable.getMaxRank().toString()));
//		thresholdSlider.setLabelTable(labelTable);
		
	    this.thresholdSlider.setLabelTable(thresholdSlider.createStandardLabels(50));
		this.thresholdSlider.setPaintLabels(true);

		thresholdSlider.addChangeListener(new ChangeListener() {
			@Override
			public void stateChanged(ChangeEvent e) {
				final int newVal = thresholdSlider.getValue();
				setThreshold(newVal);
				valuePanel.setValue(newVal);
			}
		});
		Integer ninteithPercentile = (diffusionTable.getMaxRank() / 100) * 10;
		System.out.println("Percentile was");
		System.out.println(ninteithPercentile);
		setThreshold(ninteithPercentile);
		
		thresholdSlider.setValue(ninteithPercentile);
		valuePanel.setValue(ninteithPercentile);

		this.setLayout(new BorderLayout());
		this.setBorder(TITLE);
		
		JPanel contentsPanel = new JPanel();
		contentsPanel.setLayout(new GridLayout(2, 1));
		
		contentsPanel.add(thresholdSlider);
		contentsPanel.add(valuePanel);

		this.add(contentsPanel, BorderLayout.NORTH);
	}

	private void setThreshold(Integer index) {
		System.out.println("INdex isssss");
		System.out.println(index);
		Double threshold = diffusionTable.rankToHeat(index);
		networkManager.selectNodesOverThreshold(diffusionTable.getHeatColumnName(), threshold);
	}

	@Override
	public void propertyChange(PropertyChangeEvent evt) {
		if (evt.getPropertyName().equals(SliderValueSetterPanel.SET_VALUE_EVENT) == false) {
			return;
		}

		final Double value = (Double) evt.getNewValue();
		final Integer max = diffusionTable.getMaxRank();
		if (0 <= value && max >= value) {
			this.thresholdSlider.setValue(value.intValue());
		} else if(max<value) {
			this.thresholdSlider.setValue(max);
		} else {
			this.thresholdSlider.setValue(0);
		}

	}

}
