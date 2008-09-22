/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.ui.wizards.ConfigureRemoteServices;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.dialogs.PropertyPage;

public class ServiceModelPropertyPage extends PropertyPage {
	
	PropertyPageServiceModelWidget fModelWidget;
	
	public ServiceModelPropertyPage() {
		fModelWidget = new PropertyPageServiceModelWidget();
	}

	@Override
	protected Control createContents(Composite parent) {
		Control table = fModelWidget.createContents(parent);
		fModelWidget.updateServicesTable((IProject) getElement());
		return table;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		// called when OK or Apply is pressed
		IProject project = (IProject) getElement();
		ConfigureRemoteServices.configure(project, fModelWidget.getServiceIDToSelectedProviderID(), fModelWidget.getProviderIDToProviderMap());
		return true;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performDefaults()
	 */
	@Override
	protected void performDefaults() {
		// TODO restore default using configuration strings
		super.performDefaults();
	}

}
