/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderDescriptor;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ui.IMemento;

/**
 * This class is only used to bridge between the old RM factory model and the new RMs backed
 * by service configurations.
 * 
 * @author greg
 *
 */
public class MPICH2RMServiceProviderFactory extends AbstractResourceManagerFactory {

	public static final String RM_FACTORY_ID = "org.eclipse.ptp.rm.mpi.mpich2.resourcemanager"; //$NON-NLS-1$
	
	private static final String LAUNCH_SERVICE = "org.eclipse.ptp.core.LaunchService"; //$NON-NLS-1$
	private static final String MPICH2_SERVICE_PROVIDER = "org.eclipse.ptp.rm.mpi.mpich2.MPICH2ServiceProvider"; //$NON-NLS-1$
	private static final String TAG_UNIQUE_NAME = "uniqName"; //$NON-NLS-1$
	
	private static final IServiceModelManager fServiceManager = ServiceModelManager.getInstance();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#copyConfiguration(org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public IResourceManagerConfiguration copyConfiguration(
			IResourceManagerConfiguration configuration) {
		return (IResourceManagerConfiguration)configuration.clone();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#create(org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public IResourceManagerControl create(IResourceManagerConfiguration config) {
		PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new MPICH2ResourceManager(Integer.valueOf(universe.getNextResourceManagerId()), universe, config);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#createConfiguration()
	 */
	public IResourceManagerConfiguration createConfiguration() {
		IServiceConfiguration config = fServiceManager.newServiceConfiguration(Messages.MPICH2ResourceManagerConfiguration_defaultName);
		IService service = fServiceManager.getService(LAUNCH_SERVICE);
		if (service != null) {
			IServiceProviderDescriptor desc = service.getProviderDescriptor(MPICH2_SERVICE_PROVIDER);
			if (desc != null) {
				IServiceProvider provider = fServiceManager.getServiceProvider(desc);
				if (provider instanceof AbstractResourceManagerServiceProvider) {
					((AbstractResourceManagerServiceProvider)provider).setUniqueName(config.getId());
					config.setServiceProvider(service, provider);
					return (AbstractResourceManagerServiceProvider)provider;
				}
			}
		}
		fServiceManager.remove(config);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#loadConfiguration(org.eclipse.ui.IMemento)
	 */
	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		String serviceConfigId = memento.getString(TAG_UNIQUE_NAME);
		if (serviceConfigId != null) {
			IServiceConfiguration config = fServiceManager.getConfiguration(serviceConfigId);
			if (config != null) {
				IService service = fServiceManager.getService(LAUNCH_SERVICE);
				IServiceProvider provider = config.getServiceProvider(service);
				if (provider instanceof AbstractResourceManagerServiceProvider) {
					return (AbstractResourceManagerServiceProvider)provider;
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#saveConfiguration(org.eclipse.ptp.rmsystem.IResourceManagerConfiguration, org.eclipse.ptp.core.elementcontrols.IResourceManagerControl)
	 */
	public void saveConfiguration(IResourceManagerConfiguration configuration, IResourceManagerControl resourceManager) {
		IServiceConfiguration config = fServiceManager.getConfiguration(configuration.getUniqueName());
		if (config != null) {
			IService service = fServiceManager.getService(LAUNCH_SERVICE);
			if (configuration instanceof AbstractResourceManagerServiceProvider) {
				config.setServiceProvider(service, (IServiceProvider)configuration);
			}
		}
		resourceManager.setConfiguration(configuration);		
	}
}
