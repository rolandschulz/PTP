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
package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.ListenerList;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.events.IJobChangedEvent;
import org.eclipse.ptp.core.listeners.IJobListener;
import org.eclipse.ptp.internal.core.events.JobChangedEvent;

/**
 * @since 5.0
 * 
 */
public abstract class AbstractResourceManagerMonitor implements IResourceManagerMonitor {
	private final ListenerList fJobListeners = new ListenerList();
	private final ModelManager fModelManager = (ModelManager) PTPCorePlugin.getDefault().getModelManager();
	private final AbstractResourceManagerConfiguration fConfig;

	private IResourceManager fResourceManager = null;

	public AbstractResourceManagerMonitor(AbstractResourceManagerConfiguration config) {
		fConfig = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerMonitor#addJob(java.lang.String,
	 * org.eclipse.ptp.rmsystem.IJobStatus)
	 */
	public void addJob(String jobId, IJobStatus status) {
		doAddJob(jobId, status);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#addJobListener(org.eclipse
	 * .ptp.core.listeners.IJobListener)
	 */
	/**
	 * @since 5.0
	 */
	public void addJobListener(IJobListener listener) {
		fJobListeners.add(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#dispose()
	 */
	public void dispose() {
		doDispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.core.runtime.IAdaptable#getAdapter(java.lang.Class)
	 */
	@SuppressWarnings({ "rawtypes" })
	public Object getAdapter(Class adapter) {
		if (adapter.isInstance(this)) {
			return this;
		}
		if (adapter == IPResourceManager.class) {
			return getPResourceManager();
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerMonitor#getMonitorConfiguration
	 * ()
	 */
	public IResourceManagerComponentConfiguration getMonitorConfiguration() {
		return fConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerMonitor#removeJob(java.lang.
	 * String)
	 */
	public void removeJob(String jobId) {
		doRemoveJob(jobId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManager#removeJobListener(org
	 * .eclipse.ptp.core.listeners.IJobListener)
	 */
	/**
	 * @since 5.0
	 */
	public void removeJobListener(IJobListener listener) {
		fJobListeners.remove(listener);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerMonitor#start(org.eclipse.core
	 * .runtime.IProgressMonitor)
	 */
	public void start(IProgressMonitor monitor) throws CoreException {
		doStartup(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#stop()
	 */
	public void stop() throws CoreException {
		doShutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerMonitor#updateJob(java.lang.
	 * String, org.eclipse.ptp.rmsystem.IJobStatus)
	 */
	public void updateJob(String jobId, IJobStatus status) {
		doUpdateJob(jobId, status);
	}

	/**
	 * Notify monitor that job should be treated specially.
	 * 
	 * @param jobId
	 *            ID of job to be treated specially
	 * @param status
	 *            current status of job
	 */
	protected abstract void doAddJob(String jobId, IJobStatus status);

	/**
	 * Perform any activities prior to disposing of the resource manager.
	 */
	protected abstract void doDispose();

	/**
	 * Notify monitor that job should no longer be treated specially
	 * 
	 * @param jobId
	 *            ID of job to remove
	 */
	protected abstract void doRemoveJob(String jobId);

	/**
	 * Stop the resource manager subsystem.
	 * 
	 * @throws CoreException
	 */
	protected abstract void doShutdown() throws CoreException;

	/**
	 * Start the resource manager subsystem.
	 * 
	 * @param monitor
	 * @throws CoreException
	 */
	protected abstract void doStartup(IProgressMonitor monitor) throws CoreException;

	/**
	 * Notify monitor that status of job has changed.
	 * 
	 * @param jobId
	 *            ID of job to be updated
	 * @param status
	 *            new status of job
	 */
	protected abstract void doUpdateJob(String jobId, IJobStatus status);

	/**
	 * Notify listeners when a job has changed.
	 * 
	 * @param jobId
	 *            ID of job that has changed
	 * @since 5.0
	 */
	protected void fireJobChanged(String jobId) {
		IJobChangedEvent e = new JobChangedEvent(getResourceManager(), jobId);

		for (Object listener : fJobListeners.getListeners()) {
			((IJobListener) listener).handleEvent(e);
		}
	}

	protected ModelManager getModelManager() {
		return fModelManager;
	}

	protected IPResourceManager getPResourceManager() {
		return (IPResourceManager) getResourceManager().getAdapter(IPResourceManager.class);
	}

	protected IResourceManager getResourceManager() {
		if (fResourceManager == null) {
			fResourceManager = PTPCorePlugin.getDefault().getModelManager()
					.getResourceManagerFromUniqueName(fConfig.getUniqueName());
		}
		return fResourceManager;
	}
}