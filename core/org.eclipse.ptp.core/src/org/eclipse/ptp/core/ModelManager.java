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
import org.eclipse.core.runtime.SubProgressMonitor;
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
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
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

	private static String ID_ATTRIBUTE = "id"; //$NON-NLS-1$
	private static String CLASS_ATTRIBUTE = "class"; //$NON-NLS-1$
	private static String EXTENSION_POINT = "org.eclipse.ptp.core.resourceManagers"; //$NON-NLS-1$

	private class RMStartupJob extends Job {
		private final IResourceManager resourceManager;

		public RMStartupJob(IResourceManager rm) {
			super(Messages.ModelManager_0 + rm.getName());
			resourceManager = rm;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				try {
					monitor.beginTask(Messages.ModelManager_1, 100);
					resourceManager.start(new SubProgressMonitor(monitor, 100));
				} catch (CoreException e) {
					return e.getStatus();
				}
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				return Status.OK_STATUS;
			} finally {
				monitor.done();
			}
		}

	}

	private final IServiceModelEventListener fServiceEventListener = new IServiceModelEventListener() {

		public void handleEvent(IServiceModelEvent event) {
			switch (event.getType()) {
			case IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED: {
				IServiceProvider provider = ((IServiceConfiguration) event.getSource()).getServiceProvider(fLaunchService);
				if (provider != null && provider instanceof IResourceManagerConfiguration) {
					IResourceManager rm = getResourceManagerFromUniqueName(((IResourceManagerConfiguration) provider)
							.getUniqueName());
					if (rm != null) {
						doRemoveResourceManager(rm);
					}
				}
				break;
			}

			case IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED: {
				IServiceConfiguration config = (IServiceConfiguration) event.getSource();
				IServiceProvider oldProvider = event.getOldProvider();
				if (oldProvider != null) {
					if (oldProvider instanceof IResourceManagerConfiguration) {
						IServiceProvider newProvider = config.getServiceProvider(fLaunchService);
						if (newProvider != null && newProvider instanceof IResourceManagerConfiguration) {
							IResourceManager rm = getResourceManagerFromUniqueName(((IResourceManagerConfiguration) oldProvider)
									.getUniqueName());
							if (rm != null) {
								rm.setConfiguration((IResourceManagerConfiguration) newProvider);
							}
						}
					}
				} else {
					IServiceProvider newProvider = config.getServiceProvider(fLaunchService);
					if (newProvider != null && newProvider instanceof IResourceManagerConfiguration) {
						IResourceManager rm = getResourceManagerFromUniqueName(((IResourceManagerConfiguration) newProvider)
								.getUniqueName());
						if (rm == null) {
							IResourceManagerFactory factory = getResourceManagerFactory(((IResourceManagerConfiguration) newProvider)
									.getResourceManagerId());
							if (factory != null) {
								addResourceManager(factory.create((IResourceManagerConfiguration) newProvider));
							}
						}
					}
				}
				break;
			}

			case IServiceModelEvent.SERVICE_PROVIDER_CHANGED: {
				IServiceProvider provider = (IServiceProvider) event.getSource();
				if (provider != null && provider instanceof IResourceManagerConfiguration) {
					IResourceManager rm = getResourceManagerFromUniqueName(((IResourceManagerConfiguration) provider)
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

	private final ListenerList fResourceManagerListeners = new ListenerList();

	protected final IServiceModelManager fServiceManager = ServiceModelManager.getInstance();
	protected final Map<String, IResourceManager> fResourceManagers = new HashMap<String, IResourceManager>();
	protected final IService fLaunchService = fServiceManager.getService(IServiceConstants.LAUNCH_SERVICE);
	protected final IPUniverse fUniverse = new PUniverse();

	protected Map<String, IResourceManagerFactory> fResourceManagerFactories = null;

	public ModelManager() {
		fServiceManager.addEventListener(fServiceEventListener, IServiceModelEvent.SERVICE_CONFIGURATION_ADDED
				| IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED | IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED
				| IServiceModelEvent.SERVICE_PROVIDER_CHANGED);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#addListener(org.eclipse.ptp.core.listeners
	 * .IResourceManagerListener)
	 */
	public void addListener(IResourceManagerListener listener) {
		fResourceManagerListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#addResourceManager(org.eclipse.ptp
	 * .rmsystem.IResourceManager)
	 */
	public void addResourceManager(IResourceManager rm) {
		synchronized (fResourceManagers) {
			fResourceManagers.put(rm.getUniqueName(), rm);
		}
		fireNewResourceManager(rm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#addResourceManagers(org.eclipse.ptp
	 * .rmsystem.IResourceManager[])
	 */
	public void addResourceManagers(IResourceManager[] rms) {
		for (IResourceManager rm : rms) {
			addResourceManager(rm);
		}
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#getResourceManagerFromUniqueName(java
	 * .lang.String)
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
	 * @see
	 * org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#handleEvent
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
		 * Need to force service model to load so that the resource managers are
		 * created.
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
	 * @see
	 * org.eclipse.ptp.core.IModelManager#removeListener(org.eclipse.ptp.core
	 * .listeners.IResourceManagerListener)
	 */
	public void removeListener(IResourceManagerListener listener) {
		fResourceManagerListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#removeResourceManager(org.eclipse.
	 * ptp.rmsystem.IResourceManager)
	 */
	public void removeResourceManager(IResourceManager rm) {
		IResourceManagerConfiguration rmConf = rm.getConfiguration();
		if (rmConf instanceof IServiceProvider) {
			removeProviderFromConfiguration((IServiceProvider) rmConf);
		}
		doRemoveResourceManager(rm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#removeResourceManagers(org.eclipse
	 * .ptp.rmsystem.IResourceManagerConfiguration[])
	 */
	public void removeResourceManagers(IResourceManagerConfiguration[] rms) {
		for (IResourceManagerConfiguration rmConf : rms) {
			IResourceManager rm = getResourceManagerFromUniqueName(rmConf.getUniqueName());
			if (rm != null) {
				removeResourceManager(rm);
			}
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

	/**
	 * Remove provider from a service configurations
	 * 
	 * @param provider
	 *            provider to remove
	 */
	private void removeProviderFromConfiguration(IServiceProvider provider) {
		Set<IServiceConfiguration> configs = fServiceManager.getConfigurations();

		for (IServiceConfiguration config : configs) {
			if (provider == config.getServiceProvider(fLaunchService)) {
				config.setServiceProvider(fLaunchService, null);
				break;
			}
		}
	}

	/**
	 * Dispose of all of the resource managers.
	 */
	private void disposeResourceManagers() {
		for (IResourceManager resourceManager : getResourceManagers()) {
			resourceManager.dispose();
		}
	}

	/**
	 * Start all resource managers.
	 * 
	 * @param rmsNeedStarting
	 * @throws CoreException
	 */
	private void startResourceManagers(IResourceManager[] rmsNeedStarting) throws CoreException {
		for (final IResourceManager rm : rmsNeedStarting) {
			Job job = new RMStartupJob(rm);
			job.schedule();
		}
	}

	private Map<String, IResourceManagerFactory> getResourceManagerFactories() {
		if (fResourceManagerFactories != null) {
			return fResourceManagerFactories;
		}

		fResourceManagerFactories = new HashMap<String, IResourceManagerFactory>();

		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint(EXTENSION_POINT);
		final IExtension[] extensions = extensionPoint.getExtensions();

		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];

			final IConfigurationElement[] elements = ext.getConfigurationElements();

			for (int i = 0; i < elements.length; i++) {
				IConfigurationElement ce = elements[i];
				try {
					IResourceManagerFactory factory = (IResourceManagerFactory) ce.createExecutableExtension(CLASS_ATTRIBUTE);
					String id = ce.getAttribute(ID_ATTRIBUTE);
					// factory.setId(id);
					fResourceManagerFactories.put(id, factory);
				} catch (CoreException e) {
					PTPCorePlugin.log(e);
				}
			}
		}

		return fResourceManagerFactories;
	}

	private IResourceManagerFactory getResourceManagerFactory(String id) {
		getResourceManagerFactories();
		return fResourceManagerFactories.get(id);
	}

}