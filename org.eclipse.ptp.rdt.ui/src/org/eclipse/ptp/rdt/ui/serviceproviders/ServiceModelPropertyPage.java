/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.serviceproviders;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.ptp.internal.rdt.ui.RDTHelpContextIds;
import org.eclipse.ptp.rdt.ui.wizards.ConfigureRemoteServices;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.dialogs.PropertyPage;

/**
 * Remote project property page for configuring service providers
 * @author vkong
 *
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 */
public class ServiceModelPropertyPage extends PropertyPage {
	
	PropertyPageServiceModelWidget fModelWidget;
	
	public ServiceModelPropertyPage() {
		fModelWidget = new PropertyPageServiceModelWidget();
		fModelWidget.setConfigChangeListener(new Listener() {
			public void handleEvent(Event event) {
				setValid(fModelWidget.isConfigured());
			}			
		});
	}

	@Override
	protected Control createContents(Composite parent) {
		Control table = fModelWidget.createContents(parent);

		if (getElement() instanceof IProject)
			fModelWidget.updateServicesTable((IProject) getElement());
		else if (getElement() instanceof ICProject )
			fModelWidget.updateServicesTable(((ICProject) getElement()).getProject());
		
		PlatformUI.getWorkbench().getHelpSystem().setHelp(parent, RDTHelpContextIds.SERVICE_MODEL_PROPERTY_PAGE);
		
		return table;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#performOk()
	 */
	@Override
	public boolean performOk() {
		IProject project = null;
		// called when OK or Apply is pressed
		if (getElement() instanceof IProject)
			project = (IProject) getElement();
		else 
			project = ((ICProject) getElement()).getProject();
		ConfigureRemoteServices.configure(project, fModelWidget.getServiceIDToSelectedProviderID(), fModelWidget.getProviderIDToProviderMap(), new NullProgressMonitor());
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

	/* (non-Javadoc)
	 * @see org.eclipse.jface.preference.PreferencePage#isValid()
	 */
	@Override
	public boolean isValid() {
		return fModelWidget.isConfigured();
	}
	

}
