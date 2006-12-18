/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
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
/**
 * 
 */
package org.eclipse.ptp.rmsystem;

import java.util.HashMap;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ISafeRunnable;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.jface.util.SafeRunnable;
import org.eclipse.ptp.core.IModelListener;
import org.eclipse.ptp.core.INodeListener;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPQueue;
import org.eclipse.ptp.core.IProcessListener;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.events.IModelEvent;
import org.eclipse.ptp.core.events.INodeEvent;
import org.eclipse.ptp.core.events.IProcessEvent;
import org.eclipse.ptp.internal.core.PElement;
import org.eclipse.ptp.rmsystem.events.ResourceManagerContentsChangedEvent;
import org.eclipse.swt.widgets.Display;

/**
 * @author rsqrd
 * 
 */
public abstract class AbstractResourceManager extends PElement implements IResourceManager {
	
	private final ListenerList listeners = new ListenerList();

	private final IResourceManagerConfiguration config;

	private ResourceManagerStatus status;

	private final ListenerList modelListeners = new ListenerList();

	private final ListenerList nodeListeners = new ListenerList();

	private final ListenerList processListeners = new ListenerList();

	protected final Display display;

	protected final HashMap queues = new HashMap();

	protected final HashMap machines = new HashMap();
	
	public AbstractResourceManager(IPUniverseControl universe, IResourceManagerConfiguration config)
	{
		super(universe, config.getName(), config.getResourceManagerId(), P_RESOURCE_MANAGER);
		this.display = PTPCorePlugin.getDisplay();
		this.config = config;
		this.status = ResourceManagerStatus.INIT;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#addResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void addResourceManagerListener(IResourceManagerListener listener) {
		listeners.add(listener);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#disableEvents()
	 */
	public void disableEvents() throws CoreException {
		doDisableEvents();
		setStatus(ResourceManagerStatus.EVENTS_DISABLED, true);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#dispose()
	 */
	public void dispose() {
		modelListeners.clear();
		nodeListeners.clear();
		processListeners.clear();
		SafeRunnable.run(new SafeRunnable(){
			public void run() throws Exception {
				shutdown();
			}});
		doDispose();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#enableEvents()
	 */
	public void enableEvents() throws CoreException {
		doEnableEvents();
		setStatus(ResourceManagerStatus.EVENTS_ENABLED, true);
	}

	public synchronized IPJobControl findJobById(String job_id) {
		IPQueueControl[] queues = getQueueControls();
		for (int j = 0; j < queues.length; ++j) {
			IPJobControl job = queues[j].findJobById(job_id);
			if (job != null) {
				return job;
			}
		}
		return null;
	}

	public synchronized IPQueue findQueueById(String id) {
		IPQueueControl[] queues = getQueueControls();
		for (int j = 0; j < queues.length; ++j) {
			if (queues[j].getIDString().equals(id)) {
				return queues[j];
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.core.runtime.PlatformObject#getAdapter(java.lang.Class)
	 */
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		} else {
			return super.getAdapter(adapter);
		}
	}

	/**
	 * @return
	 */
	public IResourceManagerConfiguration getConfiguration() {
		return config;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getDescription()
	 */
	public String getDescription() {
		return config.getDescription();
	}

	public synchronized IPMachine getMachine(String ID) {
		return (IPMachine) machines.get(ID);
	}

	public synchronized IPMachineControl[] getMachineControls() {
		return (IPMachineControl[]) machines.values().toArray(new IPMachineControl[0]);
	}

	public IPMachine[] getMachines() {
		return getMachineControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getName()
	 */
	public String getName() {
		return config.getName();
	}

	public IPQueue getQueue(String id) {
		return (IPQueue) queues.get(id);
	}

	public synchronized IPQueueControl[] getQueueControls() {
		return (IPQueueControl[]) queues.values().toArray(new IPQueueControl[0]);
	}

	public IPQueue[] getQueues() {
		return getQueueControls();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getStatus()
	 */
	public ResourceManagerStatus getStatus() {
		return status;
	}

	public boolean hasChildren() {
		return getMachines().length > 0 || getQueues().length > 0;
	}

	public boolean isAllStop() {
		IPMachineControl[] machines = getMachineControls();
		for (int i = 0; i < machines.length; i++) {
			if (!machines[i].isAllStop())
				return false;
		}
		IPQueueControl[] queues = getQueueControls();
		for (int i = 0; i < queues.length; i++) {
			if (!queues[i].isAllStop())
				return false;
		}
		return true;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#removeResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public void removeResourceManagerListener(IResourceManagerListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#stop()
	 */
	public void shutdown() throws CoreException {
		if (status.equals(ResourceManagerStatus.STARTED)) {
			doShutdown();
			setStatus(ResourceManagerStatus.STOPPED, false);
			fireShutdown();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#start()
	 */
	public void startUp(IProgressMonitor monitor) throws CoreException {
		monitor.beginTask("Starting Resource Manager " + getName(), 10);
		if (!status.equals(ResourceManagerStatus.STARTED) &&
				!status.equals(ResourceManagerStatus.ERROR)) {
			if (monitor == null) {
				monitor = new NullProgressMonitor();
			}
			SubProgressMonitor subMonitor = new SubProgressMonitor(monitor, 10);
			try {
				doStartup(subMonitor);
			}
			finally {
				monitor.done();
			}
			setStatus(ResourceManagerStatus.STARTED, false);
			fireStarted();
		}
		else {
			monitor.done();
		}
	}

	private void fireContentsChanged(final ResourceManagerContentsChangedEvent event) {
		Object[] tmpListeners = listeners.getListeners();
			
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					listener.handleContentsChanged(event);
				}
			});
		}
	}

	protected synchronized void addMachine(String ID, IPMachineControl machine) {
		machines.put(ID, machine);
		fireMachinesChanged(new int[]{machine.getID()});
	}

	protected synchronized void addQueue(String ID, IPQueueControl queue) {
		queues.put(ID, queue);
		fireQueuesChanged(new int[]{queue.getID()});
	}

	/**
	 * 
	 */
	protected abstract void doDisableEvents();

	/**
	 * 
	 */
	protected abstract void doDispose();

	/**
	 * 
	 */
	protected abstract void doEnableEvents();

	/**
	 * @throws CoreException
	 */
	protected abstract void doShutdown() throws CoreException;

	/**
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void doStartup(IProgressMonitor monitor) throws CoreException;

	/**
	 * @param event
	 */
	protected void fireEvent(final IModelEvent event) {
		Object[] array = modelListeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final IModelListener l = (IModelListener) array[i];
			final SafeRunnable safeRunnable = new SafeRunnable() {
				public void run() {
					l.modelEvent(event);
				}
			};
			safeRunAsyncInUIThread(safeRunnable);
		}
	}

	/**
	 * @param event
	 */
	protected void fireEvent(final INodeEvent event) {
		Object[] array = nodeListeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final INodeListener l = (INodeListener) array[i];
			final SafeRunnable safeRunnable = new SafeRunnable() {
				public void run() {
					l.nodeEvent(event);
				}
			};
			safeRunAsyncInUIThread(safeRunnable);
		}
	}

	protected void fireEvent(final IProcessEvent event) {
		Object[] array = processListeners.getListeners();
		for (int i = 0; i < array.length; i++) {
			final IProcessListener l = (IProcessListener) array[i];
			final SafeRunnable safeRunnable = new SafeRunnable() {
				public void run() {
					l.processEvent(event);
				}
			};
			safeRunAsyncInUIThread(safeRunnable);
		}
	}

	protected void fireMachinesChanged(int[] ids) {
		try {
			ResourceManagerContentsChangedEvent event = 
				new ResourceManagerContentsChangedEvent(this,
					ResourceManagerContentsChangedEvent.MACHINE, ids);
			fireContentsChanged(event);
			
		} catch (CoreException e) {
			// Shouldn't happen
			e.printStackTrace();
		}
	}

	protected void fireQueuesChanged(int[] ids) {
		try {
			ResourceManagerContentsChangedEvent event = 
				new ResourceManagerContentsChangedEvent(this,
					ResourceManagerContentsChangedEvent.QUEUE, ids);
			fireContentsChanged(event);
			
		} catch (CoreException e) {
			// Shouldn't happen
			e.printStackTrace();
		}
	}

	protected void fireShutdown() {
		Object[] tmpListeners = listeners.getListeners();
		
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					listener.handleShutdown(AbstractResourceManager.this);
				}
			});
		}
	}

