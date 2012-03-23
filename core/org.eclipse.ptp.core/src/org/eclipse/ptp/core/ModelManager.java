/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.core;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IExtension;
import org.eclipse.core.runtime.IExtensionPoint;
import org.eclipse.core.runtime.IExtensionRegistry;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.events.IResourceManagerAddedEvent;
import org.eclipse.ptp.core.events.IResourceManagerChangedEvent;
import org.eclipse.ptp.core.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.events.IResourceManagerRemovedEvent;
import org.eclipse.ptp.core.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.core.elements.PUniverse;
import org.eclipse.ptp.internal.core.events.ResourceManagerAddedEvent;
import org.eclipse.ptp.internal.core.events.ResourceManagerChangedEvent;
import org.eclipse.ptp.internal.core.events.ResourceManagerErrorEvent;
import org.eclipse.ptp.internal.core.events.ResourceManagerRemovedEvent;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.IResourceManagerControlFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerMonitor;
import org.eclipse.ptp.rmsystem.IResourceManagerMonitorFactory;
import org.eclipse.ptp.rmsystem.ResourceManagerServiceProvider;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelEvent;
import org.eclipse.ptp.services.core.IServiceModelEventListener;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * Model manager for resource manager framework.
 * 
 * @since 5.0
 */
public class ModelManager implements IModelManager {

	private class RMFactory {
		private String fId;
		private String fControlId;
		private String fMonitorId;
		private AbstractResourceManagerFactory fFactory;

		/**
		 * @return the controlFactory
		 */
		public String getControlId() {
			return fControlId;
		}

		public AbstractResourceManagerFactory getFactory() {
			return fFactory;
		}

		/**
		 * @return the id
		 */
		public String getId() {
			return fId;
		}

		/**
		 * @return the monitorFactory
		 */
		public String getMonitorId() {
			return fMonitorId;
		}

		/**
		 * @param id
		 *            the controlFactory to set
		 */
		public void setControlId(String id) {
			fControlId = id;
		}

		public void setFactory(AbstractResourceManagerFactory factory) {
			fFactory = factory;
		}

		/**
		 * @param id
		 *            the id to set
		 */
		public void setId(String id) {
			fId = id;
		}

		/**
		 * @param monitorFactory
		 *            the monitorFactory to set
		 */
		public void setMonitorId(String id) {
			fMonitorId = id;
		}
	}

	private class RMStartupJob extends Job {
		private final IResourceManager[] resourceManagers;

		public RMStartupJob(IResourceManager[] rms) {
			super(Messages.ModelManager_0);
			resourceManagers = rms;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SubMonitor subMon = SubMonitor.convert(monitor, resourceManagers.length);
			try {
				try {
					for (IResourceManager rm : resourceManagers) {
						rm.start(subMon.newChild(1));
						if (subMon.isCanceled()) {
							return Status.CANCEL_STATUS;
						}
					}
				} catch (CoreException e) {
					return e.getStatus();
				}
				return Status.OK_STATUS;
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}

	}

	private static String RM_NAME = "resourceManager"; //$NON-NLS-1$
	private static String RM_CONTROL_NAME = "resourceManagerControl"; //$NON-NLS-1$
	private static String RM_MONITOR_NAME = "resourceManagerMonitor"; //$NON-NLS-1$
	private static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static String EXTENSION_POINT = "org.eclipse.ptp.core.resourceManagers"; //$NON-NLS-1$

	private final IServiceModelEventListener fServiceEventListener = new IServiceModelEventListener() {

		public void handleEvent(IServiceModelEvent event) {
			switch (event.getType()) {
			case IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED: {
				/*
				 * The service configuration has been removed, so remove the resource manager associated with it (if any).
				 */
				IServiceProvider provider = ((IServiceConfiguration) event.getSource()).getServiceProvider(fLaunchService);
				if (provider != null && provider instanceof ResourceManagerServiceProvider) {
					IResourceManager rm = getResourceManagerFromUniqueName(((ResourceManagerServiceProvider) provider)
							.getUniqueName());
					if (rm != null) {
						doRemoveResourceManager(rm);
					}
				}
				break;
			}

			case IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED: {
				/*
				 * The service configuration has changed. Check if the old provider is null, in which case this is likely to be a
				 * new provider being added to a new configuration.
				 */
				IServiceConfiguration config = (IServiceConfiguration) event.getSource();
				if (event.getOldProvider() == null) {
					IServiceProvider newProvider = config.getServiceProvider(fLaunchService);
					if (newProvider != null && newProvider instanceof ResourceManagerServiceProvider) {
						/*
						 * Check if the rm already exists. Only add a new one if it doesn't.
						 */
						IResourceManager rm = getResourceManagerFromUniqueName(((ResourceManagerServiceProvider) newProvider)
								.getUniqueName());
						if (rm == null) {
							RMFactory factory = getResourceManagerFactory(newProvider.getId());
							if (factory != null) {
								addResourceManager(createResourceManager(factory, newProvider));
							}
						}
					}
				}
				break;
			}

			case IServiceModelEvent.SERVICE_PROVIDER_CHANGED: {
				/*
				 * The service provider has been modified, so let the UI know that something has changed and it should update.
				 */
				IServiceProvider provider = (IServiceProvider) event.getSource();
				if (provider != null && provider instanceof ResourceManagerServiceProvider) {
					IResourceManager rm = getResourceManagerFromUniqueName(((ResourceManagerServiceProvider) provider)
							.getUniqueName());
					if (rm != null) {
						fireResourceManagerChanged(rm);
					}
				}
				break;
			}
			}
		}
	};

