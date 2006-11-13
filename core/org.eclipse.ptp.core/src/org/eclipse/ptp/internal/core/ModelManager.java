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
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.core.IModelListener;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.INodeListener;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.IProcessListener;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.events.IModelEvent;
import org.eclipse.ptp.core.events.INodeEvent;
import org.eclipse.ptp.core.events.IProcessEvent;
import org.eclipse.ptp.internal.rmsystem.ResourceManagerPersistence;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerChangedListener;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerListener;
import org.eclipse.ptp.rmsystem.ResourceManagerStatus;
import org.eclipse.ptp.rmsystem.events.IResourceManagerAddedRemovedEvent;
import org.eclipse.ptp.rmsystem.events.IResourceManagerContentsChangedEvent;
import org.eclipse.ptp.rmsystem.events.ResourceManagerAddedRemovedEvent;
import org.eclipse.swt.widgets.Display;

public class ModelManager implements IModelManager, IResourceManagerChangedListener,
IResourceManagerListener {
	private final ListenerList resourceManagerListeners = new ListenerList();
	private IResourceManagerFactory[] resourceManagerFactories;
	private final IModelListener rmModelListener;
	private final INodeListener rmNodeListener;

	private final IProcessListener rmProcessListener;
	private final Display display;
	protected ListenerList modelListeners = new ListenerList();
	protected ListenerList nodeListeners = new ListenerList();
	protected ListenerList processListeners = new ListenerList();
	// protected IPMachine machine = null;
	protected IPJob processRoot = null;
	protected IPUniverseControl universe = new PUniverse();
	protected ILaunchConfiguration config = null;

	public ModelManager() {

		this.display = PTPCorePlugin.getDisplay();

		// Forward resource manager events to the ModelManager's listeners
		rmModelListener = new IModelListener(){
			public void modelEvent(IModelEvent event) {
				fireEvent(event);
			}
		};

		// Forward resource manager events to the ModelManager's listeners
		rmNodeListener = new INodeListener(){
			public void nodeEvent(INodeEvent event) {
				fireEvent(event);
			}
		};

		// Forward resource manager events to the ModelManager's listeners
		rmProcessListener = new IProcessListener(){
			public void processEvent(IProcessEvent event) {
				fireEvent(event);
			}
		};
	}

	public void abortJob(String jobName) throws CoreException {
		IResourceManager[] resourceManagers = universe.getResourceManagers();
		for (int i=0; i< resourceManagers.length; ++i) {
			boolean found = resourceManagers[i].abortJob(jobName);
			if (found) {
				return;
			}
		}
	}
	
	public void addModelListener(IModelListener listener) {
		modelListeners.add(listener);
	}
	
	public void addNodeListener(INodeListener listener) {
		nodeListeners.add(listener);
	}
	public void addProcessListener(IProcessListener listener) {
		processListeners.add(listener);
	}
	
	public void addResourceManager(IResourceManager addedManager) {
		// begin forwarding of events to the ModelManager
		addRMListeners(addedManager);
		universe.addResourceManager(addedManager);
		IResourceManagerAddedRemovedEvent event = new ResourceManagerAddedRemovedEvent(this,
				addedManager, IResourceManagerAddedRemovedEvent.ADDED);
		fireResourceManagersAddedRemoved(event);
	}
	
	public void addResourceManagerChangedListener(IResourceManagerChangedListener listener) {
		resourceManagerListeners.add(listener);
	}
	
	public void addResourceManagers(IResourceManager[] addedManagers) {
		// begin forwarding of events to the ModelManager
		for (int i=0; i<addedManagers.length; ++i) {
			addRMListeners(addedManagers[i]);
		}
		universe.addResourceManagers(addedManagers);
		IResourceManagerAddedRemovedEvent event = new ResourceManagerAddedRemovedEvent(this,
				addedManagers, IResourceManagerAddedRemovedEvent.ADDED);
		fireResourceManagersAddedRemoved(event);
	}
	
	public ILaunchConfiguration getPTPConfiguration() {
		return config;
	}

	public IResourceManagerFactory[] getResourceManagerFactories()
	{
		if (resourceManagerFactories != null) {
			return resourceManagerFactories;
		}
		
		System.out.println("In getResourceManagerFactories");
	
		final ArrayList factoryList = new ArrayList();
	
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
					System.out.println("retrieved factory: " + factory.getName() + ", " + factory.getId());
				} catch (CoreException e) {
					PTPCorePlugin.log(e);
				}
			}
		}
		resourceManagerFactories =
			(IResourceManagerFactory[]) factoryList.toArray(
					new IResourceManagerFactory[factoryList.size()]);
	
		System.out.println("leaving getResourceManagerFactories");
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
	public void handleContentsChanged(IResourceManagerContentsChangedEvent event) {
		// TODO Auto-generated method stub
		
	}

	public void handleResourceManagersAddedRemoved(IResourceManagerAddedRemovedEvent event) {
		if (event.getType() == IResourceManagerAddedRemovedEvent.ADDED) {
			IResourceManager[] rms = event.getResourceManagers();
			for (int i=0; i < rms.length; ++i) {
				rms[i].addResourceManagerListener(this);
			}
		}
		else if (event.getType() == IResourceManagerAddedRemovedEvent.REMOVED) {
			IResourceManager[] rms = event.getResourceManagers();
			for (int i=0; i < rms.length; ++i) {
				rms[i].removeResourceManagerListener(this);
			}
		}
	}

	public void handleStarted(IResourceManager resourceManager) {
		// TODO Auto-generated method stub
		
	}
	public void handleStatusChanged(ResourceManagerStatus oldStatus, IResourceManager manager) {
		// TODO Auto-generated method stub
		
	}
	public void handleStopped(IResourceManager resourceManager) {
		// TODO Auto-generated method stub
		
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IModelManager#loadResourceManagers()
	 */
	public void loadResourceManagers(IProgressMonitor monitor) throws CoreException {
		ResourceManagerPersistence rmp = new ResourceManagerPersistence();
		// Loads and, if necessary, starts saved resource managers.
		rmp.loadResourceManagers(getResourceManagersFile(), getResourceManagerFactories(),
				monitor);
		IResourceManager[] resourceManagers = rmp.getResourceManagers();
		addResourceManagers(resourceManagers);
	}
	
	public synchronized void refreshRuntimeSystems(IProgressMonitor monitor, boolean force) throws CoreException {
		IResourceManager[] rms = universe.getResourceManagers();
		for (int i=0; i < rms.length; ++i) {
			rms[i].refreshRuntimeSystems(monitor, force);
		}
	}
	public void removeModelListener(IModelListener listener) {
		modelListeners.remove(listener);
	}
	
	public void removeNodeListener(INodeListener listener) {
		nodeListeners.remove(listener);
	}	
	
	public void removeProcessListener(IProcessListener listener) {
		processListeners.remove(listener);
	}
	
	public void removeResourceManager(IResourceManager removedManager) {
		universe.removeResourceManager(removedManager);
		// discontinue forwarding of events to the ModelManager
		removeRMListeners(removedManager);
	}
	
	public void removeResourceManagerChangedListener(IResourceManagerChangedListener listener) {
		resourceManagerListeners.remove(listener);
	}
	
	public void removeResourceManagers(IResourceManager[] removedRMs) {
		universe.removeResourceManagers(removedRMs);
		IResourceManagerAddedRemovedEvent event = new ResourceManagerAddedRemovedEvent(this,
				removedRMs,	IResourceManagerAddedRemovedEvent.REMOVED);
		fireResourceManagersAddedRemoved(event);
		// discontinue forwarding of events to the ModelManager
		for (int i=0; i<removedRMs.length; ++i) {
			removeRMListeners(removedRMs[i]);
		}
	}
	
	public void saveResourceManagers() {
		ResourceManagerPersistence.saveResourceManagers(getResourceManagersFile(),
				universe.getResourceManagers());
	}
	
	public void setPTPConfiguration(ILaunchConfiguration config) {
		this.config = config;
	}
	
	public void shutdown() throws CoreException {
		saveResourceManagers();
		stopResourceManagers();
		shutdownResourceManagers();

		// discontinue forwarding of events to the ModelManager
		IResourceManager[] resourceManagers = universe.getResourceManagers();
		for (int i = 0; i<resourceManagers.length; ++i) {
			removeRMListeners(resourceManagers[i]);
		}
		resourceManagerListeners.clear();
		modelListeners.clear();
		nodeListeners.clear();
		processListeners.clear();
	}
	
	/**
	 * shuts down all of the resource managers.
	 */
	public void shutdownResourceManagers() {
		IResourceManager[] resourceManagers = universe.getResourceManagers();
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
			resourceManagers[i].stop();
		}
	}
	
	/**
	 * Forward resource manager events to the ModelManager's listeners
	 * @param manager
	 */
	private void addRMListeners(IResourceManager manager) {
		manager.addModelListener(rmModelListener);
		manager.addNodeListener(rmNodeListener);
		manager.addProcessListener(rmProcessListener);
	}
	
	private void fireEvent(final IModelEvent event) {
        Object[] array = modelListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final IModelListener l = (IModelListener) array[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.modelEvent(event);
                }
            });
        }
	}
	
	private void fireEvent(final INodeEvent event) {
        Object[] array = nodeListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final INodeListener l = (INodeListener) array[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.nodeEvent(event);
                }
            });
        }
	}
	
	private void fireEvent(final IProcessEvent event) {
        Object[] array = processListeners.getListeners();
        for (int i = 0; i < array.length; i++) {
            final IProcessListener l = (IProcessListener) array[i];
            SafeRunnable.run(new SafeRunnable() {
                public void run() {
                    l.processEvent(event);
                }
            });
        }
	}

	private void fireResourceManagersAddedRemoved(final IResourceManagerAddedRemovedEvent event) {
		final Object[] tmpListeners = resourceManagerListeners.getListeners();
		for (int i=0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerChangedListener listener =
				(IResourceManagerChangedListener) tmpListeners[i]; 
			safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					listener.handleResourceManagersAddedRemoved(event);
				}
			});
		}
	}
	
	private File getResourceManagersFile() {
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		return plugin.getStateLocation().append("resourceManagers.xml").toFile();
	}
	
	/**
	 * Forward resource manager events to the ModelManager's listeners
	 * @param manager
	 */
	private void removeRMListeners(IResourceManager manager) {
		manager.removeModelListener(rmModelListener);
		manager.removeNodeListener(rmNodeListener);
		manager.removeProcessListener(rmProcessListener);
	}

	/**
	 * Makes sure that the safeRunnable is ran in the UI thread. 
	 * @param safeRunnable
	 */
	protected void safeRunAsyncInUIThread(final ISafeRunnable safeRunnable) {
		if (display.isDisposed()) {
			try {
				safeRunnable.run();
			} catch (Exception e) {
				PTPCorePlugin.log(e);
			}
		}
		else {
			display.asyncExec(new Runnable() {
				public void run() {
					SafeRunnable.run(safeRunnable);
				}
			});
		}
	}

}