package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.diffusion.internal.util.DiffusionNetworkManager;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;

/**
 * Created by sage on 12/9/2016.
 */

public class SubnetCreatorPanel extends JPanel {

	private static final long serialVersionUID = 7578596629235573519L;
	
	private JComboBox<String> styleComboBox;
	private Set<VisualStyle> visualStyles;


	SubnetCreatorPanel(DiffusionNetworkManager networkManager, Set<VisualStyle> styles, final VisualMappingManager vmm,
			CyApplicationManager appManager) {

		final VisualStyle defStyle = vmm.getDefaultVisualStyle();
		
		this.setLayout(new BorderLayout());
		this.setMaximumSize(new Dimension(1000, 100));
		this.setBorder(BorderFactory.createTitledBorder("Create subnetwork from selected nodes"));

		JLabel styleLabel = new JLabel("Visual Style: ");
		
		visualStyles = styles;
		visualStyles.add(defStyle);

		final List<String> styleNames = styles.stream().map(style -> style.getTitle()).collect(Collectors.toList());
		styleComboBox = new JComboBox<>(styleNames.toArray(new String[styleNames.size()]));
		styleComboBox.setSelectedItem(defStyle.getTitle());
		styleComboBox.setToolTipText("Selected Visual Style will be applied to the new subnetwork.");
		
		JButton makeSubnetButton = new JButton("Create");
		makeSubnetButton.setMaximumSize(new Dimension(100, 40));
		makeSubnetButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				final VisualStyle style = getSelectedStyle();
				final CyNetworkView view = networkManager.createSubnet();
				appManager.setCurrentNetwork(view.getModel());
				vmm.setVisualStyle(style, view);
				style.apply(view);
				view.fitContent();
				view.updateView();
			}
		});
		
		this.add(styleLabel, BorderLayout.LINE_START);
		this.add(styleComboBox, BorderLayout.CENTER);
		this.add(makeSubnetButton, BorderLayout.LINE_END);
	}

	private final VisualStyle getSelectedStyle() {
		for (VisualStyle style : visualStyles) {
			if (style.getTitle() == styleComboBox.getSelectedItem()) {
				return style;
			}
		}
		return null;
	}
}