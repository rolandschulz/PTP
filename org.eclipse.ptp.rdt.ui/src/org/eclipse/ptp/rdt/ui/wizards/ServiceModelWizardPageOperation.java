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
import org.eclipse.cdt.internal.core.CCoreInternals;
import org.eclipse.cdt.internal.core.model.CModelManager;
import org.eclipse.cdt.internal.core.pdom.indexer.IndexerPreferences;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.ui.wizards.MBSCustomPageManager;
import org.eclipse.cdt.ui.wizards.CDTCommonProjectWizard;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.ptp.internal.rdt.core.index.RemoteFastIndexer;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.rdt.services.core.ServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.osgi.service.prefs.BackingStoreException;
import org.osgi.service.prefs.Preferences;

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
	public void run(IProgressMonitor monitor) throws InvocationTargetException,
			InterruptedException {
		
		IProject project = null;
		
		final ServiceModelManager serviceModelManager = ServiceModelManager.getInstance();
		
		// go through the mappings of services and set the service model to match
		Object obj = MBSCustomPageManager.getPageProperty(
				ServiceModelWizardPage.SERVICE_MODEL_WIZARD_PAGE_ID,
				ServiceModelWizardPage.SELECTED_PROVIDERS_MAP_PROPERTY);
		
		if(obj instanceof Map) {
			Map<String, String> serviceIDToProviderIDMap = (Map<String, String>) obj;
			
			IWizard wizard = MBSCustomPageManager.getPageData(ServiceModelWizardPage.SERVICE_MODEL_WIZARD_PAGE_ID).getWizardPage().getWizard();
			
			if(wizard instanceof CDTCommonProjectWizard)
				project = ((CDTCommonProjectWizard) wizard).getLastProject();
			
			if(project == null)
				throw new RuntimeException();
			
			IManagedBuildInfo info = ManagedBuildManager.getBuildInfo(project);
			
			ServiceConfiguration config = new ServiceConfiguration(info.getConfigurationName());
			
			Iterator<String> iterator = serviceIDToProviderIDMap.keySet().iterator();
			
			while(iterator.hasNext()) {
				String serviceID = iterator.next();
				
				IService service = serviceModelManager.getService(serviceID);
				
				String serviceProviderID = serviceIDToProviderIDMap.get(serviceID);
				
				IServiceProviderDescriptor serviceProviderDescriptor = service.getProviderDescriptor(serviceProviderID);
				
				Object obj2 = MBSCustomPageManager.getPageProperty(
						ServiceModelWizardPage.SERVICE_MODEL_WIZARD_PAGE_ID,
						ServiceModelWizardPage.ID_TO_PROVIDERS_MAP_PROPERTY);
				
				if (obj2 instanceof Map) {
					Map<String, IServiceProvider> providerIDToProviderMap = (Map<String, IServiceProvider>) obj2;
					
					IServiceProvider provider = providerIDToProviderMap.get(serviceProviderID);

					config.setServiceProvider(service, provider);

					// note: we only have one config at this point so it will be
					// active by default
					serviceModelManager.addConfiguration(project, config);
				}
				
			}
		}
		
		ICProject cProject = CModelManager.getDefault().getCModel().getCProject(project);
		CCorePlugin.getIndexManager().setIndexerId(cProject, RemoteFastIndexer.ID);
		Properties properties = new Properties();
		properties.put(IndexerPreferences.KEY_FILES_TO_PARSE_UP_FRONT, ""); //$NON-NLS-1$
		IndexerPreferences.setProperties(project, IndexerPreferences.SCOPE_PROJECT_PRIVATE, properties);
	}
}
