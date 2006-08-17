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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.core.runtime.PlatformObject;
import org.eclipse.ptp.core.IModelPresentation;
import org.eclipse.ptp.rtsystem.IControlSystem;

/**
 * @author rsqrd
 * 
 */
public abstract class AbstractResourceManager extends PlatformObject implements IResourceManager {
	
	private final ListenerList listeners = new ListenerList();

	private final IResourceManagerConfiguration config;

	private ResourceManagerStatus status;
	
	public AbstractResourceManager(IResourceManagerConfiguration config)
	{
		this.config = config;
		this.status = ResourceManagerStatus.INIT;
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#getControlSystem()
	 */
	public abstract IControlSystem getControlSystem();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getDescription()
	 */
	public String getDescription() {
		return config.getDescription();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#getModeManager()
	 */
	public abstract IModelPresentation getModelPresentation();

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getName()
	 */
	public String getName() {
		return config.getName();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#getStatus()
	 */
	public ResourceManagerStatus getStatus() {
		return status;
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
	 * @see org.eclipse.ptp.rm.IResourceManager#start()
	 */
	public void start() throws CoreException {
		if (!status.equals(ResourceManagerStatus.STARTED) &&
				!status.equals(ResourceManagerStatus.ERROR)) {
			doStart();
			fireStarted();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#stop()
	 */
	public void stop() throws CoreException {
		if (status.equals(ResourceManagerStatus.STARTED)) {
			doStop();
			fireStopped();
		}
	}

	private void fireStarted() {
		Object[] tmpListeners = listeners.getListeners();
		
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleStarted(this);
		}
	}

	private void fireStatusChanged(ResourceManagerStatus oldStatus) {
		Object[] tmpListeners = listeners.getListeners();
		
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleStatusChanged(oldStatus, this);
		}
	}

	private void fireStopped() {
		Object[] tmpListeners = listeners.getListeners();
		
		for (int i = 0, n = tmpListeners.length; i < n; ++i) {
			IResourceManagerListener listener = (IResourceManagerListener) tmpListeners[i];
			listener.handleStopped(this);
		}
	}

	/**
	 * @throws CoreException
	 */
	protected abstract void doStart() throws CoreException;

	/**
	 * @throws CoreException
	 */
	protected abstract void doStop() throws CoreException;

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
