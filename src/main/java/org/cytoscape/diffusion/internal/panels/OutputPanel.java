
package org.cytoscape.diffusion.internal;

import java.awt.Component;
import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.Icon;

import org.cytoscape.application.swing.CytoPanelComponent;
import org.cytoscape.application.swing.CytoPanelName;

public class OutputPanel extends JPanel implements CytoPanelComponent {

  private JLabel columnNameLabel = new JLabel("Column Name: ");
  private JTextField columnNameField = new JTextField("diffusion_output");

  private JLabel thresholdValueLabel = new JLabel("Theshold Value: ");
  private JTextField thresholdValueField = new JTextField("1.0");

  public OutputPanel() {
    this.setLayout(new GridLayout(12, 1));
    this.add(columnNameLabel);
    this.add(columnNameField);
    this.add(thresholdValueLabel);
    this.add(thresholdValueField);
  }

  public CytoPanelName getCytoPanelName() {
    return CytoPanelName.EAST;
  }

  public void setOutputColumn(String columnName) {
    this.columnNameField.setText(columnName);
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
