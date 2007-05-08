/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.launch.ui;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchImages;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * 
 */
public class ParallelTab extends PLaunchConfigurationTab {
	// Program arguments UI widgets
	// private Combo startupCombo = null;
	// private Composite dynamicComp = null;

	private final class ContentsChangedListener implements IRMLaunchConfigurationContentsChangedListener {

		public void handleContentsChanged(IRMLaunchConfigurationDynamicTab rmDynamicTab) {
			// The buttons and messages have to be updated based on anything
			// that has changed in the dynamic portion of the launch tab.
			updateLaunchConfigurationDialog();
		}
	}

	private final class WidgetListener extends SelectionAdapter {
		
		public void widgetSelected(SelectionEvent e) {

		    System.out.println("XXXXX widgetSelected");
		    
			if (e.getSource() == resourceManagerCombo) {
				rmSelectionChanged();
			}
			else if (e.getSource() == queueCombo) {
				queueSelectionChanged();
			}
		}
	}

	private Combo resourceManagerCombo;

	private Combo queueCombo;

	private final WidgetListener combosListener = new WidgetListener();

	// The composite that holds the RM's attributes for the launch configuration 
	private Composite launchConfigurationComposite;

	private final Map<Integer, IPQueue> queues = new HashMap<Integer, IPQueue>();
	private final Map<IPQueue, Integer> queueIndices = new HashMap<IPQueue, Integer>();
	private final Map<Integer, IResourceManager> resourceManagers =
		new HashMap<Integer, IResourceManager>(); 
	private final HashMap<IResourceManager, Integer> resourceManagerIndices =
		new HashMap<IResourceManager, Integer>();

	private ILaunchConfiguration launchConfiguration = null;

	private final Map<IResourceManager, IRMLaunchConfigurationDynamicTab> rmDynamicTabs = 
		new HashMap<IResourceManager, IRMLaunchConfigurationDynamicTab>();

	private final ContentsChangedListener launchContentsChangedListener = 
		new ContentsChangedListener(); 
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		setErrorMessage(null);
		final IResourceManager rm = getResourceManagerFromCombo();
		if (rm == null) {
			setErrorMessage("No Resource Manager has been selected");
			return false;
		}
		final IPQueue queue = getQueueFromCombo();
		if (queue == null) {
			//TODO setErrorMessage("No Queue has been selected");
			//TODO return false;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
		final Composite launchComp = getLaunchConfigurationComposite();
		if (rmDynamicTab == null || launchComp == null) {
			setErrorMessage("No Launch Control Available for Resource Manager: " + rm.getName());
			return false;
		}
		RMLaunchValidation validation = rmDynamicTab.canSave(launchComp, rm, queue);
		if (!validation.isSuccess()) {
			setErrorMessage(validation.getMessage());
			return false;
		}
		return true;
	}

