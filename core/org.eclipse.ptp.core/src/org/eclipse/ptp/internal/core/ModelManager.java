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
package org.eclipse.ptp.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IServiceConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.internal.core.elements.PUniverse;
import org.eclipse.ptp.internal.core.events.ChangedResourceManagerEvent;
import org.eclipse.ptp.internal.core.events.NewResourceManagerEvent;
import org.eclipse.ptp.internal.core.events.RemoveResourceManagerEvent;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceModelEvent;
import org.eclipse.ptp.services.core.IServiceModelEventListener;
import org.eclipse.ptp.services.core.IServiceModelManager;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ServiceModelManager;

public class ModelManager implements IModelManager {
	private class RMStartupJob extends Job {
		private final IResourceManagerControl resourceManager;

		public RMStartupJob(IResourceManagerControl rm) {
			super(Messages.ModelManager_0 + rm.getName());
			resourceManager = rm;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
				try {
					monitor.beginTask(Messages.ModelManager_1, 100);
					resourceManager.startUp(new SubProgressMonitor(monitor, 100));
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
					IResourceManagerControl rm = (IResourceManagerControl) getResourceManagerFromUniqueName(((IResourceManagerConfiguration) provider)
							.getUniqueName());
					if (rm != null) {
						removeResourceManager(rm);
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
							IResourceManagerControl rm = (IResourceManagerControl) getResourceManagerFromUniqueName(((IResourceManagerConfiguration) oldProvider)
									.getUniqueName());
							if (rm != null) {
								rm.setConfiguration((IResourceManagerConfiguration) newProvider);
							}
						}
					}
				} else {
					IServiceProvider newProvider = config.getServiceProvider(fLaunchService);
					if (newProvider != null && newProvider instanceof IResourceManagerConfiguration) {
						IResourceManagerControl rm = (IResourceManagerControl) getResourceManagerFromUniqueName(((IResourceManagerConfiguration) newProvider)
								.getUniqueName());
						if (rm == null) {
							addResourceManager(((IResourceManagerConfiguration) newProvider).createResourceManager());
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
						updateResourceManager(rm);
					}
				}
				break;
			}
			}
		}
	};

	private final ListenerList resourceManagerListeners = new ListenerList();
	protected final IServiceModelManager fServiceManager = ServiceModelManager.getInstance();
	protected IService fLaunchService = fServiceManager.getService(IServiceConstants.LAUNCH_SERVICE);

	protected IPUniverseControl universe = new PUniverse();

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
	 * .IModelManagerChildListener)
	 */
	public void addListener(IModelManagerChildListener listener) {
		resourceManagerListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#addResourceManager(org.eclipse.ptp
	 * .core.elementcontrols.IResourceManagerControl)
	 */
	public synchronized void addResourceManager(IResourceManagerControl rm) {
		universe.addResourceManager(rm);
		fireNewResourceManager(rm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#addResourceManagers(org.eclipse.ptp
	 * .core.elementcontrols.IResourceManagerControl[])
	 */
	public synchronized void addResourceManagers(IResourceManagerControl[] rms) {
		for (IResourceManagerControl rm : rms) {
			addResourceManager(rm);
		}
	}

	/**
	 * Fire a changed resource manager event.
	 * 
	 * @param rms
	 *            collection of resource managers
	 */
	private void fireChangedResourceManager(final Collection<IResourceManager> rms) {
		IChangedResourceManagerEvent event = new ChangedResourceManagerEvent(this, rms);
		for (Object listener : resourceManagerListeners.getListeners()) {
			((IModelManagerChildListener) listener).handleEvent(event);
		}
	}

	/**
	 * Fire a new resource manager event.
	 * 
	 * @param rm
	 */
	private void fireNewResourceManager(final IResourceManager rm) {
		INewResourceManagerEvent event = new NewResourceManagerEvent(this, rm);
		for (Object listener : resourceManagerListeners.getListeners()) {
			((IModelManagerChildListener) listener).handleEvent(event);
		}
	}

	/**
	 * Fire a remove resource manager event.
	 * 
	 * @param rm
	 */
	private void fireRemoveResourceManager(final IResourceManager rm) {
		IRemoveResourceManagerEvent event = new RemoveResourceManagerEvent(this, rm);
		for (Object listener : resourceManagerListeners.getListeners()) {
			((IModelManagerChildListener) listener).handleEvent(event);
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
		IPUniverse universe = getUniverse();
		if (universe != null) {
			IResourceManager[] rms = universe.getResourceManagers();

			for (IResourceManager rm : rms) {
				if (rm.getUniqueName().equals(rmUniqueName)) {
					return rm;
				}
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#getStartedResourceManagers(org.eclipse
	 * .ptp.core.elements.IPUniverse)
	 */
	public IResourceManager[] getStartedResourceManagers(IPUniverse universe) {
		IResourceManager[] rms = universe.getResourceManagers();
		ArrayList<IResourceManager> startedRMs = new ArrayList<IResourceManager>(rms.length);
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED) {
				startedRMs.add(rm);
			}
		}
		return startedRMs.toArray(new IResourceManager[startedRMs.size()]);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelPresentation#getUniverse()
	 */
	public IPUniverse getUniverse() {
		return universe;
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
		Set<IResourceManagerControl> rmsNeedStarting = new HashSet<IResourceManagerControl>();

		/*
		 * Need to force service model to load so that the resource managers are
		 * created.
		 */
		fServiceManager.getActiveConfiguration();

		for (IResourceManager rm : getUniverse().getResourceManagers()) {
			IResourceManagerControl rmControl = (IResourceManagerControl) rm;
			if (rmControl.getConfiguration().getAutoStart()) {
				rmsNeedStarting.add(rmControl);
			}
		}

		if (PTPCorePlugin.getDefault().getPluginPreferences().getBoolean(PreferenceConstants.PREFS_AUTO_START_RMS)) {
			startResourceManagers(rmsNeedStarting.toArray(new IResourceManagerControl[0]));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#removeListener(org.eclipse.ptp.core
	 * .listeners.IModelManagerChildListener)
	 */
	public void removeListener(IModelManagerChildListener listener) {
		resourceManagerListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#removeResourceManager(org.eclipse.
	 * ptp.core.elementcontrols.IResourceManagerControl)
	 */
	public synchronized void removeResourceManager(IResourceManagerControl rm) {
		universe.removeResourceManager(rm);
		fireRemoveResourceManager(rm);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#removeResourceManagers(org.eclipse
	 * .ptp.core.elementcontrols.IResourceManagerControl[])
	 */
	public synchronized void removeResourceManagers(IResourceManagerControl[] rms) {
		for (IResourceManagerControl rm : rms) {
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
		shutdownResourceManagers();
		resourceManagerListeners.clear();
	}

	/**
	 * shuts down all of the resource managers.
	 */
	private synchronized void shutdownResourceManagers() {
		IResourceManagerControl[] resourceManagers = universe.getResourceManagerControls();
		for (int i = 0; i < resourceManagers.length; ++i) {
			resourceManagers[i].dispose();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#start()
	 */
	public void start() throws CoreException {
		loadResourceManagers();
	}

	/**
	 * Start all resource managers.
	 * 
	 * @param rmsNeedStarting
	 * @throws CoreException
	 */
	private void startResourceManagers(IResourceManagerControl[] rmsNeedStarting) throws CoreException {
		for (final IResourceManagerControl rm : rmsNeedStarting) {
			Job job = new RMStartupJob(rm);
			job.schedule();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.IModelManager#stopResourceManagers()
	 */
	public void stopResourceManagers() throws CoreException {
		IResourceManager[] resourceManagers = universe.getResourceManagers();
		for (int i = 0; i < resourceManagers.length; ++i) {
			resourceManagers[i].shutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.core.IModelManager#updateResourceManager(org.eclipse.
	 * ptp.core.elements.IResourceManager)
	 */
	public void updateResourceManager(IResourceManager rm) {
		fireChangedResourceManager(Arrays.asList(rm));
	}

}