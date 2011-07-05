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
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.messages.Messages;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;

/**
 * The Resources tab is used to specify the resources required for a successful
 * job launch. It is populated by the selected resource manager (specified in
 * the Main tab)
 */
public class ResourcesTab extends LaunchConfigurationTab {
	private final class ContentsChangedListener implements IRMLaunchConfigurationContentsChangedListener {

		public void handleContentsChanged(IRMLaunchConfigurationDynamicTab rmDynamicTab) {
			// The buttons and messages have to be updated based on anything
			// that has changed in the dynamic portion of the launch tab.
			updateLaunchConfigurationDialog();
		}
	}

	/**
	 * @since 4.0
	 */
	public static final String TAB_ID = "org.eclipse.ptp.launch.applicationLaunch.resourcesTab"; //$NON-NLS-1$

	private Combo resourceManagerCombo = null;

	private IResourceManager resourceManager = null;
	private final List<IResourceManager> resourceManagers = new ArrayList<IResourceManager>();
	// The composite that holds the RM's attributes for the launch configuration
	private ScrolledComposite launchAttrsScrollComposite;

	private final Map<IResourceManager, IRMLaunchConfigurationDynamicTab> rmDynamicTabs = new HashMap<IResourceManager, IRMLaunchConfigurationDynamicTab>();

