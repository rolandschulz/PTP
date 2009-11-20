/*******************************************************************************
 * Copyright (c) 2008, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.wizards;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.ptp.rdt.ui.serviceproviders.NullBuildServiceProvider;
import org.eclipse.ptp.rdt.ui.serviceproviders.NullCIndexServiceProvider;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.TableItem;

/**
 * A table widget listing remote services and service providers.  This widget is used in 
 * ConvertToRemoteWizardPage and it stores information on the remote service model for the
 * projects to be converted.
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author vkong
 *
 */
public class ConvertToRemoteServiceModelWidget extends ServiceModelWidget {
	
	IProject fCurrentProject;
	
	Map<IProject, Map<String,String>> projectToServices = new HashMap<IProject, Map<String,String>>();
	Map<IProject, Map<String,IServiceProvider>> projectToProviders = new HashMap<IProject, Map<String,IServiceProvider>>();
	
	public class ConfigureListener2 extends ConfigureListener {
		public void handleEvent(Event event) {
			super.handleEvent(event);
			//users have to configure the services manually by clicking the configure button, not done automatically
			//once the services have been configured, they will be saved
			projectToServices.put(fCurrentProject, fServiceIDToSelectedProviderID);
			projectToProviders.put(fCurrentProject, fProviderIDToProviderMap);			
		}
	}
	
	/**
	 * Find available remote services and service providers for the given project and
	 * add them to the table
	 * @param project
	 */
	public void addServicesToTable(IProject project) {
		fProviderIDToProviderMap = new HashMap<String, IServiceProvider>();
		fServiceIDToSelectedProviderID = new HashMap<String, String>();
		fCurrentProject = project;
		createTableContent(project);		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rdt.ui.wizards.ServiceModelWidget#createTableContent(org.eclipse.core.resources.IProject)
	 */
	@Override
	protected void createTableContent(IProject project) {
		fTable.removeAll();
		if (project == null) {
			super.createTableContent(project);
		} else if (projectToServices.get(project) == null && projectToProviders.get(project) == null) {
			super.createTableContent(project);
		} else {
			Set<IService> services = getContributedServices(project);
			Iterator<IService> iterator = services.iterator();
			
			Map<String, String> serviceIDToSelectedProviderID = projectToServices.get(fCurrentProject);
			Map<String, IServiceProvider> providerIDToProviderMap = projectToProviders.get(fCurrentProject);
			
			while(iterator.hasNext()) {
				final IService service = iterator.next();			
				
				TableItem item = new TableItem (fTable, SWT.NONE);

				// column 0 lists the name of the service
				item.setText (0, service.getName());
				item.setData(SERVICE_KEY, service);
				
				String providerID = serviceIDToSelectedProviderID.get(service.getId());
				IServiceProvider provider= providerIDToProviderMap.get(providerID);
				String configString;
				
				//remember user's selection and restore
				if (provider != null) {				
					// column 1 holds a dropdown with a list of providers
					item.setText(1, provider.getName());
					item.setData(PROVIDER_KEY, provider);
					
					configString = provider.getConfigurationString();					
				} else { //no previous user selection
					
					// column 1 holds a dropdown with a list of providers
					// default entry is the null provider if there is one			
					IServiceProviderDescriptor descriptor;
					if (service.getId().compareTo(NullBuildServiceProvider.SERVICE_ID) == 0)
						descriptor = service.getProviderDescriptor(NullBuildServiceProvider.ID);
					else if (service.getId().compareTo(NullCIndexServiceProvider.SERVICE_ID) == 0)
						descriptor = service.getProviderDescriptor(NullCIndexServiceProvider.ID);
					else
						descriptor = service.getProviders().iterator().next();
					item.setText(1, descriptor.getName());
					item.setData(PROVIDER_KEY, descriptor);
					
					//No actual providers are created yet so there's no configuration
					configString =  Messages.getString("ServiceModelWidget.4"); //$NON-NLS-1$
					
					if (descriptor.getId().compareTo(NullBuildServiceProvider.ID) == 0 ||
							descriptor.getId().compareTo(NullCIndexServiceProvider.ID) == 0) {
						
						//since the null providers are choosen, setup the service provider mappings
						ServiceModelManager manager = ServiceModelManager.getInstance();
						IServiceProvider serviceProvider = manager.getServiceProvider(descriptor);
						
						configString = serviceProvider.getConfigurationString();
						// column 2 holds the configuration string of the provider's current configuration 
						if (configString == null) {
							configString = Messages.getString("ServiceModelWidget.4"); //$NON-NLS-1$
						}
						fServiceIDToSelectedProviderID.put(service.getId(), descriptor.getId());
						fProviderIDToProviderMap.put(descriptor.getId(), serviceProvider);						
					}
				}
				
				// column 2 holds the configuration string of the provider's current configuration 
				if (configString == null) {
					configString = Messages.getString("ServiceModelWidget.4"); //$NON-NLS-1$
				}
				item.setText(2, configString);
			}
			
			fServiceIDToSelectedProviderID.putAll(serviceIDToSelectedProviderID);
			fProviderIDToProviderMap.putAll(providerIDToProviderMap);
		}
		projectToServices.put(project, fServiceIDToSelectedProviderID);
		projectToProviders.put(project, fProviderIDToProviderMap);
		
		if (fConfigChangeListener != null)
			fConfigChangeListener.handleEvent(null);
		
		//reset the configuration button
		if (fConfigureButton != null)
			fConfigureButton.setEnabled(false);
	}

	public void emptyTable() {
		fTable.removeAll();	
	}
	
	protected Listener getConfigureListener() {
		return new ConfigureListener2();		
	}

	/**
	 * @return the projectToServices
	 */
	public Map<IProject, Map<String, String>> getProjectToServices() {
		return projectToServices;
	}

	/**
	 * @return the projectToProviders
	 */
	public Map<IProject, Map<String, IServiceProvider>> getProjectToProviders() {
		return projectToProviders;
	}
	
	public boolean isConfigured(Object[] selectedProjects) {
		boolean configured = true;
		if (selectedProjects == null || selectedProjects.length < 1)
			return false;
		for (int i = 0; i < selectedProjects.length; i++) {
			IProject project = (IProject) selectedProjects[i];
			Map<String, String> serviceIDToSelectedProviderID = projectToServices.get(project);
			Map<String, IServiceProvider> providerIDToProviderMap = projectToProviders.get(project);
			if (serviceIDToSelectedProviderID == null || providerIDToProviderMap == null)
				return false;
			configured = configured && isConfigured(project, serviceIDToSelectedProviderID, providerIDToProviderMap);
		}
		
		return configured;
		
	}

}
