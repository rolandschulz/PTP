/*******************************************************************************
 * Copyright (c) 2011 University of Illinois All rights reserved. This program
 * and the accompanying materials are made available under the terms of the
 * Eclipse Public License v1.0 which accompanies this distribution, and is
 * available at http://www.eclipse.org/legal/epl-v10.html 
 * 	
 * Contributors: 
 * 	Albert L. Rossi - design and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.jaxb.ui.launch;

import java.util.ArrayList;
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

public abstract class ExtensibleJAXBControllerTab extends AbstractRMLaunchConfigurationDynamicTab implements
		IRMLaunchConfigurationContentsChangedListener {

	private final List<AbstractJAXBLaunchConfigurationTab> tabControllers = new ArrayList<AbstractJAXBLaunchConfigurationTab>();

	private Composite control;

	public ExtensibleJAXBControllerTab(ILaunchConfigurationDialog dialog) {
		super(dialog);
	}

	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.canSave(control, rm, queue);
			if (!validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		control.setLayout(layout);

		final TabFolder tabFolder = new TabFolder(control, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			final TabItem simpleTabItem = new TabItem(tabFolder, SWT.NONE);
			tabControl.createControl(tabFolder, rm, queue);
			simpleTabItem.setText(tabControl.getText());
			simpleTabItem.setImage(tabControl.getImage());
			simpleTabItem.setControl(tabControl.getControl());
		}
	}

	public Control getControl() {
		return control;
	}

	public void handleContentsChanged(IRMLaunchConfigurationDynamicTab factory) {
		/*
		 * calls contents changed on all the child tabs
		 */
		fireContentsChanged();
	}

	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.initializeFrom(control, rm, queue, configuration);
			if (!validation.isSuccess()) {
				resultValidation = validation;
			}
		}
		return resultValidation;
	}

	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		for (AbstractJAXBLaunchConfigurationTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.isValid(launchConfig, rm, queue);
			if (!validation.isSuccess()) {
				return validation;
			}
		}
		return new RMLaunchValidation(true, null);
	}

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
		tabController.addContentsChangedListener(this);
	}

	/**
	 */
	protected List<AbstractJAXBLaunchConfigurationTab> getControllers() {
		return tabControllers;
	}
}
