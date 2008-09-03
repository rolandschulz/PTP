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
package org.eclipse.ptp.rdt.ui.wizards;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;

import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;

/**
 * @author crecoskie
 *
 */
public class ServiceModelWizardPageOperation implements IRunnableWithProgress {

	/**
	 * 
	 */
	public ServiceModelWizardPageOperation() {
		// TODO Auto-generated constructor stub
	}

	/* (non-Javadoc)
	 * @see org.eclipse.jface.operation.IRunnableWithProgress#run(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		
		IProject project = null;
		
		// go through the mappings of services and set the service model to match
		Object obj = MBSCustomPageManager.getPageProperty(
				ServiceModelWizardPage.SERVICE_MODEL_WIZARD_PAGE_ID,
				ServiceModelWizardPage.SELECTED_PROVIDERS_MAP_PROPERTY);
		
		if(obj instanceof Map) {
			Map<String, String> serviceIDToProviderIDMap = (Map<String, String>) obj;
			
			IWizard wizard = MBSCustomPageManager.getPageData(ServiceModelWizardPage.SERVICE_MODEL_WIZARD_PAGE_ID).getWizardPage().getWizard();
			
			if(wizard instanceof CDTCommonProjectWizard)
				project = ((CDTCommonProjectWizard) wizard).getLastProject();
			
			Object obj2 = MBSCustomPageManager.getPageProperty(
					ServiceModelWizardPage.SERVICE_MODEL_WIZARD_PAGE_ID,
					ServiceModelWizardPage.ID_TO_PROVIDERS_MAP_PROPERTY);
			
			if (obj2 instanceof Map) {
				Map<String, IServiceProvider> providerIDToProviderMap = (Map<String, IServiceProvider>) obj2;				
				ConfigureRemoteServices.configure(project, serviceIDToProviderIDMap, providerIDToProviderMap);
			}
		}
	}
}
