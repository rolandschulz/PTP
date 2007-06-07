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
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.events.IModelManagerNewResourceManagerEvent;
import org.eclipse.ptp.core.events.IModelManagerRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerResourceManagerListener;
import org.eclipse.ptp.internal.core.elements.PUniverse;
import org.eclipse.ptp.internal.core.events.ModelManagerNewResourceManagerEvent;
import org.eclipse.ptp.internal.core.events.ModelManagerRemoveResourceManagerEvent;
import org.eclipse.ptp.internal.rmsystem.ResourceManagerPersistence;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;

public class ModelManager implements IModelManager {
	private final ListenerList resourceManagerListeners = new ListenerList();
	private IResourceManagerFactory[] resourceManagerFactories;
	protected ListenerList modelListeners = new ListenerList();
	protected ListenerList nodeListeners = new ListenerList();
	protected ListenerList processListeners = new ListenerList();
	// protected IPMachine machine = null;
	protected IPJob processRoot = null;
	protected IPUniverseControl universe = new PUniverse();
	protected ILaunchConfiguration config = null;

	public ModelManager() {
	}

	public void addListener(IModelManagerResourceManagerListener listener) {
		resourceManagerListeners.add(listener);
	}
	
	public synchronized void addResourceManager(IResourceManagerControl rm) {
		universe.addResourceManager(rm);
		fireNewResourceManager(rm);
	}
	
	public synchronized void addResourceManagers(IResourceManagerControl[] rms) {
		for (IResourceManagerControl rm : rms) {
			addResourceManager(rm);
		}
	}
	
	public ILaunchConfiguration getPTPConfiguration() {
		return config;
	}

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
	/**
	 * Find the resource manager factory corresponding to the supplied ID.
	 * @param id
	 * @return the requested resource manager factory
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

	public IPUniverse getUniverse() {
		return universe;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#loadResourceManagers()
	 */
	public void loadResourceManagers(IProgressMonitor monitor) throws CoreException {
	    if (monitor == null) {
	        monitor = new NullProgressMonitor();
	    }
	    monitor.beginTask("Loading Resource Managers", 110);
	    try {
	        ResourceManagerPersistence rmp = new ResourceManagerPersistence();
	        // Loads and, if necessary, starts saved resource managers.
	        rmp.loadResourceManagers(getResourceManagersFile(), getResourceManagerFactories(),
	                new SubProgressMonitor(monitor, 10));
	        IResourceManagerControl[] resourceManagers = rmp.getResourceManagerControls();
	        IResourceManagerControl[] rmsNeedStarting = rmp.getResourceManagerControlsNeedStarting();
	        addResourceManagers(resourceManagers);
	        startResourceManagers(rmsNeedStarting,
	                new SubProgressMonitor(monitor, 100));
	    }
	    finally {
	        monitor.done();
	    }
	}
	
	public void removeListener(IModelManagerResourceManagerListener listener) {
		resourceManagerListeners.remove(listener);
	}

    public synchronized void removeResourceManager(IResourceManagerControl rm) {
		universe.removeResourceManager(rm);
		fireRemoveResourceManager(rm);
	}

	public synchronized void removeResourceManagers(IResourceManagerControl[] rms) {
		for (IResourceManagerControl rm : rms) {
			removeResourceManager(rm);
		}
	}
	
	public void saveResourceManagers() {
		ResourceManagerPersistence.saveResourceManagers(getResourceManagersFile(),
				universe.getResourceManagerControls());
	}

	public void setPTPConfiguration(ILaunchConfiguration config) {
		this.config = config;
	}

    public void shutdown() throws CoreException {
		saveResourceManagers();
		stopResourceManagers();
		shutdownResourceManagers();

		resourceManagerListeners.clear();
		modelListeners.clear();
		nodeListeners.clear();
		processListeners.clear();
	}

    /**
	 * shuts down all of the resource managers.
	 */
	public synchronized void shutdownResourceManagers() {
		IResourceManagerControl[] resourceManagers = universe.getResourceManagerControls();
		for (int i = 0; i<resourceManagers.length; ++i) {
			resourceManagers[i].dispose();
		}
	}

	public void start(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Starting Model Manager", 10);
		try {
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			loadResourceManagers(subMonitor);
		}
		finally {
			monitor.done();
		}
	}

	/**
	 * stops all of the resource managers.
	 * 
	 * @throws CoreException
	 */
	public void stopResourceManagers() throws CoreException {
		IResourceManager[] resourceManagers = universe.getResourceManagers();
		for (int i = 0; i<resourceManagers.length; ++i) {
			resourceManagers[i].shutdown();
		}
	}

	private void fireNewResourceManager(final IResourceManager rm) {
		IModelManagerNewResourceManagerEvent event = 
			new ModelManagerNewResourceManagerEvent(this, rm);
		for (Object listener : resourceManagerListeners.getListeners()) {
			((IModelManagerResourceManagerListener)listener).handleEvent(event);
		}
	}

	private void fireRemoveResourceManager(final IResourceManager rm) {
		IModelManagerRemoveResourceManagerEvent event = 
			new ModelManagerRemoveResourceManagerEvent(this, rm);
		for (Object listener : resourceManagerListeners.getListeners()) {
			((IModelManagerResourceManagerListener)listener).handleEvent(event);
		}
	}


	private File getResourceManagersFile() {
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		return plugin.getStateLocation().append("resourceManagers.xml").toFile();
	}

	private void startResourceManagers(IResourceManagerControl[] rmsNeedStarting,
	        IProgressMonitor monitor) throws CoreException {
	    monitor.beginTask("Starting Resource Managers",
	            rmsNeedStarting.length);
	    try {
	        SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 1);
	        for (IResourceManagerControl rm : rmsNeedStarting) {
	            rm.startUp(subMonitor);
	        }
	    }
	    finally {
	        monitor.done();
	    }
    }

}