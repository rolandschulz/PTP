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
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.resources.IProject;
import org.eclipse.ptp.internal.rdt.core.index.RemoteFastIndexer;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
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
	
	/**
	 * @throws NullPointerException if any of the parameters are null
	 */
	public static void configure(IProject project, Map<String, String> serviceIDToProviderIDMap, 
			Map<String, IServiceProvider> providerIDToProviderMap) {
		
		if(project == null)
			throw new NullPointerException();
				
		final ServiceModelManager serviceModelManager = ServiceModelManager.getInstance();

		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		ServiceConfiguration config = new ServiceConfiguration(info.getConfigurationName());
		
		for(String serviceID : serviceIDToProviderIDMap.keySet()) {
			IService service = serviceModelManager.getService(serviceID);
			String serviceProviderID = serviceIDToProviderIDMap.get(serviceID);
			IServiceProvider provider = providerIDToProviderMap.get(serviceProviderID);
			config.setServiceProvider(service, provider);

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
		Properties properties = new Properties();
		properties.put(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT, ""); //$NON-NLS-1$
		IndexerPreferences.setProperties(project, IndexerPreferences.SCOPE_PROJECT_PRIVATE, properties);
	}
}
