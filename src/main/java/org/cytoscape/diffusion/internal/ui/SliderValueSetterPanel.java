package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.border.TitledBorder;

public class SliderValueSetterPanel extends JPanel {

	private static final long serialVersionUID = -6505890571956050665L;
	
	final static String SET_VALUE_EVENT = "VALUE_SET";
	
	private final JLabel label;
 	private final JButton setButton;
	private final JTextField valueTextField;

	public SliderValueSetterPanel(final String title) {
		this.label = new JLabel("Current " + title + ":");
		this.setButton = new JButton("Set");
		this.setButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setButtonPressed();
			}
		});
		this.valueTextField = new JTextField();
		this.valueTextField.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				setButtonPressed();
			}
		});

		this.setLayout(new BorderLayout());

		this.add(label, BorderLayout.LINE_START);
		this.add(valueTextField, BorderLayout.CENTER);
		this.add(setButton, BorderLayout.LINE_END);
	}

	void setValue(final Number value) {
		final String valText;
		if(value instanceof Integer) {
			valText = value.toString();
		} else {
			valText = String.format("%.5f", value);
		}
		this.valueTextField.setText(valText);
	}

	private final void setButtonPressed() {
		final String value = this.valueTextField.getText();
		try {
			final Double numberValue = Double.parseDouble(value);
			firePropertyChange(SET_VALUE_EVENT, "", numberValue);
		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(
					this, "Please enter valid number.  You entered: " + value, 
					"Invalid Number", JOptionPane.ERROR_MESSAGE);
		}
	}

}