	/**
	 * @see ILaunchConfigurationTab#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
		
		GridLayout topLayout = new GridLayout();
		comp.setLayout(topLayout);
		// createVerticalSpacer(comp, 1);

		final Composite parallelComp = new Composite(comp, SWT.NONE);
		parallelComp.setLayout(createGridLayout(2, false, 0, 0));
		parallelComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		createVerticalSpacer(parallelComp, 2);

		IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
		if (universe == null) {
			return;
		}
		
		IResourceManager[] rms = getStartedResourceManagers(universe);
		new Label(parallelComp, SWT.NONE).setText("Select resource manager:");
		
		resourceManagerCombo = new Combo(parallelComp, SWT.READ_ONLY);
		for (int i = 0; i < rms.length; i++) {
			resourceManagerCombo.add(rms[i].getName());
			resourceManagers.put(i, rms[i]);
			resourceManagerIndices.put(rms[i], i);
		}
		resourceManagerCombo.addSelectionListener(combosListener);
 
		new Label(parallelComp, SWT.NONE).setText("Select queue:");
		queueCombo = new Combo(parallelComp, SWT.READ_ONLY);

		final IResourceManager rm = rms.length > 0 ? rms[0] : null;
		loadQueueCombo(rm);
		
		queueCombo.addSelectionListener(combosListener);

		createVerticalSpacer(parallelComp, 2);

		// The composite that holds the RM's attributes for the launch configuration
		Group attrGroup = new Group(parallelComp, SWT.SHADOW_ETCHED_IN);
		attrGroup.setText("Launch Attributes");
		attrGroup.setLayout(createGridLayout(1, false, 0, 0));
		attrGroup.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));

		createVerticalSpacer(attrGroup, 2);
		
		final Composite launchComp = createLaunchAttributeControlComposite(attrGroup, 2);
		setLaunchConfigurationComposite(launchComp);
		
		createVerticalSpacer(attrGroup, 2);
		
		createVerticalSpacer(parallelComp, 2);
		resourceManagerCombo.deselectAll();
 	}

	/**
	 * @see ILaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return LaunchImages.getImage(LaunchImages.IMG_PARALLEL_TAB);
	}

	/**
	 * @see ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchMessages.getResourceString("ParallelTab.Parallel");
	}

	/**
	 * @see ILaunchConfigurationTab#initializeFrom(ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		// cache the launch configuration for updates
		setLaunchConfiguration(configuration);
		
		try {
			IResourceManager rm = getResourceManagerFromConfiguration(configuration);
			if (rm == null) {
				setErrorMessage(LaunchMessages.getResourceString(
						"ParallelTab.Invalid_Resource_Manager"));
				return;
			}

			setResourceManagerComboSelection(rm);
			
			// load up the combos given that the configuration has selected
			// a resource manager
			loadQueueCombo(rm);
			
			IPQueue queue = getQueueFromName(rm, configuration.getAttribute(
					IPTPLaunchConfigurationConstants.QUEUE_NAME, EMPTY_STRING));
			if (queue != null) {
				setErrorMessage(LaunchMessages.getResourceString(
						"ParallelTab.Invalid_Queue"));
				return;
			}

			setQueueComboSelection(queue);
			
			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
			final Composite launchComp = getLaunchConfigurationComposite();
			if (rmDynamicTab == null || launchComp == null) {
				setErrorMessage("No Launch Control Available for Resource Manager: " +
						rm.getName());
				return;
			}
			
			// Update the dynamic portions of the launch configuration
			// tab.
			updateLaunchAttributeControls(rm, queue, getLaunchConfiguration());
			
		} catch (CoreException e) {
			setErrorMessage(LaunchMessages.getFormattedResourceString(
							"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION",
							e.getStatus().getMessage()));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		try {
			IResourceManager rm = getResourceManagerFromConfiguration(configuration);
			if (rm == null) {
				setErrorMessage(LaunchMessages.getResourceString(
						"ParallelTab.Invalid_Resource_Manager"));
				return false;
			}
			
			assert(rm == getResourceManagerFromCombo());

			// load up the combos given that the configuration has selected
			// a resource manager
			loadQueueCombo(rm);
			
			IPQueue queue = getQueueFromName(rm, configuration.getAttribute(
					IPTPLaunchConfigurationConstants.QUEUE_NAME, EMPTY_STRING));
			if (queue == null) {
				//TODO setErrorMessage(LaunchMessages.getResourceString(
                //				"ParallelTab.Invalid_Queue"));
				// TODO	return false;
			}
			
			assert(queue == getQueueFromCombo());

			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
			if (rmDynamicTab == null) {
				setErrorMessage("No Launch Configuration Available for Resource Manager: " +
						rm.getName());
				return false;
			}
			RMLaunchValidation validation = rmDynamicTab.isValid(configuration, rm, queue);
			if (!validation.isSuccess()) {
				setErrorMessage(validation.getMessage());
				return false;
			}
			
			return true;

		} catch (CoreException e) {
			setErrorMessage(LaunchMessages
					.getFormattedResourceString(
							"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION",
							e.getStatus().getMessage()));
			return false;
		}
	}

	/**
	 * @see ILaunchConfigurationTab#performApply(ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		final IResourceManager rm = getResourceManagerFromCombo();
		setResourceManagerInConfiguration(configuration, rm);
		configuration.setAttribute(IPTPLaunchConfigurationConstants.QUEUE_NAME,
				getQueueNameFromCombo());
		if (rm != null) {
			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
			if (rmDynamicTab == null) {
				setErrorMessage("No Launch Configuration Available for Resource Manager: " +
						rm.getName());
				return;
			}
			final IPQueue queue = getQueueFromCombo();
			if (queue != null) {
				RMLaunchValidation validation = rmDynamicTab.performApply(configuration, rm, queue);
				if (!validation.isSuccess()) {
					setErrorMessage(validation.getMessage());
					return;
				}
			}
		}
		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		final IResourceManager rm = getResourceManagerDefault();
		if (rm == null) {
			setErrorMessage("Cannot find a resource manager.");
			return;
		}
		setResourceManagerInConfiguration(configuration, rm);
		final IPQueue queue = getQueueDefault(rm);
		final String queueName = (queue != null ? queue.getName() : "");
		configuration.setAttribute(IPTPLaunchConfigurationConstants.QUEUE_NAME,
				queueName);
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
		if (rmDynamicTab == null) {
			setErrorMessage("Cannot find a launch configuration for this resource manager.");
			return;
		}
		rmDynamicTab.setDefaults(configuration, rm, queue);
	}

	/**
	 * @see ILaunchConfigurationTab#setLaunchConfigurationDialog(ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	private Composite createLaunchAttributeControlComposite(Composite parent, int colspan) {
		Composite attrComp = new Composite(parent, SWT.NONE);
//		attrComp.setLayout(createGridLayout(1, false, 0, 0));
//		attrComp.setLayoutData(spanGridData(GridData.FILL_HORIZONTAL, 5));
		GridLayout launchConfigLayout = new GridLayout();
		launchConfigLayout.marginHeight = 0;
		launchConfigLayout.marginWidth = 0;
		launchConfigLayout.numColumns = 1;
		attrComp.setLayout(launchConfigLayout);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.horizontalSpan = colspan;
		attrComp.setLayoutData(gd);
		return attrComp;
	}

	/**
	 * Create a new dynamic tab, given the resource manager.
	 * 
	 * @param rm
	 * @return
	 * @throws CoreException
	 */
	private IRMLaunchConfigurationDynamicTab createRMLaunchConfigurationDynamicTab(
			final IResourceManager rm) throws CoreException {
		
		final AbstractRMLaunchConfigurationFactory rmFactory = 
			PTPLaunchPlugin.getDefault().getRMLaunchConfigurationFactory(rm);
		if (rmFactory == null) {
			return null;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab =	rmFactory.create(rm);
		if (rmDynamicTab == null) {
			return null;
		}
		rmDynamicTab.addContentsChangedListener(launchContentsChangedListener);
		return rmDynamicTab;
	}

	/**
	 * @return the launchConfiguration
	 */
	private ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	private Composite getLaunchConfigurationComposite() {
		return launchConfigurationComposite;
	}

	private IPQueue getQueueDefault(IResourceManager rm) {
		final IPQueue[] queues = rm.getQueues();
		if (queues.length == 0) {
			return null;
		}
		return queues[0];
	}

	private IPQueue getQueueFromCombo() {
		if (queueCombo != null) {
			int i = queueCombo.getSelectionIndex();
			return queues.get(i);
		}
		return null;
	}

	private IPQueue getQueueFromName(IResourceManager rm, String queueName) {
		if (rm == null) {
			return null;
		}
		
		IPQueue[] queues = rm.getQueues();
		
		for (IPQueue queue : queues) {
			if (queue.getName().equals(queueName))
				return queue;
		}
		return null;
	}

	private String getQueueNameFromCombo() {
		IPQueue queue = getQueueFromCombo();
		if (queue == null) {
			return "";
		}
		return queue.getName();
	}

	private IResourceManager getResourceManagerDefault() {
		IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
		if (universe == null) {
			return null;
		}
		
		IResourceManager[] rms = getStartedResourceManagers(universe);
		if (rms.length == 0) {
			return null;
		}
		return rms[0];
	}

	private IResourceManager getResourceManagerFromCombo() {
		if (resourceManagerCombo != null) {
			int i = resourceManagerCombo.getSelectionIndex();
			return resourceManagers.get(i);
		}
		return null;
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	private IResourceManager getResourceManagerFromConfiguration(
			ILaunchConfiguration configuration) throws CoreException {
		final String rmUniqueName = configuration.getAttribute(
						IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_UNIQUENAME, EMPTY_STRING);
		return getResourceManagerFromUniqueName(rmUniqueName);
	}

	private IResourceManager getResourceManagerFromUniqueName(String rmUniqueName) {
		IPUniverse universe = PTPCorePlugin.getDefault().getModelManager().getUniverse();
		if (universe == null) {
			return null;
		}
		
		IResourceManager[] rms = getStartedResourceManagers(universe);
		
		for (IResourceManager rm : rms) {
			if (rm.getUniqueName().equals(rmUniqueName)) {
				return rm;
			}
		}
		return null;
	}

	private String getResourceManagerUniqueNameFromCombo() {
		IResourceManager rm = getResourceManagerFromCombo();
		if (rm == null) {
			return "";
		}
		return rm.getUniqueName();
	}
	
	/**
	 * Returns a cached launch configuration dynamic tab.  If it isn't in the cache
	 * then it creates a new one, and puts it in the cache.
	 * 
	 * @param rm
	 * @return
	 */
	private IRMLaunchConfigurationDynamicTab getRMLaunchConfigurationDynamicTab(
			final IResourceManager rm) {
		if (!rmDynamicTabs.containsKey(rm)) { 
			try {
				IRMLaunchConfigurationDynamicTab rmDynamicTab =
					createRMLaunchConfigurationDynamicTab(rm);
				rmDynamicTabs.put(rm, rmDynamicTab);
				return rmDynamicTab;
			} catch (CoreException e) {
				setErrorMessage(e.getMessage());
				PTPLaunchPlugin.errorDialog(e.getMessage(), e);
				return null;
			}
		}
		return rmDynamicTabs.get(rm);
	}

	/**
	 * @param universe
	 * @return
	 */
	private IResourceManager[] getStartedResourceManagers(IPUniverse universe) {
		IResourceManager[] rms = universe.getResourceManagers();
		ArrayList<IResourceManager> startedRMs = 
			new ArrayList<IResourceManager>(rms.length);
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED) {
				startedRMs.add(rm);
			}
		}
		return startedRMs.toArray(new IResourceManager[startedRMs.size()]);
	}

