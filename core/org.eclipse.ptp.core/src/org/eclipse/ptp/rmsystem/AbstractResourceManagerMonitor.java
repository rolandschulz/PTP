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
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.jobs.IJobAddedEvent;
import org.eclipse.ptp.core.jobs.IJobChangedEvent;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;

/**
 * @since 5.0
 * 
 */
public abstract class AbstractResourceManagerMonitor implements IResourceManagerMonitor {
	private class JobListener implements IJobListener {

		public void handleEvent(IJobAddedEvent e) {
			doAddJob(e.getJobStatus());
		}

		public void handleEvent(IJobChangedEvent e) {
			doUpdateJob(e.getJobStatus());
		}

	}

	private final JobListener fJobListener = new JobListener();
	private final ModelManager fModelManager = (ModelManager) PTPCorePlugin.getDefault().getModelManager();
	private final AbstractResourceManagerConfiguration fConfig;

	private IResourceManager fResourceManager = null;

	public AbstractResourceManagerMonitor(AbstractResourceManagerConfiguration config) {
		fConfig = config;
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
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#getMonitorConfiguration ()
	 */
	public IResourceManagerComponentConfiguration getMonitorConfiguration() {
		return fConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#removeJob(java.lang. String)
	 */
	public void removeJob(String jobId) {
		doRemoveJob(jobId);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#start(org.eclipse.core .runtime.IProgressMonitor)
	 */
	public void start(IProgressMonitor monitor) throws CoreException {
		JobManager.getInstance().addListener(fJobListener);
		doStartup(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitor#stop()
	 */
	public void stop() throws CoreException {
		doShutdown();
		JobManager.getInstance().removeListener(fJobListener);
	}

	/**
	 * Notify monitor that job should be treated specially.
	 * 
	 * @param status
	 *            current status of job
	 * @since 6.0
	 */
	protected abstract void doAddJob(IJobStatus status);

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
	 * Stop the resource manager subsystem. This must be callable at any time (including if the resource manager is not started). It
	 * should take any actions necessary to shut down the subsystem. Implementations should not modify the resource manager state,
	 * this will be handled by the main resource manager class.
	 * 
	 * @throws CoreException
	 *             this exception should be thrown if the shutdown encountered an error for any reason. This will not halt shutdown
	 *             of the resource manager, but may produce a message to the user.
	 */
	protected abstract void doShutdown() throws CoreException;

	/**
	 * Start the resource manager subsystem. This should perform any actions necessary to start the subsystem. Implementations
	 * should not modify the resource manager state, this will be handled by the main resource manager class. If the subsystem fails
	 * to start, implementations should throw a CoreException with a description of what went wrong.
	 * 
	 * The progress monitor is primarily for indicating startup status to the user. If the user cancels the progress monitor, the
	 * implementation should assume the resource manager has not started and {@link #doShutdown()} will be called soon after.
	 * 
	 * @param monitor
	 *            progress monitor indicating startup progress and for cancelling startup
	 * @throws CoreException
	 *             this exception should be thrown if the startup encountered an error for any reason. This will halt shutdown of
	 *             the resource manager, and may produce a message to the user.
	 */
	protected abstract void doStartup(IProgressMonitor monitor) throws CoreException;

	/**
	 * Notify monitor that status of job has changed.
	 * 
	 * @param status
	 *            new status of job
	 * @since 6.0
	 */
	protected abstract void doUpdateJob(IJobStatus status);

	protected void fireResourceManagerError(String message) {
		getResourceManager().fireResourceManagerError(message);
	}

	protected ModelManager getModelManager() {
		return fModelManager;
	}

	protected IPResourceManager getPResourceManager() {
		return (IPResourceManager) getResourceManager().getAdapter(IPResourceManager.class);
	}

	protected AbstractResourceManager getResourceManager() {
		if (fResourceManager == null) {
			fResourceManager = PTPCorePlugin.getDefault().getModelManager()
					.getResourceManagerFromUniqueName(fConfig.getUniqueName());
		}
		return (AbstractResourceManager) fResourceManager;
	}
}