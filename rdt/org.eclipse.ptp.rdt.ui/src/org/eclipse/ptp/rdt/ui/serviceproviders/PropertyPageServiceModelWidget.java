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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Set;

import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.wizards.ServiceModelWidget;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.TableItem;

public class PropertyPageServiceModelWidget extends ServiceModelWidget {
	
	/**
	 * Find available remote services and service providers for the given project and
	 * add them to the table
	 * @param project
	 */
	public void updateServicesTable(IProject project) {
		fTable.removeAll();
		fProviderIDToProviderMap = new HashMap<String, IServiceProvider>();
		fServiceIDToSelectedProviderID = new HashMap<String, String>();
		createTableContent(project);		
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.ui.wizards.ServiceModelWidget#createTableContent(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected void createTableContent(IProject project) {
		if(project == null) {
			super.createTableContent(project);
		} else {		
			//read the project's configuration and restore
			final ServiceModelManager serviceModelManager = ServiceModelManager.getInstance();
	
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			ServiceConfiguration config = (ServiceConfiguration) serviceModelManager.getConfiguration(project, info.getConfigurationName());
	
			Set<IService> services = config.getServices();
			Iterator<IService> iterator = services.iterator();
			while (iterator.hasNext()) {
				IService service = iterator.next();
				IServiceProvider provider = config.getServiceProvider(service);
				
				TableItem item = new TableItem (fTable, SWT.NONE);
	
				// column 0 lists the name of the service
				item.setText (0, service.getName());
				item.setData(SERVICE_KEY, service);
				
				// column 1 holds a dropdown with a list of providers
				item.setText(1, provider.getName());
				item.setData(PROVIDER_KEY, provider);
				
				// column 2 holds the configuration string of the provider's current configuration 
				String configString = provider.getConfigurationString();
				if (configString == null) {
					configString = Messages.getString("ServiceModelWidget.4"); //$NON-NLS-1$
				}
				item.setText(2, configString);
				
				fServiceIDToSelectedProviderID.put(service.getId(), provider.getId());
				fProviderIDToProviderMap.put(provider.getId(), provider);
			}
		}		
	}
}
