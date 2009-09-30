/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rmsystem;

import java.util.UUID;

import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IServiceConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelEvent;
import org.eclipse.ptp.services.core.IServiceModelEventListener;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;
import org.eclipse.ptp.services.core.ServiceProvider;
import org.eclipse.ui.IMemento;

public abstract class AbstractResourceManagerServiceProvider 	
	extends ServiceProvider 
	implements IResourceManagerConfiguration
{
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_UNIQUE_NAME = "uniqName"; //$NON-NLS-1$
	private static final String TAG_CONNECTION_NAME = "connectionName"; //$NON-NLS-1$
	private static final String TAG_REMOTE_SERVICES_ID = "remoteServicesID"; //$NON-NLS-1$
	private static final String TAG_STATE = "state"; //$NON-NLS-1$
	
	private final IModelManager fModelManager = PTPCorePlugin.getDefault().getModelManager();
	private final IServiceModelManager fServiceManager = ServiceModelManager.getInstance();
	private final IService fLaunchService = fServiceManager.getService(IServiceConstants.LAUNCH_SERVICE);
	
	/*
	 * Keep a copy of our service configuration so we don't have to search for it
	 */
	private IServiceConfiguration fServiceConfiguration = null;
	
	private IModelManagerChildListener fModelListener = new IModelManagerChildListener() {
		
		public void handleEvent(IRemoveResourceManagerEvent e) {
			if (e.getResourceManager().getUniqueName().equals(getUniqueName())) {
				/*
				 * Unregister listeners first so we don't get called
				 * by another SERVICE_CONFIGURATION_CHANGED event
				 */
				unregisterListeners();
				
				if (fServiceConfiguration != null) {
					fServiceConfiguration.setServiceProvider(fLaunchService, null);
				}
			}
		}
		
		public void handleEvent(INewResourceManagerEvent e) {
			// Don't need to do anything
		}
		
		public void handleEvent(IChangedResourceManagerEvent e) {
			// Don't need to do anything
		}
	};
	
	private IServiceModelEventListener fEventListener = new IServiceModelEventListener() {
		
		public void handleEvent(IServiceModelEvent event) {
			IServiceConfiguration config = (IServiceConfiguration)event.getSource();
			IServiceProvider provider = config.getServiceProvider(fLaunchService);
			
			/*
			 * We get notified of events on any service configuration, so make sure that
			 * we only respond to ours.
			 */
			final boolean ourEvent = (provider instanceof IResourceManagerConfiguration &&
					((IResourceManagerConfiguration)provider).getUniqueName().equals(getUniqueName()));
			
			if (ourEvent) {
				switch (event.getType()) {
				case IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED:
					IResourceManager rm = fModelManager.getResourceManagerFromUniqueName(getUniqueName());
					if (rm != null) {
						fModelManager.removeResourceManager((IResourceManagerControl)rm);
					}
					break;
					
				case IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED:
					/*
					 * Remove old resource manager if there was one
					 */
					IServiceProvider oldProvider = event.getOldProvider();
					if (oldProvider != null && oldProvider instanceof IResourceManagerConfiguration) {
						IResourceManager oldRM = fModelManager.getResourceManagerFromUniqueName(((IResourceManagerConfiguration)oldProvider).getUniqueName());
						if (oldRM != null) {
							fModelManager.removeResourceManager((IResourceManagerControl)oldRM);
						}
					}
					
					/*
					 * Now, if we're being added, then add a new resource manager
					 */
					if (ourEvent) {
						fModelManager.addResourceManager(createResourceManager());
						fServiceConfiguration = config;
					}
					break;
				}
			}
		}
	};
	
	public AbstractResourceManagerServiceProvider() {
		registerListeners();
	}
	
	public AbstractResourceManagerServiceProvider(AbstractResourceManagerServiceProvider provider) {
		super(provider);
		setConnectionName(provider.getConnectionName());
		setDescription(provider.getDescription());
		setRemoteServicesId(provider.getRemoteServicesId());
		setResourceManagerId(provider.getResourceManagerId());
		setName(provider.getName());
		setUniqueName(null); // Generate another unique id
		registerListeners();
	}
	
	public void registerListeners() {
		fServiceManager.addEventListener(fEventListener, 
				IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED |
				IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED);
		fModelManager.addListener(fModelListener);
	}
	
	public void unregisterListeners() {
		fServiceManager.removeEventListener(fEventListener);
		fModelManager.removeListener(fModelListener);
	}
	
	@Override
	public abstract Object clone();

	/**
	 * Create a resource manager using this configuration.
	 *  
	 * @return resource manager
	 */
	public abstract IResourceManagerControl createResourceManager();
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getConnectionName()
	 */
	public String getConnectionName() {
		return getString(TAG_CONNECTION_NAME, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getDescription()
	 */
	public String getDescription() {
		return getString(TAG_DESCRIPTION, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.services.core.ServiceProvider#getName()
	 */
	@Override
	public String getName() {
		return getString(TAG_NAME, ""); //$NON-NLS-1$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getRemoteServicesId()
	 */
	public String getRemoteServicesId() {
		return getString(TAG_REMOTE_SERVICES_ID, ""); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getResourceManagerId()
	 */
	public String getResourceManagerId() {
		return super.getId();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getState()
	 */
	public ResourceManagerAttributes.State getState() {
		return ResourceManagerAttributes.State.valueOf(getString(TAG_STATE, ResourceManagerAttributes.State.STOPPED.toString()));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getType()
	 */
	public String getType() {
		return super.getName();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#getUniqueName()
	 */
	public String getUniqueName() {
		String name = getString(TAG_UNIQUE_NAME, null);
		if (name == null) {
			name = UUID.randomUUID().toString();
			putString(TAG_UNIQUE_NAME, name);
		}
		return name;
	}
	
	public boolean isConfigured() {
		return !getConnectionName().equals("") && !getRemoteServicesId().equals(""); //$NON-NLS-1$//$NON-NLS-2$
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#needsDebuggerLaunchHelp()
	 */
	public boolean needsDebuggerLaunchHelp() {
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#save(org.eclipse.ui.IMemento)
	 */
	public void save(IMemento memento) {
		/*
		 *  Not needed (needs to be @deprecated). Currently used to 
		 *  bridge between RM configurations and service configurations.
		 */
		memento.putString(TAG_UNIQUE_NAME, getUniqueName());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setConnectionName(java.lang.String)
	 */
	public void setConnectionName(String name) {
		putString(TAG_CONNECTION_NAME, name);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDescription(java.lang.String)
	 */
	public void setDescription(String description) {
		putString(TAG_DESCRIPTION, description);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setName(java.lang.String)
	 */
	public void setName(String name) {
		putString(TAG_NAME, name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setRemoteServicesId(java.lang.String)
	 */
	public void setRemoteServicesId(String id) {
		putString(TAG_REMOTE_SERVICES_ID, id);
	}
	
	/**
	 * @param id
	 */
	public void setResourceManagerId(String id) {
		// Do nothing (needs to be @deprecated)
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setState(org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes.State)
	 */
	public void setState(ResourceManagerAttributes.State state) {
		putString(TAG_STATE, state.name());
	}
	
	/**
	 * Set the IResourceManagerConfiguration unique name. This is only used to transition
	 * to the new service model framework. It is set to the name of the service configuration
	 * that was created for this service provider.
	 * 
	 * @param id
	 */
	public void setUniqueName(String id) {
		putString(TAG_UNIQUE_NAME, id);
	}
}
