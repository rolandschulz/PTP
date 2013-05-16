/*******************************************************************************
 * Copyright (c) 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.properties;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.ptp.internal.rdt.sync.ui.SynchronizePropertiesRegistry;
import org.eclipse.ptp.rdt.sync.ui.ISynchronizeProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * @author greg
 * 
 */
public class SyncPropertyPage extends PropertyPage {

	private ManageConfigurationWidget fWidget;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#createContents(org.eclipse.swt.widgets.Composite)
	 */
	@Override
	protected Control createContents(Composite parent) {
		Composite controls = new Composite(parent, SWT.NONE);
		GridLayout layout = new GridLayout(1, false);
		layout.marginHeight = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_MARGIN);
		layout.marginWidth = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_MARGIN);
		layout.verticalSpacing = convertVerticalDLUsToPixels(IDialogConstants.VERTICAL_SPACING);
		layout.horizontalSpacing = convertHorizontalDLUsToPixels(IDialogConstants.HORIZONTAL_SPACING);
		controls.setLayout(layout);
		controls.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		fWidget = new ManageConfigurationWidget(controls, SWT.NONE);
		fWidget.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		fWidget.setProject(getProject());

		return controls;
	}

	/**
	 * Returns the project this property page is open on.
	 * 
	 * @return project
	 */
	protected IProject getProject() {
		Object element = getElement();
		IResource resource = null;
		if (element instanceof IResource) {
			resource = (IResource) element;
		} else if (element instanceof IAdaptable) {
			resource = (IResource) ((IAdaptable) element).getAdapter(IResource.class);
		}
		if (resource != null) {
			return resource.getProject();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performCancel()
	 */
	@Override
	public boolean performCancel() {
		ISynchronizeProperties prop = SynchronizePropertiesRegistry.getSynchronizePropertiesForProject(getProject());
		if (prop != null) {
			prop.performCancel();
		}
		return super.performCancel();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		fWidget.setDefaults();
		ISynchronizeProperties prop = SynchronizePropertiesRegistry.getSynchronizePropertiesForProject(getProject());
		if (prop != null) {
			prop.performDefaults();
		}
		super.performDefaults();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		fWidget.commit();
		ISynchronizeProperties prop = SynchronizePropertiesRegistry.getSynchronizePropertiesForProject(getProject());
		if (prop != null) {
			prop.performApply();
		}
		return true;
	}
}