	/**
	 * After a resource manager is chosen the machine and
	 * queues combo boxes must be re-loaded.
	 * This method does just that.
	 * @param rm 
	 */
	private void loadQueueCombo(IResourceManager rm) {
		queueCombo.removeAll();
		queues.clear();

		if (rm == null) {
			return;
		}

		final IPQueue[] qs = rm.getQueues();
		for (int i = 0; i < qs.length; i++) {
			queueCombo.add(qs[i].getName());
			queues.put(i, qs[i]);
			queueIndices.put(qs[i], i);
		}
		queueCombo.select(0);
	}

	/**
     * 
     */
    private void queueSelectionChanged() {
        // If a different queue is chosen the
        // attributes' controls must be updated.
        final IResourceManager rm = getResourceManagerFromCombo();
        final IPQueue queue = getQueueFromCombo();

        // Update the dynamic portions of the launch configuration
        // tab.
        updateLaunchAttributeControls(rm, queue, getLaunchConfiguration());

        /*
         * Updates the buttons and message in this page's launch
         * configuration dialog.
         */
        updateLaunchConfigurationDialog();
    }

	/**
     * 
     */
    private void rmSelectionChanged() {
        /*
         * After a resource manager is chosen the machine and
         * queues combo boxes must be re-loaded, and the attributes'
         * controls must be updated.
         */
        IResourceManager rm = getResourceManagerFromCombo();
        loadQueueCombo(rm);
        final IPQueue queue = getQueueFromCombo();
    
        // Update the dynamic portions of the launch configuration
        // tab.
        updateLaunchAttributeControls(rm, queue, getLaunchConfiguration());
    
        /*
         * Updates the buttons and message in this page's launch
         * configuration dialog.
         */
        updateLaunchConfigurationDialog();
    }

