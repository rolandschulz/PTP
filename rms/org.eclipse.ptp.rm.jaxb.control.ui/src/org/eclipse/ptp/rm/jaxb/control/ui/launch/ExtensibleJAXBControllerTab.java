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

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
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
	protected TabFolder tabFolder;

	private final LinkedList<AbstractJAXBLaunchConfigurationTab> tabControllers = new LinkedList<AbstractJAXBLaunchConfigurationTab>();
	private Composite control;

	/**
	 * @param dialog
	 */
	protected ExtensibleJAXBControllerTab(ILaunchConfigurationDialog dialog) {
		super(dialog);
		voidRMConfig = false;
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
		List<AbstractJAXBLaunchConfigurationTab> controllers = getControllers();
		for (AbstractJAXBLaunchConfigurationTab tabControl : controllers) {
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
	 * Calls initializeFrom on child tabs. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.swt.widgets.Control,
	 * org.eclipse.ptp.rmsystem.IResourceManager,
	 * org.eclipse.ptp.core.elements.IPQueue,
	 * org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		List<AbstractJAXBLaunchConfigurationTab> controllers = getControllers();
		for (AbstractJAXBLaunchConfigurationTab tabControl : controllers) {
			RMLaunchValidation validation = tabControl.initializeFrom(control, rm, queue, configuration);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
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
		List<AbstractJAXBLaunchConfigurationTab> controllers = getControllers();
		for (AbstractJAXBLaunchConfigurationTab tabControl : controllers) {
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
		List<AbstractJAXBLaunchConfigurationTab> controllers = getControllers();
		for (AbstractJAXBLaunchConfigurationTab tabControl : controllers) {
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
		List<AbstractJAXBLaunchConfigurationTab> controllers = getControllers();
		for (AbstractJAXBLaunchConfigurationTab tabControl : controllers) {
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
		tabController.addContentsChangedListener(this);
	}

	/**
	 * Makes the visible tab last.
	 * 
	 * @return list of child tabs.
	 */
	protected synchronized List<AbstractJAXBLaunchConfigurationTab> getControllers() {
		for (int i = 0; i < tabControllers.size(); i++) {
			AbstractJAXBLaunchConfigurationTab last = tabControllers.getLast();
			if (last.getControl().isVisible()) {
				break;
			}
			tabControllers.addFirst(tabControllers.removeLast());
		}
		return tabControllers;
	}
}
