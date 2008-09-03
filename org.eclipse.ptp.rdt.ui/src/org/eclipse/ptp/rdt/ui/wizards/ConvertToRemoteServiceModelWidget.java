/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import java.util.Map;

import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Listener;

/**
 * A table widget listing remote services and service providers.  This widget is used in 
 * ConvertToRemoteWizardPage and it stores information on the remote service model for the
 * projects to be converted.
 * @author vkong
 *
 */
public class ConvertToRemoteServiceModelWidget extends ServiceModelWidget {
	
	IProject currentProject;
	
	Map<IProject, Map<String,String>> projectToServices = new HashMap<IProject, Map<String,String>>();
	Map<IProject, Map<String,IServiceProvider>> projectToProviders = new HashMap<IProject, Map<String,IServiceProvider>>();
	
	public class ConfigureListener2 extends ConfigureListener {
		public void handleEvent(Event event) {
			super.handleEvent(event);
			//users have to configure the services manually by clicking the configure button, not done automatically
			//once the services have been configured, they will be saved
			projectToServices.put(currentProject, fServiceIDToSelectedProviderID);
			projectToProviders.put(currentProject, fProviderIDToProviderMap);			
		}
	}
	
	/**
	 * Find available remote services and service providers for the given project and
	 * add them to the table
	 * @param project
	 */
	public void addServicesToTable(IProject project) {
		fTable.removeAll();
		fProviderIDToProviderMap = new HashMap<String, IServiceProvider>();
		fServiceIDToSelectedProviderID = new HashMap<String, String>();
		currentProject = project;
		getContributedServices(project);		
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

}
