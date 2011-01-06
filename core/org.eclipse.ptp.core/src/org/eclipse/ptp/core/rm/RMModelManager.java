/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.core.rm;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.IServiceConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelEvent;
import org.eclipse.ptp.services.core.IServiceModelEventListener;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * @since 5.0
 */
public class RMModelManager {
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
					resourceManager.startup(new SubProgressMonitor(monitor, 100));
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
							addResourceManager((IResourceManager) ((IResourceManagerConfiguration) newProvider)
									.getAdapter(IResourceManager.class));
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
						fireChangedResourceManager(rm);
					}
				}
				break;
			}
			}
		}
	};

	private final ListenerList fResourceManagerListeners = new ListenerList();
	private final List<IResourceManager> fResourceManagers = new ArrayList<IResourceManager>();
	protected final IServiceModelManager fServiceManager = ServiceModelManager.getInstance();
	protected IService fLaunchService = fServiceManager.getService(IServiceConstants.LAUNCH_SERVICE);

	public RMModelManager() {
		fServiceManager.addEventListener(fServiceEventListener, IServiceModelEvent.SERVICE_CONFIGURATION_ADDED
				| IServiceModelEvent.SERVICE_CONFIGURATION_REMOVED | IServiceModelEvent.SERVICE_CONFIGURATION_CHANGED
				| IServiceModelEvent.SERVICE_PROVIDER_CHANGED);
	}

	public void addListener(IRMModelListener listener) {
		fResourceManagerListeners.add(listener);
	}

	public void addResourceManager(IResourceManager rm) {
		synchronized (fResourceManagers) {
			fResourceManagers.add(rm);
		}
		fireNewResourceManager(rm);
	}

	public IResourceManager getResourceManagerFromUniqueName(String rmUniqueName) {
		for (IResourceManager rm : getResourceManagers()) {
			if (rm.getUniqueName().equals(rmUniqueName)) {
				return rm;
			}
		}
		return null;
	}

	public IResourceManager[] getStartedResourceManagers() {
		ArrayList<IResourceManager> startedRMs = new ArrayList<IResourceManager>();
		for (IResourceManager rm : getResourceManagers()) {
			if (rm.getSessionStatus() == IResourceManager.SessionStatus.STARTED) {
				startedRMs.add(rm);
			}
		}
		return startedRMs.toArray(new IResourceManager[startedRMs.size()]);
	}

	public void loadResourceManagers() throws CoreException {
		Set<IResourceManager> rmsNeedStarting = new HashSet<IResourceManager>();

		/*
		 * Need to force service model to load so that the resource managers are
		 * created.
		 */
		fServiceManager.getActiveConfiguration();

		for (IResourceManager rm : getResourceManagers()) {
			IResourceManager rmControl = rm;
			if (rmControl.getConfiguration().getAutoStart()) {
				rmsNeedStarting.add(rmControl);
			}
		}

		if (Preferences.getBoolean(PTPCorePlugin.getUniqueIdentifier(), PreferenceConstants.PREFS_AUTO_START_RMS)) {
			startResourceManagers(rmsNeedStarting.toArray(new IResourceManager[0]));
		}
	}

	public void removeListener(IRMModelListener listener) {
		fResourceManagerListeners.remove(listener);
	}

	public void removeResourceManager(IResourceManager rm) {
		IResourceManagerConfiguration rmConf = rm.getConfiguration();
		if (rmConf instanceof IServiceProvider) {
			removeProviderFromConfiguration((IServiceProvider) rmConf);
		}
		doRemoveResourceManager(rm);
	}

	public void shutdown() throws CoreException {
		shutdownResourceManagers();
		fResourceManagerListeners.clear();
	}

	public void start() throws CoreException {
		loadResourceManagers();
	}

	private void doRemoveResourceManager(IResourceManager rm) {
		synchronized (fResourceManagers) {
			fResourceManagers.remove(rm);
		}
		fireRemoveResourceManager(rm);
	}

	/**
	 * Fire a changed resource manager event.
	 * 
	 * @param rms
	 *            collection of resource managers
	 */
	private void fireChangedResourceManager(final IResourceManager rm) {
		for (Object listener : fResourceManagerListeners.getListeners()) {
			((IRMModelListener) listener).handleResourceManagerChanged(rm);
		}
	}

	/**
	 * Fire a new resource manager event.
	 * 
	 * @param rm
	 */
	private void fireNewResourceManager(final IResourceManager rm) {
		for (Object listener : fResourceManagerListeners.getListeners()) {
			((IRMModelListener) listener).handleResourceManagerAdded(rm);
		}
	}

	/**
	 * Fire a remove resource manager event.
	 * 
	 * @param rm
	 */
	private void fireRemoveResourceManager(final IResourceManager rm) {
		for (Object listener : fResourceManagerListeners.getListeners()) {
			((IRMModelListener) listener).handleResourceManagerRemoved(rm);
		}
	}

	private synchronized IResourceManager[] getResourceManagers() {
		return fResourceManagers.toArray(new IResourceManager[0]);
	}

	/**
	 * Remove provider from all service configurations
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
	 * shuts down all of the resource managers.
	 */
	private void shutdownResourceManagers() throws CoreException {
		for (IResourceManager rm : getResourceManagers()) {
			rm.shutdown();
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
}