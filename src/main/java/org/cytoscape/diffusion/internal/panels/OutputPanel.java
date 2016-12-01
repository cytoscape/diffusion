
package org.cytoscape.diffusion.internal;

import org.cytoscape.diffusion.internal.NodeTable;
import java.util.List;

import java.util.Collection;
import java.util.Collections;

import java.awt.Component;
import java.awt.GridLayout;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

import javax.swing.JPanel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JComboBox;
import javax.swing.Icon;
import javax.swing.JSlider;
import javax.swing.DefaultComboBoxModel;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import org.cytoscape.task.create.NewNetworkSelectedNodesAndEdgesTaskFactory;
import org.cytoscape.work.swing.DialogTaskManager;

import org.cytoscape.model.CyNetwork;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;
import org.cytoscape.application.CyApplicationManager;

public class OutputPanel extends JPanel implements CytoPanelComponent {

  private JLabel columnNameLabel = new JLabel("Column Name: ");
  private JComboBox columnNameComboBox;

  private JSlider thresholdValueSlider;

  private JButton makeSubnetButton = new JButton("Turn Selection into Subnet");

  private JLabel thresholdValueLabel = new JLabel("Theshold Value: ");
  private JTextField thresholdValueField = new JTextField("1.0");

  private NewNetworkSelectedNodesAndEdgesTaskFactory networkFactory;
  private CyApplicationManager applicationManager;
  private DialogTaskManager taskManager;
  private NodeTable nodeTable;

  public OutputPanel(CyApplicationManager applicationManager, DialogTaskManager taskManager, NewNetworkSelectedNodesAndEdgesTaskFactory networkFactory) {
    //Set listener on fields
    this.taskManager = taskManager;
    this.applicationManager = applicationManager;
    this.nodeTable = new NodeTable(applicationManager.getCurrentNetwork().getDefaultNodeTable());
    columnNameComboBox = new JComboBox(this.nodeTable.getAvaiableOutputColumns());
    ActionListener listener = this.getSelectNodesListener();
    PopupMenuListener menuListener = this.getMenuListener();
    thresholdValueField.addActionListener(listener);
    columnNameComboBox.addActionListener(listener);
    columnNameComboBox.addPopupMenuListener(menuListener);

    this.thresholdValueSlider = new JSlider(0, this.nodeTable.getTable().getRowCount());
    SliderChangeListener sliderChangeListener = new SliderChangeListener(this);
    this.thresholdValueSlider.addChangeListener(sliderChangeListener);

    makeSubnetButton.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        createSubnet();
      }
    });
    //Add fields and labels to GUI panel
    this.setLayout(new GridLayout(12, 1));
    this.add(columnNameLabel);
    this.add(columnNameComboBox);
    this.add(thresholdValueLabel);
    this.add(thresholdValueSlider);
    this.add(thresholdValueField);
    this.add(makeSubnetButton);
    this.updateThreshold();
  }

  private MenuListener getMenuListener() {
    return new MenuListener(this);
  }

  private ActionListener getSelectNodesListener() {
    return new SelectionListener(this);
  }

  public void createSubnet() {
    CyNetwork network = this.applicationManager.getCurrentNetwork();
    System.out.println(network.toString());
    this.taskManager.execute(this.networkFactory.createTaskIterator(network));
  }

  class SliderChangeListener implements ChangeListener {
    OutputPanel panel;
    public SliderChangeListener(OutputPanel panel) {
      this.panel = panel;
    }
    public void stateChanged(ChangeEvent e) {
        JSlider source = (JSlider)e.getSource();
        Integer index = source.getValue();
        List<Double> heats = this.panel.nodeTable.getTable().getColumn(this.panel.getOutputColumn()).getValues(Double.class);
        Collections.sort(heats);
        Collections.reverse(heats);
        Double newThreshold = heats.get(index);
        this.panel.setThreshold(newThreshold);
        this.panel.nodeTable.selectNodesOverThreshold(this.panel.getOutputColumn(), newThreshold);
    }
  }

  public class MenuListener implements PopupMenuListener {

      OutputPanel panel;

      public void popupMenuCanceled(PopupMenuEvent e) {
      }

      public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
      }


      public MenuListener(OutputPanel panel) {
        this.panel = panel;
      }

      public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
        this.panel.refreshOutputColumns();
      }
    };

  public class SelectionListener implements ActionListener {

    OutputPanel panel;

    public SelectionListener(OutputPanel panel) {
      this.panel = panel;
    }

    public void actionPerformed(ActionEvent e) {
      Double threshold = 0.0;
      String columnName = this.panel.getOutputColumn();
      if (e.getActionCommand() == "comboBoxChanged") {
        threshold = this.panel.nodeTable.getThreshold(columnName, 90);
      } else {
        threshold = this.panel.getThreshold();
      }
      this.panel.thresholdValueSlider.setValue(this.panel.nodeTable.getThresholdIndex(columnName, 90));
      this.panel.setThreshold(threshold);
      this.panel.nodeTable.selectNodesOverThreshold(columnName, threshold);
    }

  }

  public void updateThreshold() {
    String columnName = this.getOutputColumn();
    Double threshold = this.nodeTable.getThreshold(columnName, 90);
    this.thresholdValueSlider.setValue(this.nodeTable.getThresholdIndex(columnName, 90));
    this.setThreshold(threshold);
    this.nodeTable.selectNodesOverThreshold(columnName, threshold);
  }

  public void refreshOutputColumns() {
    this.columnNameComboBox.setModel(new DefaultComboBoxModel(this.nodeTable.getAvaiableOutputColumns()));
  }

  public CytoPanelName getCytoPanelName() {
    return CytoPanelName.EAST;
  }

  public String getOutputColumn() {
    return (String)this.columnNameComboBox.getSelectedItem();
  }

  public void setOutputColumn(String columnName) {
    this.columnNameComboBox.addItem(columnName);
    this.columnNameComboBox.setSelectedItem(columnName);
  }

  public Double getThreshold() {
    return Double.parseDouble(this.thresholdValueField.getText());
  }

  public void setThreshold(Double threshold) {
    this.thresholdValueField.setText(threshold.toString());
  }

  public Component getComponent() {
		return this;
	}

  public String getTitle() {
		return "Diffusion Output";
	}

  public Icon getIcon() {
    return null;
  }


}
