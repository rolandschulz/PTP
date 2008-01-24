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

import java.io.File;
import java.util.ArrayList;

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
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.internal.core.elements.PUniverse;
import org.eclipse.ptp.internal.core.events.NewResourceManagerEvent;
import org.eclipse.ptp.internal.core.events.RemoveResourceManagerEvent;
import org.eclipse.ptp.internal.rmsystem.ResourceManagerPersistence;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;

public class ModelManager implements IModelManager {
	private class RMStartupJob extends Job {
		private IResourceManagerControl resourceManager;
		
		public RMStartupJob(IResourceManagerControl rm) {
			super("Starting Resource Manager: " + rm.getName());
			resourceManager = rm;
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			try {
	            try {
	            	monitor.beginTask("Starting resource manager", 100);
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
	
	private IResourceManagerFactory[] resourceManagerFactories;
	
	private final ListenerList resourceManagerListeners = new ListenerList();

	// protected IPMachine machine = null;
	protected IPJob processRoot = null;

	protected IPUniverseControl universe = new PUniverse();
	protected ILaunchConfiguration config = null;
	
	public ModelManager() {
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#addListener(org.eclipse.ptp.core.listeners.IModelManagerChildListener)
	 */
	public void addListener(IModelManagerChildListener listener) {
		resourceManagerListeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#addResourceManager(org.eclipse.ptp.core.elementcontrols.IResourceManagerControl)
	 */
	public synchronized void addResourceManager(IResourceManagerControl rm) {
		universe.addResourceManager(rm);
		fireNewResourceManager(rm);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#addResourceManagers(org.eclipse.ptp.core.elementcontrols.IResourceManagerControl[])
	 */
	public synchronized void addResourceManagers(IResourceManagerControl[] rms) {
		for (IResourceManagerControl rm : rms) {
			addResourceManager(rm);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#getResourceManagerFactories()
	 */
	public IResourceManagerFactory[] getResourceManagerFactories()
	{
		if (resourceManagerFactories != null) {
			return resourceManagerFactories;
		}
		
		final ArrayList<AbstractResourceManagerFactory> factoryList = new ArrayList<AbstractResourceManagerFactory>();
	
		IExtensionRegistry registry = Platform.getExtensionRegistry();
		IExtensionPoint extensionPoint = registry.getExtensionPoint("org.eclipse.ptp.core.resourcemanager");
		final IExtension[] extensions = extensionPoint.getExtensions();
		
		for (int iext = 0; iext < extensions.length; ++iext) {
			final IExtension ext = extensions[iext];
			
			final IConfigurationElement[] elements = ext.getConfigurationElements();
		
			for (int i=0; i< elements.length; i++)
			{
				IConfigurationElement ce = elements[i];
				try {
					AbstractResourceManagerFactory factory = (AbstractResourceManagerFactory) ce.createExecutableExtension("class");
					factory.setId(ce.getAttribute("id"));
					factoryList.add(factory);
				} catch (CoreException e) {
					PTPCorePlugin.log(e);
				}
			}
		}
		resourceManagerFactories =
			(IResourceManagerFactory[]) factoryList.toArray(
					new IResourceManagerFactory[factoryList.size()]);
	
		return resourceManagerFactories;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#getResourceManagerFactory(java.lang.String)
	 */
	public IResourceManagerFactory getResourceManagerFactory(String id)
	{
		IResourceManagerFactory[] factories = getResourceManagerFactories();
		for (int i=0; i<factories.length; i++)
		{
			if (factories[i].getId().equals(id)) return factories[i];
		}
		
		throw new RuntimeException("Unable to find resource manager factory");
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#getResourceManagerFromUniqueName(java.lang.String)
	 */
	public IResourceManager getResourceManagerFromUniqueName(String rmUniqueName) {
		IPUniverse universe = getUniverse();
		if (universe != null) {
			IResourceManager[] rms = getStartedResourceManagers(universe);
			
			for (IResourceManager rm : rms) {
				if (rm.getUniqueName().equals(rmUniqueName)) {
					return rm;
				}
			}
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#getStartedResourceManagers(org.eclipse.ptp.core.elements.IPUniverse)
	 */
	public IResourceManager[] getStartedResourceManagers(IPUniverse universe) {
		IResourceManager[] rms = universe.getResourceManagers();
		ArrayList<IResourceManager> startedRMs = 
			new ArrayList<IResourceManager>(rms.length);
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED) {
				startedRMs.add(rm);
			}
		}
		return startedRMs.toArray(new IResourceManager[startedRMs.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelPresentation#getUniverse()
	 */
	public IPUniverse getUniverse() {
		return universe;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent)
	 */
	public void handleEvent(IResourceManagerErrorEvent e) {
		// Ignore - handled by listener on RM		
	}

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#loadResourceManagers()
	 */
	public void loadResourceManagers() throws CoreException {
        ResourceManagerPersistence rmp = new ResourceManagerPersistence();
        rmp.loadResourceManagers(getResourceManagersFile(), getResourceManagerFactories());
        IResourceManagerControl[] resourceManagers = rmp.getResourceManagerControls();
        IResourceManagerControl[] rmsNeedStarting = rmp.getResourceManagerControlsNeedStarting();
        addResourceManagers(resourceManagers);
        startResourceManagers(rmsNeedStarting);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#removeListener(org.eclipse.ptp.core.listeners.IModelManagerChildListener)
	 */
	public void removeListener(IModelManagerChildListener listener) {
		resourceManagerListeners.remove(listener);
	}
	
	/* (non-Javadoc)
     * @see org.eclipse.ptp.core.IModelManager#removeResourceManager(org.eclipse.ptp.core.elementcontrols.IResourceManagerControl)
     */
    public synchronized void removeResourceManager(IResourceManagerControl rm) {
		universe.removeResourceManager(rm);
		fireRemoveResourceManager(rm);
	}

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#removeResourceManagers(org.eclipse.ptp.core.elementcontrols.IResourceManagerControl[])
	 */
	public synchronized void removeResourceManagers(IResourceManagerControl[] rms) {
		for (IResourceManagerControl rm : rms) {
			removeResourceManager(rm);
		}
	}

    /* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#saveResourceManagers()
	 */
	public void saveResourceManagers() {
		ResourceManagerPersistence.saveResourceManagers(getResourceManagersFile(),
				universe.getResourceManagerControls());
	}

	/* (non-Javadoc)
     * @see org.eclipse.ptp.core.IModelManager#shutdown()
     */
    public void shutdown() throws CoreException {
		saveResourceManagers();
		stopResourceManagers();
		shutdownResourceManagers();
		resourceManagerListeners.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#start()
	 */
	public void start() throws CoreException {
		loadResourceManagers();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#stopResourceManagers()
	 */
	public void stopResourceManagers() throws CoreException {
		IResourceManager[] resourceManagers = universe.getResourceManagers();
		for (int i = 0; i<resourceManagers.length; ++i) {
			resourceManagers[i].shutdown();
		}
	}

	/**
	 * Fire a new resource manager event.
	 * 
	 * @param rm
	 */
	private void fireNewResourceManager(final IResourceManager rm) {
		INewResourceManagerEvent event = 
			new NewResourceManagerEvent(this, rm);
		for (Object listener : resourceManagerListeners.getListeners()) {
			((IModelManagerChildListener)listener).handleEvent(event);
		}
	}
	
	/**
	 * Fire a remove resource manager event.
	 * 
	 * @param rm
	 */
	private void fireRemoveResourceManager(final IResourceManager rm) {
		IRemoveResourceManagerEvent event = 
			new RemoveResourceManagerEvent(this, rm);
		for (Object listener : resourceManagerListeners.getListeners()) {
			((IModelManagerChildListener)listener).handleEvent(event);
		}
	}

	/**
	 * Locate the resource managers configuration file.
	 * 
	 * @return
	 */
	private File getResourceManagersFile() {
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		return plugin.getStateLocation().append("resourceManagers.xml").toFile(); //$NON-NLS-1$
	}

	/**
	 * shuts down all of the resource managers.
	 */
	private synchronized void shutdownResourceManagers() {
		IResourceManagerControl[] resourceManagers = universe.getResourceManagerControls();
		for (int i = 0; i<resourceManagers.length; ++i) {
			resourceManagers[i].dispose();
		}
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

}