	private final ContentsChangedListener launchContentsChangedListener = new ContentsChangedListener();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#canSave()
	 */
	@Override
	public boolean canSave() {
		setErrorMessage(null);
		if (resourceManager == null) {
			setErrorMessage(Messages.ResourcesTab_No_Resource_Manager);
			return false;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(resourceManager);
		final Composite launchComp = getLaunchAttrsScrollComposite();
		if (rmDynamicTab == null || launchComp == null) {
			setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { resourceManager.getName() }));
			return false;
		}
		RMLaunchValidation validation = rmDynamicTab.canSave(launchComp, resourceManager, null);
		if (!validation.isSuccess()) {
			setErrorMessage(validation.getMessage());
			return false;
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#createControl(org.eclipse
	 * .swt.widgets.Composite)
	 */
	public void createControl(Composite parent) {
		final int numColumns = 2;
		final Composite comp = new Composite(parent, SWT.NONE);
		setControl(comp);

		GridLayout layout = new GridLayout(2, false);
		comp.setLayout(layout);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		comp.setLayoutData(gd);

		IResourceManager[] rms = PTPCorePlugin.getDefault().getModelManager().getResourceManagers();
		new Label(comp, SWT.NONE).setText(Messages.ApplicationTab_RM_Selection_Label);

		resourceManagerCombo = new Combo(comp, SWT.READ_ONLY);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		resourceManagerCombo.setLayoutData(gd);
		resourceManagerCombo.add(Messages.ResourcesTab_pleaseSelectRM);
		resourceManagers.add(null);
		for (IResourceManager rm : rms) {
			resourceManagerCombo.add(rm.getName());
			resourceManagers.add(rm);
		}
		resourceManagerCombo.addSelectionListener(new SelectionListener() {
			public void widgetDefaultSelected(SelectionEvent e) {
			}

			public void widgetSelected(SelectionEvent e) {
				if (e.getSource() == resourceManagerCombo) {
					rmSelectionChanged();
					updateLaunchAttributeControls(resourceManager, null, getLaunchConfiguration());
					updateLaunchConfigurationDialog();
				}
			}
		});
		resourceManagerCombo.deselectAll();

		createVerticalSpacer(comp, 2);

		final ScrolledComposite scrollComp = createLaunchAttributeControlComposite(comp, numColumns);
		setLaunchAttrsScrollComposite(scrollComp);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getId()
	 */
	@Override
	public String getId() {
		return TAB_ID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#getImage()
	 */
	@Override
	public Image getImage() {
		return LaunchImages.getImage(LaunchImages.IMG_PARALLEL_TAB);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.ILaunchConfigurationTab#getName()
	 */
	public String getName() {
		return Messages.ResourcesTab_Resources;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#initializeFrom(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		super.initializeFrom(configuration);

		resourceManager = getResourceManager(configuration);
		setResourceManagerComboSelection(resourceManager);

		/*
		 * Check if there is launch configuration for the RM
		 */
		if (resourceManager != null) {
			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(resourceManager);
			final Composite launchComp = getLaunchAttrsScrollComposite();
			if (rmDynamicTab == null || launchComp == null) {
				setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { resourceManager.getName() }));
				return;
			}
		}

		/*
		 * Update the dynamic portions of the launch configuration tab.
		 */
		updateLaunchAttributeControls(resourceManager, null, getLaunchConfiguration());

		if (resourceManager == null) {
			setErrorMessage(Messages.ApplicationTab_No_Resource_Manager_Available);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.AbstractLaunchConfigurationTab#isValid(org.eclipse
	 * .debug.core.ILaunchConfiguration)
	 */
	@Override
	public boolean isValid(ILaunchConfiguration configuration) {
		setErrorMessage(null);
		setMessage(null);
		if (resourceManager == null) {
			setErrorMessage(Messages.ResourcesTab_No_Resource_Manager);
			return false;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(resourceManager);
		if (rmDynamicTab == null) {
			setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { resourceManager.getName() }));
			return false;
		}
		if (!resourceManager.getState().equals(IResourceManager.STARTED_STATE)) {
			setErrorMessage(Messages.ResourcesTab_Resource_Manager_Not_Started);
			return false;
		}
		RMLaunchValidation validation = rmDynamicTab.isValid(configuration, resourceManager, null);
		if (!validation.isSuccess()) {
			setErrorMessage(validation.getMessage());
			return false;
		}

		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#performApply(org.eclipse
	 * .debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		if (resourceManager != null) {
			configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME,
					resourceManager.getUniqueName());
			IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(resourceManager);
			if (rmDynamicTab == null) {
				setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { resourceManager.getName() }));
				return;
			}
			RMLaunchValidation validation = rmDynamicTab.performApply(configuration, resourceManager, null);
			if (!validation.isSuccess()) {
				setErrorMessage(validation.getMessage());
				return;
			}
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.ui.ILaunchConfigurationTab#setDefaults(org.eclipse.
	 * debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public void setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		IResourceManager rm = getResourceManagerDefault();
		if (rm == null) {
			setErrorMessage(Messages.ResourcesTab_No_Resource_Manager);
			return;
		}
		String rmName = rm.getUniqueName();
		configuration.setAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, rmName);

		IRMLaunchConfigurationDynamicTab rmDynamicTab = getRMLaunchConfigurationDynamicTab(rm);
		if (rmDynamicTab == null) {
			setErrorMessage(NLS.bind(Messages.ResourcesTab_No_Launch_Configuration, new Object[] { resourceManager.getName() }));
		} else {
			rmDynamicTab.setDefaults(configuration, rm, null);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.ui.AbstractLaunchConfigurationTab#
	 * setLaunchConfigurationDialog
	 * (org.eclipse.debug.ui.ILaunchConfigurationDialog)
	 */
	@Override
	public void setLaunchConfigurationDialog(ILaunchConfigurationDialog dialog) {
		super.setLaunchConfigurationDialog(dialog);
	}

	/**
	 * @param parent
	 * @param colspan
	 * @return
	 */
	private ScrolledComposite createLaunchAttributeControlComposite(Composite parent, int colspan) {
		ScrolledComposite attrComp = new ScrolledComposite(parent, SWT.H_SCROLL | SWT.V_SCROLL);
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
	private IRMLaunchConfigurationDynamicTab createRMLaunchConfigurationDynamicTab(final IResourceManager rm) throws CoreException {

		final AbstractRMLaunchConfigurationFactory rmFactory = PTPLaunchPlugin.getDefault().getRMLaunchConfigurationFactory(rm);
		if (rmFactory == null) {
			return null;
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = rmFactory.create(rm, getLaunchConfigurationDialog());
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
	 * Find a default resource manager. If there is only one, then it is the
	 * default. If there are more or less than one, there is no default.
	 * 
	 * @return resource manager
	 */
	private IResourceManager getResourceManagerDefault() {
		IResourceManager[] rms = PTPCorePlugin.getDefault().getModelManager().getResourceManagers();
		if (rms.length != 1) {
			return null;
		}
		return rms[0];
	}

	/**
	 * @return
	 */
	private IResourceManager getResourceManagerFromCombo() {
		if (resourceManagerCombo != null) {
			int i = resourceManagerCombo.getSelectionIndex();
			return resourceManagers.get(i);
		}
		return null;
	}

	/**
	 * Returns a cached launch configuration dynamic tab. If it isn't in the
	 * cache then it creates a new one, and puts it in the cache.
	 * 
	 * @param rm
	 * @return
	 */
	private IRMLaunchConfigurationDynamicTab getRMLaunchConfigurationDynamicTab(final IResourceManager rm) {
		if (!rmDynamicTabs.containsKey(rm)) {
			try {
				IRMLaunchConfigurationDynamicTab rmDynamicTab = createRMLaunchConfigurationDynamicTab(rm);
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
	 * Handle selection of a resource manager
	 */
	private void rmSelectionChanged() {
		resourceManager = getResourceManagerFromCombo();
	}

	/**
	 * @param comp
	 */
	private void setLaunchAttrsScrollComposite(ScrolledComposite comp) {
		this.launchAttrsScrollComposite = comp;
	}

	/**
	 * Given a resource manager, select it in the combo
	 * 
	 * @param resource
	 *            manager
	 */
	private void setResourceManagerComboSelection(IResourceManager rm) {
		int i = 0;
		if (rm != null) {
			i = resourceManagers.lastIndexOf(rm);
		}
		resourceManagerCombo.select(i);
		rmSelectionChanged();
	}

	/**
	 * This routine is called when the resource manager has been changed via the
	 * combo boxes. It's job is to regenerate the dynamic ui components,
	 * dependent on the resource manager choice.
	 * 
	 * @param rm
	 * @param queue
	 * @param launchConfiguration
	 */
	private void updateLaunchAttributeControls(IResourceManager rm, IPQueue queue, ILaunchConfiguration launchConfiguration) {
		final ScrolledComposite launchAttrsScrollComp = getLaunchAttrsScrollComposite();
		launchAttrsScrollComp.setContent(null);
		for (Control child : launchAttrsScrollComp.getChildren()) {
			child.dispose();
		}
		if (rm != null) {
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
					Throwable t = e.getCause();
					if (t == null) {
						t = e;
					}
					PTPLaunchPlugin.errorDialog(e.getMessage(), t);
				}
			}
			launchAttrsScrollComp.layout(true);
		}
	}
}