/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.wizards;

import static org.eclipse.ptp.rdt.ui.wizards.ServiceModelWizardPage.CONFIG_PROPERTY;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.internal.rdt.core.index.RemoteFastIndexer;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.rse.internal.connectorservice.dstore.Activator;

/**
 * An operation which handles configuring the remote portions of the Remote C/C++ Project
 * when the project is actually being created.
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 *
 */
public class ServiceModelWizardPageOperation implements IRunnableWithProgress {



	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("configure model services", 100);  //$NON-NLS-1$
		
		IWizard wizard = MBSCustomPageManager.getPageData(ServiceModelWizardPage.SERVICE_MODEL_WIZARD_PAGE_ID).getWizardPage().getWizard();
		IProject project = ((ICDTCommonProjectWizard) wizard).getLastProject();
		
		IServiceConfiguration config = (IServiceConfiguration)getMBSProperty(CONFIG_PROPERTY);
		
		ServiceModelManager smm = ServiceModelManager.getInstance();
		smm.addConfiguration(project, config);
		
		try {
			smm.saveModelConfiguration();
		} catch (IOException e) {
			Activator.logError(e.toString(), e);
		}
		
		ICProject cProject = CModelManager.getDefault().getCModel().getCProject(project);
		CCorePlugin.getIndexManager().setIndexerId(cProject, RemoteFastIndexer.ID);

		monitor.done();		
	}
	
	
	private static Object getMBSProperty(String propertyId) {
		return MBSCustomPageManager.getPageProperty(ServiceModelWizardPage.SERVICE_MODEL_WIZARD_PAGE_ID, propertyId);
	}
}
