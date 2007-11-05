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

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchImages;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

/**
 * The Resources tab is used to specify the resources required for
 * a successful job launch. It is populated by the selected
 * resource manager (specified in the Main tab)
 */
public class ResourcesTab extends PLaunchConfigurationTab {
	private final class ContentsChangedListener implements IRMLaunchConfigurationContentsChangedListener {

		public void handleContentsChanged(IRMLaunchConfigurationDynamicTab rmDynamicTab) {
			// The buttons and messages have to be updated based on anything
			// that has changed in the dynamic portion of the launch tab.
			updateLaunchConfigurationDialog();
		}
	}

	private final class WidgetListener extends SelectionAdapter {
		
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource() == queueCombo) {
				queueSelectionChanged();
			}
		}
	}

	private Combo queueCombo;

	private final WidgetListener combosListener = new WidgetListener();

	// The composite that holds the RM's attributes for the launch configuration 
	private ScrolledComposite launchAttrsScrollComposite;

	private final Map<Integer, IPQueue> queues = new HashMap<Integer, IPQueue>();
	private final Map<IPQueue, Integer> queueIndices = new HashMap<IPQueue, Integer>();

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
		final IResourceManager rm = getResourceManager(getLaunchConfiguration());
		if (rm == null) {
			setErrorMessage(LaunchMessages.getResourceString("ResourcesTab.No_Resource_Manager")); //$NON-NLS-1$
			return false;
		}
		final IPQueue queue = getQueueFromCombo();
		if (queue == null) {
			setErrorMessage(LaunchMessages.getResourceString("ResourcesTab.No_Queue")); //$NON-NLS-1$
			return false;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
		final Composite launchComp = getLaunchAttrsScrollComposite();
		if (rmDynamicTab == null || launchComp == null) {
			setErrorMessage(LaunchMessages.getFormattedResourceString("ResourcesTab.No_Launch_Control", rm.getName())); //$NON-NLS-1$
			return false;
		}
		RMLaunchValidation validation = rmDynamicTab.canSave(launchComp, rm, queue);
		if (!validation.isSuccess()) {
			setErrorMessage(validation.getMessage());
			return false;
		}
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse.swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		final int numColumns = 2;
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);
	
		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);
	 
		new Label(comp, SWT.NONE).setText(LaunchMessages.getResourceString("ResourcesTab.Queue_Label")); //$NON-NLS-1$
		queueCombo = new Combo(comp, SWT.READ_ONLY);
		queueCombo.addSelectionListener(combosListener);

		// The composite that holds the RM's attributes for the launch configuration
		Group attrGroup = new Group(comp, SWT.SHADOW_ETCHED_IN);
		attrGroup.setText(LaunchMessages.getResourceString("ResourcesTab.Launch_Attributes")); //$NON-NLS-1$
		gd = new GridData(SWT.FILL, SWT.FILL, true, true);
		gd.horizontalSpan = numColumns;
		attrGroup.setLayoutData(gd);
		GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 1;
		attrGroup.setLayout(gridLayout);

		final ScrolledComposite scrollComp = createLaunchAttributeControlComposite(attrGroup,
				numColumns);
		setLaunchAttrsScrollComposite(scrollComp);
 	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	public Image getImage() {
		return LaunchImages.getImage(LaunchImages.IMG_PARALLEL_TAB);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return LaunchMessages.getResourceString("ResourcesTab.Resources"); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		// cache the launch configuration for updates
		setLaunchConfiguration(configuration);
		
		try {
			IResourceManager rm = getResourceManager(configuration);
			if (rm == null) {
				queueCombo.setEnabled(false);
				setErrorMessage(LaunchMessages.getResourceString(
						"ResourcesTab.No_Resource_Manager")); //$NON-NLS-1$
				return;
			}
			
			queueCombo.setEnabled(rm.getQueues().length > 1);
			
			// load up the combos given that the configuration has selected
			// a resource manager
			loadQueueCombo(rm);
			
			IPQueue queue = getQueueFromName(rm, configuration.getAttribute(
					IPTPLaunchConfigurationConstants.ATTR_QUEUE_NAME, EMPTY_STRING));
			if (queue == null) {
				setErrorMessage(LaunchMessages.getResourceString(
						"ResourcesTab.Invalid_Queue")); //$NON-NLS-1$
				return;
			}

			setQueueComboSelection(queue);
			
			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
			final Composite launchComp = getLaunchAttrsScrollComposite();
			if (rmDynamicTab == null || launchComp == null) {
				setErrorMessage(LaunchMessages.getFormattedResourceString(
						"ResourcesTab.No_Launch_Configuration", rm.getName())); //$NON-NLS-1$
				return;
			}
			
			// Update the dynamic portions of the launch configuration
			// tab.
			updateLaunchAttributeControls(rm, queue, getLaunchConfiguration());
			
		} catch (CoreException e) {
			setErrorMessage(LaunchMessages.getFormattedResourceString(
							"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", //$NON-NLS-1$
							e.getStatus().getMessage()));
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		try {
			IResourceManager rm = getResourceManager(configuration);
			if (rm == null) {
				setErrorMessage(LaunchMessages.getResourceString(
						"ResourcesTab.No_Resource_Manager")); //$NON-NLS-1$
				return false;
			}

			// load up the combos given that the configuration has selected
			// a resource manager
			loadQueueCombo(rm);
			
			IPQueue queue = getQueueFromName(rm, configuration.getAttribute(
					IPTPLaunchConfigurationConstants.ATTR_QUEUE_NAME, EMPTY_STRING));
			if (queue == null) {
				setErrorMessage(LaunchMessages.getResourceString(
                				"ResourcesTab.Invalid_Queue")); //$NON-NLS-1$
				return false;
			}
			
			assert(queue == getQueueFromCombo());

			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
			if (rmDynamicTab == null) {
				setErrorMessage(LaunchMessages.getFormattedResourceString(
						"ResourcesTab.No_Launch_Configuration", rm.getName())); //$NON-NLS-1$
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
							"CommonTab.common.Exception_occurred_reading_configuration_EXCEPTION", //$NON-NLS-1$
							e.getStatus().getMessage()));
			return false;
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_QUEUE_NAME,
				getQueueNameFromCombo());
		IResourceManager rm = getResourceManager(configuration);
		if (rm != null) {
			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
			if (rmDynamicTab == null) {
				setErrorMessage(LaunchMessages.getFormattedResourceString(
						"ResourcesTab.No_Launch_Configuration", rm.getName())); //$NON-NLS-1$
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
		final IResourceManager rm = getResourceManager(configuration);
		if (rm == null) {
			setErrorMessage(LaunchMessages.getResourceString(
					"ResourcesTab.No_Resource_Manager")); //$NON-NLS-1$
		}
		final IPQueue queue = getQueueDefault(rm);
		final String queueName = (queue != null ? queue.getName() : ""); //$NON-NLS-1$
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_QUEUE_NAME,
				queueName);
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
		if (rmDynamicTab == null) {
			setErrorMessage(LaunchMessages.getFormattedResourceString(
					"ResourcesTab.No_Launch_Configuration", rm.getName())); //$NON-NLS-1$
		}
		rmDynamicTab.setDefaults(configuration, rm, queue);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#setLaunchConfigurationDialog(org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	/**
	 * @param parent
	 * @param colspan
	 * @return
	 */
	private ScrolledComposite createLaunchAttributeControlComposite(Composite parent, int colspan) {
		ScrolledComposite attrComp = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL | SWT.BORDER);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		gridData.horizontalSpan = colspan;
		attrComp.setLayoutData(gridData);
		attrComp.setExpandHorizontal(true);
		attrComp.setExpandVertical(true);
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
	 * @return
	 */
	private ScrolledComposite getLaunchAttrsScrollComposite() {
		return launchAttrsScrollComposite;
	}

	/**
	 * @return the launchConfiguration
	 */
	/**
	 * @return
	 */
	private ILaunchConfiguration getLaunchConfiguration() {
		return launchConfiguration;
	}

	/**
	 * @param rm
	 * @return
	 */
	private IPQueue getQueueDefault(IResourceManager rm) {
		final IPQueue[] queues = rm.getQueues();
		if (queues.length == 0) {
			return null;
		}
		return queues[0];
	}

	/**
	 * @return
	 */
	private IPQueue getQueueFromCombo() {
		if (queueCombo != null) {
			int i = queueCombo.getSelectionIndex();
			return queues.get(i);
		}
		return null;
	}

	/**
	 * @param rm
	 * @param queueName
	 * @return
	 */
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

	/**
	 * @return
	 */
	private String getQueueNameFromCombo() {
		IPQueue queue = getQueueFromCombo();
		if (queue == null) {
			return "";
		}
		return queue.getName();
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
        final IResourceManager rm = getResourceManager(getLaunchConfiguration());
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
	 * @param comp
	 */
	private void setLaunchAttrsScrollComposite(ScrolledComposite comp) {
		this.launchAttrsScrollComposite = comp;
	}

	/**
	 * @param configuration
	 */
	private void setLaunchConfiguration(ILaunchConfiguration configuration) {
		launchConfiguration = configuration;
	}

	/**
	 * @param queue
	 */
	private void setQueueComboSelection(IPQueue queue) {
		final Integer results = queueIndices.get(queue);
		int i = 0;
		if (results != null) {
			i = results.intValue();
		}
		queueCombo.select(i);
		queueSelectionChanged();
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
		final ScrolledComposite launchAttrsScrollComp = getLaunchAttrsScrollComposite();
		launchAttrsScrollComp.setContent(null);
		for (Control child : launchAttrsScrollComp.getChildren()) {
			child.dispose();
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
		if (rmDynamicTab != null) {
			try {
				rmDynamicTab.createControl(launchAttrsScrollComp, rm, queue);
				final Control dynControl = rmDynamicTab.getControl();
				launchAttrsScrollComp.setContent(dynControl);
				Point size = dynControl.computeSize(SWT.DEFAULT, SWT.DEFAULT);
				launchAttrsScrollComp.setMinSize(size);
				rmDynamicTab.initializeFrom(launchAttrsScrollComp, rm, queue, launchConfiguration);
			} catch (CoreException e) {
				setErrorMessage(e.getMessage());
				PTPLaunchPlugin.errorDialog(e.getMessage(), e);
			}
		}		
		launchAttrsScrollComp.layout(true);
	}
}