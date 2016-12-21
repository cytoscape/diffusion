package org.cytoscape.diffusion.internal.ui;

import java.util.Hashtable;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.diffusion.internal.util.DiffusionTable;

/**
 * Created by sage on 12/9/2016.
 */
public class HeatSelectionPanel extends JPanel {

    private JSlider thresholdSlider;
    private JLabel label = new JLabel("Heat threshold");

    private DiffusionTable diffusionTable;
    private DiffusionNetworkManager networkManager;

    HeatSelectionPanel(DiffusionNetworkManager networkManager, DiffusionTable diffusionTable) {
        this.diffusionTable = diffusionTable;
        this.networkManager = networkManager;
        thresholdSlider = new JSlider(0, diffusionTable.getMaxRank());
        Hashtable labelTable = new Hashtable();
        labelTable.put(0, new JLabel("0"));
        labelTable.put(diffusionTable.getMaxRank(), new JLabel(diffusionTable.getMaxRank().toString()));
        thresholdSlider.setLabelTable(labelTable);
        thresholdSlider.setPaintLabels(true);
        thresholdSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
               setThreshold(thresholdSlider.getValue());
            }
        });
        Integer ninteithPercentile = (diffusionTable.getMaxRank()/100)*10;
        System.out.println("Percentile was");
        System.out.println(ninteithPercentile);
        setThreshold(ninteithPercentile);
        thresholdSlider.setValue(ninteithPercentile);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        this.add(label);
        this.add(thresholdSlider);

    }

    private void setThreshold(Integer index) {
        System.out.println("INdex isssss");
        System.out.println(index);
        Double threshold = diffusionTable.rankToHeat(index);
        networkManager.selectNodesOverThreshold(diffusionTable.getHeatColumnName(), threshold);
    }


}
