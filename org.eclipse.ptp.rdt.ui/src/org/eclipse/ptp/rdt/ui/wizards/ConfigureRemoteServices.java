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

import java.io.IOException;
import java.util.Map;
import java.util.Properties;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.internal.rdt.core.index.RemoteFastIndexer;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.rdt.ui.messages.Messages;
import org.eclipse.rse.internal.connectorservice.dstore.Activator;

/**
 * Configure remote services for a project with the available services and service providers
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * @author vkong
 *
 */
public class ConfigureRemoteServices {
	
	public static String DEFAULT_CONFIG = Messages.getString("ConfigureRemoteServices.0"); //$NON-NLS-1$
	
	/**
	 * @throws NullPointerException if any of the parameters are null
	 */
	public static void configure(IProject project, Map<String, String> serviceIDToProviderIDMap, 
			Map<String, IServiceProvider> providerIDToProviderMap, IProgressMonitor monitor) {
		
		monitor.beginTask("actual Configure remote services", 100); //$NON-NLS-1$
		
		if(project == null)
			throw new NullPointerException();
				
		final ServiceModelManager serviceModelManager = ServiceModelManager.getInstance();

		ServiceConfiguration config = new ServiceConfiguration(DEFAULT_CONFIG);
		
		int workUnit = 90/serviceIDToProviderIDMap.size();
		
		for(String serviceID : serviceIDToProviderIDMap.keySet()) {
			IService service = serviceModelManager.getService(serviceID);
			String serviceProviderID = serviceIDToProviderIDMap.get(serviceID);
			IServiceProvider provider = providerIDToProviderMap.get(serviceProviderID);
			config.setServiceProvider(service, provider);
			
			monitor.worked(workUnit);

			//have to set it as active
			serviceModelManager.putConfiguration(project, config);
			serviceModelManager.setActiveConfiguration(project, config);
		}
		
		try {
			serviceModelManager.saveModelConfiguration();
		} catch (IOException e) {
			Activator.logError(e.toString(), e);
		}
		
		ICProject cProject = CModelManager.getDefault().getCModel().getCProject(project);
		CCorePlugin.getIndexManager().setIndexerId(cProject, RemoteFastIndexer.ID);
		monitor.worked(10);
		monitor.done();		
	}
}
