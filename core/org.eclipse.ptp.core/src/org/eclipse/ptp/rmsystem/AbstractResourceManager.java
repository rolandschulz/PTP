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

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.ptp.core.IModelPresentation;
import org.eclipse.ptp.rtsystem.IControlSystem;

/**
 * @author rsqrd
 * 
 */
public abstract class AbstractResourceManager implements IResourceManager {
	
	private final List listeners = new ArrayList();

	private final IResourceManagerConfiguration config;

	private IStatus status;
	
	public AbstractResourceManager(IResourceManagerConfiguration config)
	{
		this.config = config;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#addResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public synchronized void addResourceManagerListener(IResourceManagerListener listener) {
		if (!listeners.contains(listener))
			listeners.add(listener);
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

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#getModeManager()
	 */
	public abstract IModelPresentation getModelPresentation();

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#removeResourceManagerListener(org.eclipse.ptp.rm.IResourceManagerListener)
	 */
	public synchronized void removeResourceManagerListener(IResourceManagerListener listener) {
		listeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#start()
	 */
	public void start() throws CoreException {
		doStart();
		fireStarted();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rm.IResourceManager#stop()
	 */
	public void stop() throws CoreException {
		getModelManager().shutdown();
		doStop();
		fireStopped();
	}

	public IStatus getStatus() {
		return status;
	}

	protected void setStatus(IStatus status) {
		final IStatus oldStatus = this.status;
		this.status = status;
		fireStatusChanged(oldStatus);
	}

	private void fireStatusChanged(IStatus oldStatus) {
		// make a copy of the listener list in case one of the listeners
		// wants to add or remove a listener
		List tmpListeners = new ArrayList(listeners);
		
		for (Iterator tit = tmpListeners.iterator(); tit.hasNext(); ) {
			IResourceManagerListener listener = (IResourceManagerListener) tit.next();
			listener.handleStatusChanged(oldStatus, this);
		}
	}

	private void fireStarted() {
		// make a copy of the listener list in case one of the listeners
		// wants to add or remove a listener
		List tmpListeners = new ArrayList(listeners);
		
		for (Iterator tit = tmpListeners.iterator(); tit.hasNext(); ) {
			IResourceManagerListener listener = (IResourceManagerListener) tit.next();
			listener.handleStarted(this);
		}
	}

	private void fireStopped() {
		// make a copy of the listener list in case one of the listeners
		// wants to add or remove a listener
		List tmpListeners = new ArrayList(listeners);
		
		for (Iterator tit = tmpListeners.iterator(); tit.hasNext(); ) {
			IResourceManagerListener listener = (IResourceManagerListener) tit.next();
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

}