	private void setLaunchConfiguration(ILaunchConfiguration configuration) {
		launchConfiguration = configuration;
	}

	private void setLaunchConfigurationComposite(Composite launchConfigurationComposite) {
		this.launchConfigurationComposite = launchConfigurationComposite;
	}

	private void setQueueComboSelection(IPQueue queue) {
		final Integer results = queueIndices.get(queue);
		int i = 0;
		if (results != null)
			i = results.intValue();
		queueCombo.select(i);
		queueSelectionChanged();
	}

	private void setResourceManagerComboSelection(IResourceManager rm) {
		final Integer results = resourceManagerIndices.get(rm);
		int i = 0;
		if (results != null)
			i = results.intValue();
		resourceManagerCombo.select(i);
		rmSelectionChanged();
	}

    /**
	 * @param configuration
	 * @param rm
	 */
	private void setResourceManagerInConfiguration(ILaunchConfigurationWorkingCopy configuration,
			final IResourceManager rm) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_UNIQUENAME,
				rm.getUniqueName());
	}

    /**
	 * This routine is called when either the resource manager or the
	 * queue has been changed via the combo boxes.  It's job is to
	 * regenerate the dynamic ui components, dependent on the resource
	 * manager and queue choice.
	 * 
	 * @param rm
	 * @param queue
	 * @param launchConfiguration
	 */
	private void updateLaunchAttributeControls(IResourceManager rm, IPQueue queue,
			ILaunchConfiguration launchConfiguration) {
		final Composite launchComp = getLaunchConfigurationComposite();
		for (Control child : launchComp.getChildren()) {
			child.dispose();
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
		if (rmDynamicTab != null) {
			try {
				rmDynamicTab.createControl(launchComp, rm, queue);
				rmDynamicTab.initializeFrom(launchComp, rm, queue, launchConfiguration);
			} catch (CoreException e) {
				setErrorMessage(e.getMessage());
				PTPLaunchPlugin.errorDialog(e.getMessage(), e);
			}
		}		
		launchComp.layout(true);
	}
}