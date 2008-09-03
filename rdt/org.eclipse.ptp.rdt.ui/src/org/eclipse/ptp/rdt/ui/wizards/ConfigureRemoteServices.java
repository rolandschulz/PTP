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
import java.util.Iterator;
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

/**
 * Configure remote services for a project with the available services and service providers
 * @author vkong
 *
 */
public class ConfigureRemoteServices {
	public static void configure(IProject project, Map<String, String> serviceIDToProviderIDMap, 
			Map<String, IServiceProvider> providerIDToProviderMap) throws InvocationTargetException,
			InterruptedException {
		
		final ServiceModelManager serviceModelManager = ServiceModelManager.getInstance();

		if(project == null)
			throw new RuntimeException();
		
		IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
		
		ServiceConfiguration config = new ServiceConfiguration(info.getConfigurationName());
		
		Iterator<String> iterator = serviceIDToProviderIDMap.keySet().iterator();
		
		while(iterator.hasNext()) {
			String serviceID = iterator.next();
			
			IService service = serviceModelManager.getService(serviceID);
			
			String serviceProviderID = serviceIDToProviderIDMap.get(serviceID);

			IServiceProvider provider = providerIDToProviderMap.get(serviceProviderID);

			config.setServiceProvider(service, provider);

			// note: we only have one config at this point so it will be
			// active by default
			serviceModelManager.addConfiguration(project, config);
		}
		
		ICProject cProject = CModelManager.getDefault().getCModel().getCProject(project);
		CCorePlugin.getIndexManager().setIndexerId(cProject, RemoteFastIndexer.ID);
		Properties properties = new Properties();
		properties.put(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT, ""); //$NON-NLS-1$
		IndexerPreferences.setProperties(project, IndexerPreferences.SCOPE_PROJECT_PRIVATE, properties);
	}
}
