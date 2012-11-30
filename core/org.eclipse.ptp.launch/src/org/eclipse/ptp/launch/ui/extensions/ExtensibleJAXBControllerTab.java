/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.launch.ui.extensions;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.rm.jaxb.control.ui.utils.LaunchTabBuilder;
import org.eclipse.ptp.rm.jaxb.control.ui.variables.LCVariableMap;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIConstants;
import org.eclipse.ptp.rm.jaxb.ui.JAXBUIPlugin;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * Base class for the parent controller tab. Maintains list of child controllers, and implements multiplexed versions of the
 * abstract dynamic tab interface.
 * 
 * @author arossi
 * @since 7.0
 * 
 */
public abstract class ExtensibleJAXBControllerTab extends AbstractRMLaunchConfigurationDynamicTab implements
		IRMLaunchConfigurationContentsChangedListener {

	/**
	 * This flag is set when there is a validation error for the resource manager configuration. An error will be displayed to the
	 * user, but the exception will not be thrown. This allows the ResourcesTab to cache this launch tab instead of trying to
	 * reconstruct it everytime the selection changed listener is called. The user will be advised that this ResourceManager has
	 * become invalid and should be discarded. The flag is reset to valid (false) at the next load of a resource manager.
	 */
	protected boolean voidRMConfig;
	protected boolean initialized;
	protected TabFolder tabFolder;
	protected int lastIndex;
	protected String controlId;

	private final LinkedList<AbstractJAXBLaunchConfigurationTab> tabControllers = new LinkedList<AbstractJAXBLaunchConfigurationTab>();

	private final Map<String, AbstractJAXBLaunchConfigurationTab> controllerIndex = new HashMap<String, AbstractJAXBLaunchConfigurationTab>();
	private Composite control;

	/**
	 * @param context
	 */
	protected ExtensibleJAXBControllerTab() {
		voidRMConfig = false;
		initialized = false;
		lastIndex = 0;
	}

	/*
	 * calls canSave on children (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #canSave(org.eclipse.swt.widgets.Control)
	 */
	public RMLaunchValidation canSave(Control control) {
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.canSave(control);
			if (!validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#createControl(org.eclipse.swt.widgets.Composite,
	 * java.lang.String)
	 */
	public void createControl(Composite parent, String id) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		if (!voidRMConfig) {
			GridLayout layout = new GridLayout();
			control.setLayout(layout);

			tabFolder = new TabFolder(control, SWT.NONE);
			tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

			LaunchTabBuilder.initialize();

			for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
				final TabItem simpleTabItem = new TabItem(tabFolder, SWT.NONE);
				tabControl.createControl(tabFolder, id);
				simpleTabItem.setText(tabControl.getText());
				simpleTabItem.setImage(tabControl.getImage());
				simpleTabItem.setControl(tabControl.getControl());
			}
		}
		control.layout(true, true);
		controlId = id;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #getControl()
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
	 * Listener method delegates to the fireContentsChanged method. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions. IRMLaunchConfigurationContentsChangedListener #handleContentsChanged(org.eclipse
	 * .ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab)
	 */
	public void handleContentsChanged(IRMLaunchConfigurationDynamicTab factory) {
		/*
		 * calls contents changed on all the child tabs
		 */
		fireContentsChanged();
	}

	/*
	 * Calls initializeFrom on child tabs, then sets the shared environment. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(ILaunchConfiguration configuration) {
		String lastTab = null;
		String key = null;
		try {
			key = controlId + JAXBUIConstants.DOT + JAXBUIConstants.INITIALIZED;
			initialized = configuration.getAttribute(key, false);
			key = controlId + JAXBUIConstants.DOT + JAXBUIConstants.CURRENT_CONTROLLER;
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
			RMLaunchValidation validation = tabControl.initializeFrom(configuration);
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
				ILaunchConfigurationWorkingCopy wc = configuration.getWorkingCopy();
				wc.setAttribute(controlId + JAXBUIConstants.DOT + JAXBUIConstants.INITIALIZED, true);
				wc.doSave();
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
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig) {
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.isValid(launchConfig);
			if (!validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/*
	 * Calls performApply on child tabs. (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration) {
		try {
			LCVariableMap.normalizeStandardProperties(controlId + JAXBUIConstants.DOT, configuration);
		} catch (CoreException ce) {
			return new RMLaunchValidation(false, ce.getLocalizedMessage());
		}
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.performApply(configuration);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	/*
	 * Calls setDefaults on child tabs. (non-Javadoc) (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.setDefaults(configuration);
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
