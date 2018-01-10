package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.diffusion.internal.task.EmptyTaskMonitor;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.AbstractNetworkCollectionTask;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.vizmap.VisualMappingManager;
import org.cytoscape.view.vizmap.VisualStyle;
import org.cytoscape.work.ObservableTask;
import org.cytoscape.work.Task;
import org.cytoscape.work.TaskIterator;

/**
 * Created by sage on 12/9/2016.
 */

public class SubnetCreatorPanel extends JPanel {

	private static final long serialVersionUID = 7578596629235573519L;

	private final NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory;
	private final DiffusionTableManager tableManager;

	private JComboBox<String> styleComboBox;
	private Set<VisualStyle> visualStyles;

	private final VisualMappingManager vmm;
	
	private final CyApplicationManager appManager;

	SubnetCreatorPanel(DiffusionTableManager tableManager, Set<VisualStyle> styles, final VisualMappingManager vmm,
			final NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory, CyApplicationManager appManager) {

		this.createSubnetworkFactory = createSubnetworkFactory;
		this.tableManager = tableManager;
		this.vmm = vmm;
		this.appManager = appManager;

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

				createSubnetwork();

			}
		});

		this.add(styleLabel, BorderLayout.LINE_START);
		this.add(styleComboBox, BorderLayout.CENTER);
		this.add(makeSubnetButton, BorderLayout.LINE_END);
	}

	private final void createSubnetwork() {
		final VisualStyle style = getSelectedStyle();
		final CyNetwork network = this.tableManager.getCurrentTable().getAssociatedNetwork();
		final CyNetworkView view = createSubnet(network);
		if (view == null)
			return;
		appManager.setCurrentNetwork(view.getModel());
		vmm.setVisualStyle(style, view);
		style.apply(view);
		view.fitContent();
		view.updateView();
	}

	private final VisualStyle getSelectedStyle() {
		for (VisualStyle style : visualStyles) {
			if (style.getTitle() == styleComboBox.getSelectedItem()) {
				return style;
			}
		}
		return null;
	}

	public CyNetworkView createSubnet(final CyNetwork network) {

		TaskIterator taskIterator = createSubnetworkFactory.createTaskIterator(network);
		// final TaskIterator finalIterator = new TaskIterator();
		AbstractNetworkCollectionTask viewTask = null;
		try {
			while (taskIterator.hasNext()) {
				final Task task = taskIterator.next();
				task.run(new EmptyTaskMonitor());
				// finalIterator.append(task);
				if (task instanceof ObservableTask) {
					viewTask = (AbstractNetworkCollectionTask) task;
				}
			}
		} catch (Exception e) {
		}
		if (viewTask == null)
			return null;
		final Collection<?> result = ((ObservableTask) viewTask).getResults(Collection.class);
		return ((CyNetworkView) result.iterator().next());
	}
}