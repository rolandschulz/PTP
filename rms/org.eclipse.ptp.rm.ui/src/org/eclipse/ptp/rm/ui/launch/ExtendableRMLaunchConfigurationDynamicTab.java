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
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
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

public abstract class ExtendableRMLaunchConfigurationDynamicTab extends AbstractRMLaunchConfigurationDynamicTab implements
		IRMLaunchConfigurationContentsChangedListener {

	private final List<BaseRMLaunchConfigurationDynamicTab> tabControllers = new ArrayList<BaseRMLaunchConfigurationDynamicTab>();
	private Composite control;

	public RMLaunchValidation canSave(Control control, IResourceManager rm, IPQueue queue) {
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.canSave(control, rm, queue);
			if (!validation.isSuccess())
				return validation;
		}
		return new RMLaunchValidation(true, null);
	}

	public void createControl(Composite parent, IResourceManager rm, IPQueue queue) throws CoreException {
		control = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout();
		control.setLayout(layout);

		final TabFolder tabFolder = new TabFolder(control, SWT.NONE);
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			final TabItem simpleTabItem = new TabItem(tabFolder, SWT.NONE);
			tabControl.createControl(tabFolder, rm, queue);
			simpleTabItem.setText(tabControl.getText());
			simpleTabItem.setImage(tabControl.getImage());
			simpleTabItem.setControl(tabControl.getControl());
		}
	}

	public IAttribute<?, ?, ?>[] getAttributes(IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration, String mode)
			throws CoreException {
		List<IAttribute<?, ?, ?>> attributes = new ArrayList<IAttribute<?, ?, ?>>();
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			IAttribute<?, ?, ?> attributeArray[] = tabControl.getAttributes(rm, queue, configuration, mode);
			if (attributeArray != null) {
				List<IAttribute<?, ?, ?>> attributesList = Arrays.asList(attributeArray);
				attributes.addAll(attributesList);
			}
		}
		return attributes.toArray(new IAttribute<?, ?, ?>[attributes.size()]);
	}

	public Control getControl() {
		return control;
	}

	public void handleContentsChanged(IRMLaunchConfigurationDynamicTab factory) {
		fireContentsChanged();
	}

	public RMLaunchValidation initializeFrom(Control control, IResourceManager rm, IPQueue queue, ILaunchConfiguration configuration) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.initializeFrom(control, rm, queue, configuration);
			if (!validation.isSuccess())
				resultValidation = validation;
		}
		return resultValidation;
	}

	public RMLaunchValidation isValid(ILaunchConfiguration launchConfig, IResourceManager rm, IPQueue queue) {
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.isValid(launchConfig, rm, queue);
			if (!validation.isSuccess())
				return validation;
		}
		return new RMLaunchValidation(true, null);
	}

	public RMLaunchValidation performApply(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.performApply(configuration, rm, queue);
			if (!validation.isSuccess())
				resultValidation = validation;
		}
		return resultValidation;
	}

	public RMLaunchValidation setDefaults(ILaunchConfigurationWorkingCopy configuration, IResourceManager rm, IPQueue queue) {
		RMLaunchValidation resultValidation = new RMLaunchValidation(true, null);
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers) {
			RMLaunchValidation validation = tabControl.setDefaults(configuration, rm, queue);
			if (!validation.isSuccess())
				resultValidation = validation;
		}
		return resultValidation;
	}

	public void updateControls() {
		for (BaseRMLaunchConfigurationDynamicTab tabControl : tabControllers)
			tabControl.updateControls();
	}

	protected void addDynamicTab(BaseRMLaunchConfigurationDynamicTab tabController) {
		tabControllers.add(tabController);
		tabController.addContentsChangedListener(this);
	}
}
