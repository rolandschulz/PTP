/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.ui.launch;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationContentsChangedListener;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.launch.ui.extensions.RMLaunchValidation;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;

/**
 * @since 2.0
 */
public abstract class ExtendableRMLaunchConfigurationDynamicTab extends AbstractRMLaunchConfigurationDynamicTab implements
		IRMLaunchConfigurationContentsChangedListener {

	private final List<BaseRMLaunchConfigurationDynamicTab> tabControllers = new ArrayList<BaseRMLaunchConfigurationDynamicTab>();

	private Composite control;

	/**
	 * @since 3.0
	 */
	public ExtendableRMLaunchConfigurationDynamicTab() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #canSave(org.eclipse.swt.widgets.Control)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation canSave(Control control) {
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.canSave(control);
			if (!validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/**
	 * @since 3.0
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab#createControl(org.eclipse.swt.widgets.Composite,
	 * java.lang.String)
	 */
	public void createControl(Composite parent, String id) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		control.setLayout(layout);

		final TabFolder tabFolder = new TabFolder(control, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			final TabItem simpleTabItem = new TabItem(tabFolder, SWT.NONE);
			tabControl.createControl(tabFolder, id);
			simpleTabItem.setText(tabControl.getText());
			simpleTabItem.setImage(tabControl.getImage());
			simpleTabItem.setControl(tabControl.getControl());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab #getControl()
	 */
	public Control getControl() {
		return control;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions. IRMLaunchConfigurationContentsChangedListener #handleContentsChanged(org.eclipse
	 * .ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab)
	 */
	public void handleContentsChanged(IRMLaunchConfigurationDynamicTab factory) {
		fireContentsChanged();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #initializeFrom(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation initializeFrom(ILaunchConfiguration configuration) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.initializeFrom(configuration);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #isValid(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig) {
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.isValid(launchConfig);
			if (!validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #performApply(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.performApply(configuration);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab
	 * #setDefaults(org.eclipse.debug.core.ILaunchConfigurationWorkingCopy)
	 */
	/**
	 * @since 3.0
	 */
	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.setDefaults(configuration);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	/**
	 * Update controls on the tab
	 */
	public void updateControls() {
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			tabControl.updateControls();
		}
	}

	/**
	 * @param tabController
	 */
	protected void addDynamicTab(BaseRMLaunchConfigurationDynamicTab tabController) {
		tabControllers.add(tabController);
		tabController.addContentsChangedListener(this);
	}
}
