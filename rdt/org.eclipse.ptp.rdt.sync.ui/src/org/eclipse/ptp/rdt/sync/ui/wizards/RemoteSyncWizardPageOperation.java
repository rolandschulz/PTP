/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 * Roland Schulz, University of Tennessee
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui.wizards;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.cdt.internal.ui.wizards.ICDTCommonProjectWizard;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.rdt.sync.core.services.IRemoteSyncServiceConstants;
import org.eclipse.ptp.rdt.sync.ui.RDTSyncUIPlugin;
import org.eclipse.ptp.rdt.sync.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * An operation which handles configuring the remote portions of the Remote
 * C/C++ Project when the project is actually being created.
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as part
 * of a work in progress. There is no guarantee that this API will work or that
 * it will remain the same. Please do not use this API without consulting with
 * the RDT team.
 * 
 * 
 */
public class RemoteSyncWizardPageOperation implements IRunnableWithProgress {

	public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
		monitor.beginTask("configure model services", 100); //$NON-NLS-1$

		IWizard wizard = MBSCustomPageManager.getPageData(NewRemoteSyncProjectWizardPage.REMOTE_SYNC_WIZARD_PAGE_ID).getWizardPage()
				.getWizard();
		IProject project = ((ICDTCommonProjectWizard) wizard).getLastProject();

		IRemoteServices remoteServices = (IRemoteServices) getMBSProperty(NewRemoteSyncProjectWizardPage.SERVICE_PROVIDER_PROPERTY);
		IRemoteConnection remoteConnection = (IRemoteConnection) getMBSProperty(NewRemoteSyncProjectWizardPage.CONNECTION_PROPERTY);
		String path = (String) getMBSProperty(NewRemoteSyncProjectWizardPage.PATH_PROPERTY);

		ServiceModelManager smm = ServiceModelManager.getInstance();
		IServiceConfiguration config = smm.newServiceConfiguration(getConfigName(remoteConnection.getName()));
		IService buildService = smm.getService(IRemoteSyncServiceConstants.SERVICE_BUILD);
		IServiceProviderDescriptor descriptor = buildService.getProviderDescriptor(RemoteBuildServiceProvider.ID);
		RemoteBuildServiceProvider rbsp = (RemoteBuildServiceProvider) smm.getServiceProvider(descriptor);
		if (rbsp != null) {
			rbsp.setRemoteToolsProviderID(remoteServices.getId());
			rbsp.setRemoteToolsConnection(remoteConnection);
			rbsp.setConfigLocation(path);
			config.setServiceProvider(buildService, rbsp);
		}
		smm.addConfiguration(project, config);

		try {
			smm.saveModelConfiguration();
		} catch (IOException e) {
			RDTSyncUIPlugin.log(e.toString(), e);
		}

		monitor.done();
	}

	private static Object getMBSProperty(String propertyId) {
		return MBSCustomPageManager.getPageProperty(NewRemoteSyncProjectWizardPage.REMOTE_SYNC_WIZARD_PAGE_ID, propertyId);
	}

	/**
	 * Creates a name for the service configuration based on the remote
	 * connection name. If multiple names exist, appends a qualifier to the
	 * name.
	 * 
	 * @return new name guaranteed to be unique
	 */
	private String getConfigName(String candidateName) {
		Set<IServiceConfiguration> configs = ServiceModelManager.getInstance().getConfigurations();
		Set<String> existingNames = new HashSet<String>();
		for (IServiceConfiguration config : configs) {
			existingNames.add(config.getName());
		}

		int i = 2;
		String newConfigName = candidateName;
		while (existingNames.contains(newConfigName)) {
			newConfigName = candidateName + " (" + (i++) + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		}

		return newConfigName;
	}
}
