package org.cytoscape.diffusion.internal;

import javax.swing.*;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import java.awt.*;
import java.util.Hashtable;

/**
 * Created by sage on 12/9/2016.
 */
public class RankSelectionPanel extends JPanel {

    private JSlider thresholdSlider;
    private JLabel label = new JLabel("Rank threshold");

    private DiffusionTable diffusionTable;
    private DiffusionNetworkManager networkManager;

    RankSelectionPanel(DiffusionNetworkManager networkManager, DiffusionTable diffusionTable) {
        this.diffusionTable = diffusionTable;
        this.networkManager = networkManager;
        thresholdSlider = new JSlider(0, 1000);
        Hashtable labelTable = new Hashtable();
        labelTable.put(0, new JLabel("0.0"));
        System.out.println("Max heat");
        System.out.println(diffusionTable.getMaxRank());
        String maxHeat = String.format("%.2f", diffusionTable.getMaxHeat());
        labelTable.put(diffusionTable.getMaxRank(), new JLabel(maxHeat));
        thresholdSlider.setLabelTable(labelTable);
        thresholdSlider.setPaintLabels(true);
        thresholdSlider.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                setThreshold(thresholdSlider.getValue());
            }
        });
        Integer ninteithPercentile = 900;
        System.out.println("Percentile was");
        System.out.println(ninteithPercentile);
        setThreshold(ninteithPercentile);
        thresholdSlider.setValue(ninteithPercentile);
        thresholdSlider.setInverted(true);
        this.setLayout(new BoxLayout(this, BoxLayout.Y_AXIS));
        //this.setLayout(new GridLayout(2, 1));
        this.add(label);
        this.add(thresholdSlider);

    }

    private void setThreshold(Integer index) {
        System.out.println("Thresh set you");
        System.out.println(index);
        Double threshold = (diffusionTable.getMaxHeat()/1000)*index;
        System.out.println(threshold);
        networkManager.selectNodesOverThreshold(diffusionTable.getHeatColumnName(), threshold);
    }


}
