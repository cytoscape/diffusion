package org.cytoscape.diffusion.internal.ui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import javax.swing.BorderFactory;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;

import org.cytoscape.application.CyApplicationManager;
import org.cytoscape.diffusion.internal.task.EmptyTaskMonitor;
import org.cytoscape.diffusion.internal.util.DiffusionTableManager;
import org.cytoscape.model.CyNetwork;
import org.cytoscape.task.create.NewNetworkSelectedNodesOnlyTaskFactory;
import org.cytoscape.task.read.LoadVizmapFileTaskFactory;
import org.cytoscape.view.model.CyNetworkView;
import org.cytoscape.view.presentation.RenderingEngineManager;
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

	private static final String STYLES = "/styles.xml";

	private final NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory;
	private final DiffusionTableManager tableManager;

	private JComboBox<String> styleComboBox;
	private final LoadVizmapFileTaskFactory vizmapLoader;
	private Set<VisualStyle> visualStyles = null;

	private final VisualMappingManager vmm;
	private final CyApplicationManager appManager;
	final RenderingEngineManager renderingEngineMgr;


	SubnetCreatorPanel(DiffusionTableManager tableManager, LoadVizmapFileTaskFactory vizmapLoader, final VisualMappingManager vmm,
			final NewNetworkSelectedNodesOnlyTaskFactory createSubnetworkFactory, CyApplicationManager appManager,
			final RenderingEngineManager renderingEngineMgr) {

		this.createSubnetworkFactory = createSubnetworkFactory;
		this.tableManager = tableManager;
		this.vizmapLoader = vizmapLoader;
		this.vmm = vmm;
		this.appManager = appManager;
		this.renderingEngineMgr = renderingEngineMgr;

		this.setLayout(new BorderLayout());
		this.setMaximumSize(new Dimension(1000, 100));
		this.setBorder(BorderFactory.createTitledBorder("Create subnetwork from selected nodes"));

		JLabel styleLabel = new JLabel("Visual Style: ");

		//	visualStyles = styles;
		//	visualStyles.add(defStyle);

		//	final List<String> styleNames = styles.stream().map(style -> style.getTitle()).collect(Collectors.toList());
		//	styleComboBox = new JComboBox<>(styleNames.toArray(new String[styleNames.size()]));
		styleComboBox = new JComboBox<>();
		//	styleComboBox.setSelectedItem(defStyle.getTitle());
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

	protected void updateStyles() {
		
		if (this.visualStyles == null) {
			final VisualStyle defStyle = vmm.getDefaultVisualStyle();
			try {
				visualStyles = vizmapLoader.loadStyles(getClass().getResource(STYLES).openStream());
				visualStyles.add(defStyle);
				final List<String> styleNames = visualStyles.stream().map(style -> style.getTitle()).collect(Collectors.toList());
				styleComboBox.setModel(new DefaultComboBoxModel(styleNames.toArray()));
				styleComboBox.setSelectedItem(defStyle.getTitle());
			}
			catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private final void createSubnetwork() {
		final VisualStyle style = getSelectedStyle();
		final CyNetwork network = this.tableManager.getCurrentTable().getAssociatedNetwork();

		if (network.getDefaultNodeTable().getMatchingRows(CyNetwork.SELECTED, true).isEmpty()) {
			return;
		}
		CyNetworkView view = createSubnet(network);

		if (view == null) {
			return;
		}
		// createSubnetworkFactory fails to run CopyExistingViewTask for some
		// reason...
		// copyLayoutToNewView(network);
	
		appManager.setCurrentNetwork(view.getModel());
		vmm.setVisualStyle(style, view);
		vmm.setCurrentVisualStyle(style);
		style.apply(view);
		view.fitContent();
		view.updateView();
	}

	private final VisualStyle getSelectedStyle() {
		for (VisualStyle style : visualStyles) {
			if (style.getTitle().equals(styleComboBox.getSelectedItem())) {
				return style;
			}
		}
		return null;
	}

	public CyNetworkView createSubnet(final CyNetwork network) {
		TaskIterator taskIterator = createSubnetworkFactory.createTaskIterator(network);

		// final TaskIterator finalIterator = new TaskIterator();
		boolean viewCopied = false;
		ObservableTask viewTask = null;
		try {
			while (!viewCopied || taskIterator.hasNext()) {
				if (taskIterator.hasNext()) {
					final Task task = taskIterator.next();
					// finalIterator.append(task);
					task.run(new EmptyTaskMonitor());
					if(task.getClass().getName().endsWith("CopyExistingViewTask"))
						viewCopied = true;
					else if (task.getClass().getName().endsWith("RegisterNetworkTask")){
						viewTask = (ObservableTask) task;
					}
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		if (viewTask == null) {
			return null;
		}

		CyNetworkView result = viewTask.getResults(CyNetworkView.class);
		return result;
	}

}