	protected void fireStarted() {
		Object[] tmpListeners = listeners.getListeners();
		
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					listener.handleStartup(AbstractResourceManager.this);
				}
			});
		}
	}

	protected void fireStatusChanged(final ResourceManagerStatus oldStatus) {
		Object[] tmpListeners = listeners.getListeners();
		
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			final IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			safeRunAsyncInUIThread(new SafeRunnable() {
				public void run() {
					listener.handleStatusChanged(oldStatus, AbstractResourceManager.this);
				}
			});
		}
	}

	protected IPMachineControl getMachineControl(String ID) {
		return (IPMachineControl) machines.get(ID);
	}

	protected synchronized IPProcess getProcess(String ID) {
		IPQueueControl[] theQueues = getQueueControls();
		for (int iq=0; iq<theQueues.length; ++iq) {
			IPJob[] jobs = theQueues[iq].getJobs();
			for (int i = 0; i < jobs.length; ++i) {
				IPJob job = jobs[i];
				IPProcess proc = job.findProcessByName(ID);
				if (proc != null)
					return proc;
			}
		}
		return null;
	}

	protected CoreException makeCoreException(String string) {
		IStatus status = new Status(Status.ERROR, PTPCorePlugin.getUniqueIdentifier(),
				Status.ERROR, string, null);
		return new CoreException(status);
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

	/**
	 * @param status
	 * @param fireEvent
	 */
	protected void setStatus(ResourceManagerStatus status, boolean fireEvent) {
		final ResourceManagerStatus oldStatus = this.status;
		this.status = status;
		if (fireEvent) {
			fireStatusChanged(oldStatus);
		}
	}

}
