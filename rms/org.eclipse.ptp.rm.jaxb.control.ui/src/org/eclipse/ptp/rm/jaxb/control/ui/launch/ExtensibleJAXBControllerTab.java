/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.control.ui.launch;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Base class for the parent controller tab. Maintains list of child
 * controllers, and implements multiplexed versions of the abstract dynamic tab
 * interface.
 * 
 * @author arossi
 * 
 */
public abstract class ExtensibleJAXBControllerTab extends AbstractRMLaunchConfigurationDynamicTab implements
		IRMLaunchConfigurationContentsChangedListener {

	/**
	 * This flag is set when there is a validation error for the resource
	 * manager configuration. An error will be displayed to the user, but the
	 * exception will not be thrown. This allows the ResourcesTab to cache this
	 * launch tab instead of trying to reconstruct it everytime the selection
	 * changed listener is called. The user will be advised that this
	 * ResourceManager has become invalid and should be discarded. The flag is
	 * reset to valid (false) at the next load of a resource manager.
	 */
	protected boolean voidRMConfig;
	protected boolean initialized;
	protected TabFolder tabFolder;
	protected int lastIndex;

	private final LinkedList<AbstractJAXBLaunchConfigurationTab> tabControllers = new LinkedList<AbstractJAXBLaunchConfigurationTab>();

	private final Map<String, AbstractJAXBLaunchConfigurationTab> controllerIndex = new HashMap<String, AbstractJAXBLaunchConfigurationTab>();
	private Composite control;

	/**
	 * @param dialog
	 */
	protected ExtensibleJAXBControllerTab(ILaunchConfigurationDialog dialog) {
		super(dialog);
		voidRMConfig = false;
		initialized = false;
		lastIndex = 0;
	}

	/*
	 * calls canSave on children (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #canSave(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.canSave(control, rm, queue);
			if (!validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Builds the tab folder containing child tabs. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #createControl(org.eclipse.swt.widgets.Composite,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		if (!voidRMConfig) {
			GridLayout layout = new GridLayout();
			control.setLayout(layout);

			tabFolder = new TabFolder(control, SWT.NONE);
			tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
				final TabItem simpleTabItem = new TabItem(tabFolder, SWT.NONE);
				tabControl.createControl(tabFolder, rm, queue);
				simpleTabItem.setText(tabControl.getText());
				simpleTabItem.setImage(tabControl.getImage());
				simpleTabItem.setControl(tabControl.getControl());
			}
		}
		control.layout(true, true);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #getControl()
	 */
	public Control getControl() {
		return control;
	}

	/**
	 * @return index of the selected controller
	 */
	public int getSelectedController() {
		return tabFolder.getSelectionIndex();
	}

	/*
	 * Listener method delegates to the fireContentsChanged method.
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.
	 * IRMLaunchConfigurationContentsChangedListener
	 * #handleContentsChanged(org.eclipse
	 * .ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab)
	 */
	public void handleContentsChanged(IRMLaunchConfigurationDynamicTab factory) {
		/*
		 * calls contents changed on all the child tabs
		 */
		fireContentsChanged();
	}

	/*
	 * Calls initializeFrom on child tabs, then sets the shared environment.
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		String lastTab = null;
		String key = null;
		try {
			key = rm.getUniqueName() + JAXBUIConstants.DOT + JAXBUIConstants.INITIALIZED;
			initialized = configuration.getAttribute(key, false);
			key = rm.getUniqueName() + JAXBUIConstants.DOT + JAXBUIConstants.CURRENT_CONTROLLER;
			lastTab = configuration.getAttribute(key, JAXBUIConstants.ZEROSTR);
		} catch (CoreException t1) {
			JAXBUIPlugin.log(t1);
		}

		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		int i = 0;
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			if (tabControl.getControllerTag().equals(lastTab)) {
				lastIndex = i;
			}
			RMLaunchValidation validation = tabControl.initializeFrom(control, rm, queue, configuration);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
			i++;
		}

		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			try {
				tabControl.setUpSharedEnvironment(controllerIndex);
			} catch (CoreException t) {
				return new RMLaunchValidation(false, t.getLocalizedMessage());
			}
		}

		if (!initialized) {
			for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
				try {
					tabControl.refreshLocal(configuration.getWorkingCopy());
				} catch (CoreException t) {
					return new RMLaunchValidation(false, t.getLocalizedMessage());
				}
			}
			try {
				configuration.getWorkingCopy().setAttribute(rm.getUniqueName() + JAXBUIConstants.DOT + JAXBUIConstants.INITIALIZED,
						true);
			} catch (CoreException t) {
				JAXBUIPlugin.log(t);
			}
		}
		return resultValidation;
	}

	/**
	 * @return whether the config has already been initialized for this rm
	 */
	public boolean isInitialized() {
		return initialized;
	}

	/*
	 * Calls isValid on child tabs. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.isValid(launchConfig, rm, queue);
			if (!validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Calls performApply on child tabs. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.performApply(configuration, rm, queue);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	/*
	 * Calls setDefaults on child tabs. (non-Javadoc) (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.setDefaults(configuration, rm, queue);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	/**
	 * @param tabController
	 */
	protected void addDynamicTab(AbstractJAXBLaunchConfigurationTab tabController) {
		tabControllers.add(tabController);
		controllerIndex.put(tabController.getText(), tabController);
		tabController.addContentsChangedListener(this);
	}

	/**
	 * Makes the visible tab last.
	 * 
	 * @return list of child tabs.
	 */
	protected List<AbstractJAXBLaunchConfigurationTab> getControllers() {
		return tabControllers;
	}
}