	private static final ModelManager fInstance = new ModelManager();

	private final ListenerList fResourceManagerListeners = new ListenerList();

	private final IServiceModelManager fServiceManager = ServiceModelManager.getInstance();
	private final Map<String, IResourceManager> fResourceManagers = new HashMap<String, IResourceManager>();
	private final IService fLaunchService = fServiceManager.getService(IServiceConstants.LAUNCH_SERVICE);
	private final IPUniverse fUniverse = new PUniverse();

	private Map<String, RMFactory> fResourceManagerFactories = null;

	private Map<String, IResourceManagerControlFactory> fResourceManagerControlFactories = null;
	private Map<String, IResourceManagerMonitorFactory> fResourceManagerMonitorFactories = null;

	public ModelManager() {
		fServiceManager.addEventListener(fServiceEventListener, IServiceModelEvent.SERVICE_CONFIGURATION_ADDED
				| IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED | IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED
				| IServiceModelEvent.SERVICE_PROVIDER_CHANGED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#addListener(org.eclipse.ptp.core.listeners .IResourceManagerListener)
	 */
	public void addListener(IResourceManagerListener listener) {
		fResourceManagerListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#addResourceManager(org.eclipse.ptp .rmsystem.IResourceManager)
	 */
	public void addResourceManager(IResourceManager rm) {
		synchronized (fResourceManagers) {
			fResourceManagers.put(rm.getUniqueName(), rm);
		}
		fireNewResourceManager(rm);
	}

	/**
	 * @since 6.0
	 */
	public void changeResourceManagerUniqueName(String oldName, String newName) {
		IResourceManager rm = getResourceManagerFromUniqueName(oldName);
		if (rm != null) {
			synchronized (fResourceManagers) {
				fResourceManagers.put(oldName, null);
				fResourceManagers.put(newName, rm);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#addResourceManagers(org.eclipse.ptp .rmsystem.IResourceManager[])
	 */
	public void addResourceManagers(IResourceManager[] rms) {
		for (IResourceManager rm : rms) {
			addResourceManager(rm);
		}
	}

	/**
	 * Create a base configuration from the given service provider
	 * 
	 * @param provider
	 *            service provider
	 * @return base configuration
	 */
	public IResourceManagerConfiguration createBaseConfiguration(IServiceProvider provider) {
		RMFactory factory = getResourceManagerFactory(provider.getId());
		if (factory != null) {
			return factory.getFactory().createConfiguration(provider);
		}
		return null;
	}

	/**
	 * Create a control configuration from the given service provider
	 * 
	 * @param provider
	 *            service provider
	 * @return control configuration
	 */
	public IResourceManagerComponentConfiguration createControlConfiguration(IServiceProvider provider) {
		RMFactory factory = getResourceManagerFactory(provider.getId());
		if (factory != null) {
			IResourceManagerControlFactory controlFactory = fResourceManagerControlFactories.get(factory.getControlId());
			if (controlFactory != null) {
				return controlFactory.createControlConfiguration(provider);
			}
		}
		return null;
	}

	/**
	 * Create a monitor configuration from the given service provider
	 * 
	 * @param provider
	 *            service provider
	 * @return monitor configuration
	 */
	public IResourceManagerComponentConfiguration createMonitorConfiguration(IServiceProvider provider) {
		RMFactory factory = getResourceManagerFactory(provider.getId());
		if (factory != null) {
			IResourceManagerMonitorFactory monitorFactory = fResourceManagerMonitorFactories.get(factory.getMonitorId());
			if (monitorFactory != null) {
				return monitorFactory.createMonitorConfiguration(provider);
			}
		}
		return null;
	}

	/**
	 * Fire an event to notify that a resource manager has changed
	 * 
	 * @param rm
	 *            rm that has changed
	 * @since 5.0
	 */
	public void fireResourceManagerChanged(IResourceManager rm) {
		IResourceManagerChangedEvent e = new ResourceManagerChangedEvent(rm);

		for (Object listener : fResourceManagerListeners.getListeners()) {
			((IResourceManagerListener) listener).handleEvent(e);
		}
	}

	/**
	 * Fire an event to notify that a resource manager error has occurred
	 * 
	 * @param rm
	 *            rm that caused the error
	 * @param message
	 *            error message
	 */
	public void fireResourceManagerError(IResourceManager rm, String message) {
		IResourceManagerErrorEvent e = new ResourceManagerErrorEvent(rm, message);

		for (Object listener : fResourceManagerListeners.getListeners()) {
			((IResourceManagerListener) listener).handleEvent(e);
		}
	}

	public String getControlFactoryId(String rmId) {
		RMFactory factory = getResourceManagerFactory(rmId);
		if (factory != null) {
			return factory.getControlId();
		}
		return null;
	}

	public String getMonitorFactoryId(String rmId) {
		RMFactory factory = getResourceManagerFactory(rmId);
		if (factory != null) {
			return factory.getMonitorId();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#getResourceManagerFromUniqueName(java .lang.String)
	 */
	public IResourceManager getResourceManagerFromUniqueName(String rmUniqueName) {
		synchronized (fResourceManagers) {
			return fResourceManagers.get(rmUniqueName);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#getResourceManagers()
	 */
	public IResourceManager[] getResourceManagers() {
		synchronized (fResourceManagers) {
			return fResourceManagers.values().toArray(new IResourceManager[0]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelPresentation#getUniverse()
	 */
	public IPUniverse getUniverse() {
		return fUniverse;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#handleEvent
	 * (org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent)
	 */
	public void handleEvent(IResourceManagerErrorEvent e) {
		// Ignore - handled by listener on RM
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#loadResourceManagers()
	 */
	public void loadResourceManagers() throws CoreException {
		Set<IResourceManager> rmsNeedStarting = new HashSet<IResourceManager>();

		/*
		 * Need to force service model to load so that the resource managers are created.
		 */
		fServiceManager.getActiveConfiguration();

		synchronized (fResourceManagers) {
			for (IResourceManager rm : fResourceManagers.values()) {
				if (rm.getConfiguration().getAutoStart()) {
					rmsNeedStarting.add(rm);
				}
			}
		}

		if (Preferences.getBoolean(PTPCorePlugin.getUniqueIdentifier(), PreferenceConstants.PREFS_AUTO_START_RMS)) {
			startResourceManagers(rmsNeedStarting.toArray(new IResourceManager[0]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#removeListener(org.eclipse.ptp.core .listeners.IResourceManagerListener)
	 */
	public void removeListener(IResourceManagerListener listener) {
		fResourceManagerListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#removeResourceManager(org.eclipse. ptp.rmsystem.IResourceManager)
	 */
	public void removeResourceManager(IResourceManager rm) {
		IServiceProvider provider = (IServiceProvider) rm.getConfiguration().getAdapter(IServiceProvider.class);
		if (provider != null) {
			removeProviderFromConfiguration(provider);
		}
		doRemoveResourceManager(rm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#removeResourceManagers(org.eclipse .ptp.rmsystem.IResourceManager[])
	 */
	public void removeResourceManagers(IResourceManager[] rms) {
		for (IResourceManager rm : rms) {
			removeResourceManager(rm);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#saveResourceManagers()
	 */
	public void saveResourceManagers() {
		// No longer needed
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#shutdown()
	 */
	public void shutdown() throws CoreException {
		saveResourceManagers();
		stopResourceManagers();
		disposeResourceManagers();
		fResourceManagerListeners.clear();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#start()
	 */
	public void start() throws CoreException {
		loadResourceManagers();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#stopResourceManagers()
	 */
	public void stopResourceManagers() throws CoreException {
		for (IResourceManager resourceManager : getResourceManagers()) {
			resourceManager.stop();
		}
	}

	private String addControlFactory(IConfigurationElement element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		if (element.getAttribute(CLASS_ATTRIBUTE) != null) {
			try {
				IResourceManagerControlFactory factory = (IResourceManagerControlFactory) element
						.createExecutableExtension(CLASS_ATTRIBUTE);
				fResourceManagerControlFactories.put(id, factory);
			} catch (Exception e) {
				PTPCorePlugin.log(e);
			}
		}
		return id;
	}

	private String addMonitorFactory(IConfigurationElement element) {
		String id = element.getAttribute(ID_ATTRIBUTE);
		if (element.getAttribute(CLASS_ATTRIBUTE) != null) {
			try {
				IResourceManagerMonitorFactory factory = (IResourceManagerMonitorFactory) element
						.createExecutableExtension(CLASS_ATTRIBUTE);
				fResourceManagerMonitorFactories.put(id, factory);
			} catch (Exception e) {
				PTPCorePlugin.log(e);
			}
		}
		return id;
	}

	private IResourceManager createResourceManager(RMFactory factory, IServiceProvider provider) {
		IResourceManagerControl control = null;
		IResourceManagerMonitor monitor = null;
		IResourceManagerControlFactory controlFactory = fResourceManagerControlFactories.get(factory.getControlId());
		if (controlFactory != null) {
			IResourceManagerComponentConfiguration controlConfig = controlFactory.createControlConfiguration(provider);
			control = controlFactory.createControl(controlConfig);
		}
		IResourceManagerMonitorFactory monitorFactory = fResourceManagerMonitorFactories.get(factory.getMonitorId());
		if (monitorFactory != null) {
			IResourceManagerComponentConfiguration monitorConfig = monitorFactory.createMonitorConfiguration(provider);
			monitor = monitorFactory.createMonitor(monitorConfig);
		}
		IResourceManagerConfiguration config = factory.getFactory().createConfiguration(provider);
		return factory.getFactory().create(config, control, monitor);
	}

	/**
	 * Dispose of all of the resource managers.
	 */
	private void disposeResourceManagers() {
		for (IResourceManager resourceManager : getResourceManagers()) {
			resourceManager.dispose();
		}
	}

	private void doRemoveResourceManager(IResourceManager rm) {
		synchronized (fResourceManagers) {
			fResourceManagers.remove(rm.getUniqueName());
		}
		fireRemoveResourceManager(rm);
		rm.dispose();
	}

	/**
	 * Fire a new resource manager event.
	 * 
	 * @param rm
	 */
	private void fireNewResourceManager(final IResourceManager rm) {
		IResourceManagerAddedEvent event = new ResourceManagerAddedEvent(this, rm);
		for (Object listener : fResourceManagerListeners.getListeners()) {
			((IResourceManagerListener) listener).handleEvent(event);
		}
	}

	/**
	 * Fire a remove resource manager event.
	 * 
	 * @param rm
	 */
	private void fireRemoveResourceManager(final IResourceManager rm) {
		IResourceManagerRemovedEvent event = new ResourceManagerRemovedEvent(this, rm);
		for (Object listener : fResourceManagerListeners.getListeners()) {
			((IResourceManagerListener) listener).handleEvent(event);
		}
	}

	private void getResourceManagerFactories() {
		if (fResourceManagerFactories == null) {
			fResourceManagerFactories = new HashMap<String, RMFactory>();
			fResourceManagerControlFactories = new HashMap<String, IResourceManagerControlFactory>();
			fResourceManagerMonitorFactories = new HashMap<String, IResourceManagerMonitorFactory>();

			IExtensionRegistry registry = Platform.getExtensionRegistry();
			IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);

			for (IExtension ext : extensionPoint.getExtensions()) {
				for (IConfigurationElement ce : ext.getConfigurationElements()) {
					if (ce.getName().equals(RM_NAME)) {
						RMFactory factory = new RMFactory();
						IConfigurationElement[] el = ce.getChildren();
						if (el.length == 2) {
							try {
								factory.setFactory((AbstractResourceManagerFactory) ce.createExecutableExtension(CLASS_ATTRIBUTE));
								factory.setControlId(addControlFactory(el[0]));
								factory.setMonitorId(addMonitorFactory(el[1]));
								factory.setId(ce.getAttribute(ID_ATTRIBUTE));
								fResourceManagerFactories.put(factory.getId(), factory);
							} catch (Exception e) {
								PTPCorePlugin.log(e);
							}
						}
					} else if (ce.getName().equals(RM_CONTROL_NAME)) {
						addControlFactory(ce);
					} else if (ce.getName().equals(RM_MONITOR_NAME)) {
						addMonitorFactory(ce);
					}
				}
			}
		}
	}

	/**
	 * Get the resource manager factory corresponding to the id
	 * 
	 * @param id
	 *            id of factory
	 * @return resource manager factory
	 */
	private RMFactory getResourceManagerFactory(String id) {
		getResourceManagerFactories();
		return fResourceManagerFactories.get(id);
	}

	/**
	 * Remove provider from a service configurations, or if the configuration has the same name as the resource manager, remove it
	 * from service manager.
	 * 
	 * @param provider
	 *            provider to remove
	 */
	private void removeProviderFromConfiguration(IServiceProvider provider) {
		Set<IServiceConfiguration> configs = fServiceManager.getConfigurations();

		for (IServiceConfiguration config : configs) {
			if (config.getServiceProvider(fLaunchService) == provider) {
				config.setServiceProvider(fLaunchService, null);
				break;
			}
		}
	}

	/**
	 * Start all resource managers.
	 * 
	 * @param rmsNeedStarting
	 * @throws CoreException
	 */
	private void startResourceManagers(IResourceManager[] rmsNeedStarting) throws CoreException {
		Job job = new RMStartupJob(rmsNeedStarting);
		job.schedule();
	}

	public static ModelManager getInstance() {
		return fInstance;
	}

}