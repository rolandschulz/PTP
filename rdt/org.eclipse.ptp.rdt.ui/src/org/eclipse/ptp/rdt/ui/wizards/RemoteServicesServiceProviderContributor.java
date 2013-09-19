/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - initial API and implementation
 *******************************************************************************/ 
package org.eclipse.ptp.rdt.ui.wizards;

import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.ptp.rdt.ui.serviceproviders.RemoteBuildServiceProvider;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.remote.ui.RemoteUIServices;
import org.eclipse.remote.ui.widgets.RemoteConnectionWidget;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;
import org.eclipse.ptp.services.ui.IServiceProviderContributor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class RemoteServicesServiceProviderContributor implements IServiceProviderContributor {

	private IServiceProviderWorkingCopy fProviderWorkingCopy;
	private IRemoteConnection fSelectedConnection;
 	private RemoteBuildServiceFileLocationWidget fBuildConfigLocationWidget;
 	private RemoteConnectionWidget fRemoteConnectionWidget;
	
	/**
	 * @since 3.1
	 */
	public void configureServiceProvider(IServiceProviderWorkingCopy provider, final Composite container) {
		//The UI components works with IServiceProviderWorkingCopy and not the original IServiceProvider
		
		fProviderWorkingCopy = null;
		if (provider instanceof IServiceProviderWorkingCopy) {
			fProviderWorkingCopy = (IServiceProviderWorkingCopy) provider;
		}

		if (!(provider.getOriginal() instanceof RemoteBuildServiceProvider)) {
			throw new IllegalArgumentException(); // should never happen
		}

		GridLayout layout = new GridLayout();
        layout.numColumns = 1;
        container.setLayout(layout);
        
        fRemoteConnectionWidget = new RemoteConnectionWidget(container, SWT.NONE, null, RemoteConnectionWidget.FLAG_FORCE_PROVIDER_SELECTION, null);
        fRemoteConnectionWidget.setLayoutData(new GridData(SWT.BEGINNING, SWT.BEGINNING, false, false));
        fRemoteConnectionWidget.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				fSelectedConnection = fRemoteConnectionWidget.getConnection();
				updateProvider();
				fBuildConfigLocationWidget.update(fSelectedConnection);
			}
		});

        //attempt to restore settings from saved state
        String providerSelected = fProviderWorkingCopy.getString(RemoteBuildServiceProvider.REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID, null);
        String connection = fProviderWorkingCopy.getString(RemoteBuildServiceProvider.REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME, null);
        fRemoteConnectionWidget.setConnection(providerSelected, connection);
		fSelectedConnection = fRemoteConnectionWidget.getConnection();
        
        String configPath = fProviderWorkingCopy.getString(RemoteBuildServiceProvider.REMOTE_BUILD_SERVICE_PROVIDER_CONFIG_LOCATION, ""); //$NON-NLS-1$
        if(configPath.length() <= 0)
        	configPath = RemoteBuildServiceFileLocationWidget.getDefaultPath(fSelectedConnection);

        fBuildConfigLocationWidget = new RemoteBuildServiceFileLocationWidget(container, SWT.NONE, fSelectedConnection, configPath);
        GridData data = new GridData(SWT.FILL, SWT.FILL, true, false);
        data.horizontalSpan = 3;
        fBuildConfigLocationWidget.setLayoutData(data); // set layout to grab horizontal space
        fBuildConfigLocationWidget.addPathListener(new IIndexFilePathChangeListener() {
			public void pathChanged(String newPath) {
				updateProvider();
			}
		});
        
        updateProvider();
	}
    
	protected void updateProvider() {
		String provider = ""; //$NON-NLS-1$
		String name = ""; //$NON-NLS-1$
		if (fSelectedConnection != null) {
			provider = fSelectedConnection.getRemoteServices().getId();
			name = fSelectedConnection.getName();
		}
		fProviderWorkingCopy.putString(RemoteBuildServiceProvider.REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_PROVIDER_ID, provider);
		fProviderWorkingCopy.putString(RemoteBuildServiceProvider.REMOTE_BUILD_SERVICE_PROVIDER_REMOTE_TOOLS_CONNECTION_NAME, name);
		fProviderWorkingCopy.putString(RemoteBuildServiceProvider.REMOTE_BUILD_SERVICE_PROVIDER_CONFIG_LOCATION, fBuildConfigLocationWidget.getConfigLocationPath());
	}
	

	public IWizard getWizard(IServiceProvider provider, IWizardPage page) {
		// TODO Auto-generated method stub
		return null;
	}

	public WizardPage[] getWizardPages(IWizard wizard, IServiceProvider provider) {
		// TODO Auto-generated method stub
		return null;
	}

}
