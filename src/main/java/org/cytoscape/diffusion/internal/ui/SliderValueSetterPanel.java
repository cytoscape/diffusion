package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

public class SliderValueSetterPanel extends JPanel {

	private static final long serialVersionUID = -6505890571956050665L;
	
	final static String SET_VALUE_EVENT = "VALUE_SET";
	
	private final JButton setButton;
	private final JTextField valueTextField;

	public SliderValueSetterPanel() {
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

		this.add(valueTextField, BorderLayout.CENTER);
		this.add(setButton, BorderLayout.EAST);
	}

	void setValue(final Number value) {
		this.valueTextField.setText(value.toString());
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
