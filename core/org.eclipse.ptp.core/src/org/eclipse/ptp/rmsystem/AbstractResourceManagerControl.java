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
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.ptp.core.ModelManager;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;

/**
 * @since 5.0
 * 
 */
public abstract class AbstractResourceManagerControl implements IResourceManagerControl {
	private final AbstractResourceManagerConfiguration fConfig;
	private IResourceManager fResourceManager = null;

	public AbstractResourceManagerControl(AbstractResourceManagerConfiguration config) {
		fConfig = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#control(java.lang.String , java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void control(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		doControlJob(jobId, operation, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#dispose()
	 */
	public void dispose() {
		doDispose();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#getControlConfiguration ()
	 */
	public IResourceManagerComponentConfiguration getControlConfiguration() {
		return fConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.core.jobs.IJobControl#getControlId()
	 */
	/**
	 * @since 6.0
	 */
	public String getControlId() {
		return fConfig.getUniqueName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#getJobStatus(java.lang .String)
	 */
	/**
	 * @since 6.0
	 */
	public IJobStatus getJobStatus(String jobId, boolean force, IProgressMonitor monitor) {
		IJobStatus status = null;
		try {
			status = doGetJobStatus(jobId, force, monitor);
		} catch (CoreException e) {
		}
		if (status == null) {
			status = new IJobStatus() {
				public String getControlId() {
					return fConfig.getUniqueName();
				}

				public String getErrorPath() {
					return null;
				}

				public String getJobId() {
					return null;
				}

				public ILaunchConfiguration getLaunchConfiguration() {
					return null;
				}

				public String getOutputPath() {
					return null;
				}

				public String getOwner() {
					return null;
				}

				public String getQueueName() {
					return null;
				}

				public String getState() {
					return UNDETERMINED;
				}

				public String getStateDetail() {
					return UNDETERMINED;
				}

				public IStreamsProxy getStreamsProxy() {
					return null;
				}

				public boolean isInteractive() {
					return false;
				}
			};
		}
		return status;
	}

	/*
	 * Equivalent to the two-parameter call (force is not implemented). (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#getJobStatus(java.lang .String, boolean,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 6.0
	 */
	public IJobStatus getJobStatus(String jobId, IProgressMonitor monitor) {
		return getJobStatus(jobId, false, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#start(org.eclipse.core .runtime.IProgressMonitor)
	 */
	public void start(IProgressMonitor monitor) throws CoreException {
		doStartup(monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#stop()
	 */
	public void stop() throws CoreException {
		doShutdown();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControl#submitJob(org.eclipse .debug.core.ILaunchConfiguration,
	 * java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String submitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		IJobStatus status = doSubmitJob(configuration, mode, monitor);
		JobManager.getInstance().fireJobChanged(status);
		return status.getJobId();
	}

	/**
	 * Control a job.
	 * 
	 * @param jobId
	 *            ID of job to control
	 * @param operation
	 *            operation to perform on job
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 */
	protected abstract void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException;

	/**
	 * Perform any activities prior to disposing of the resource manager.
	 */
	protected abstract void doDispose();

	/**
	 * Get the status of a job.
	 * 
	 * @throws CoreException
	 * @since 6.0
	 */
	protected abstract IJobStatus doGetJobStatus(String jobId, boolean force, IProgressMonitor monitor) throws CoreException;

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
	 * Submit a job to the resource manager. Returns a job ID that represents the submitted job. Throws a core exception if there
	 * was an error submitting the job or if the progress monitor was canceled.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param mode
	 *            launch mode
	 * @param monitor
	 *            progress monitor
	 * @return job status representing the status of the submitted job
	 * @throws CoreException
	 * @since 6.0
	 */
	protected abstract IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException;

	protected void fireResourceManagerError(String message) {
		getResourceManager().fireResourceManagerError(message);
	}

	protected AbstractResourceManager getResourceManager() {
		if (fResourceManager == null) {
			fResourceManager = ModelManager.getInstance().getResourceManagerFromUniqueName(fConfig.getUniqueName());
		}
		return (AbstractResourceManager) fResourceManager;
	}
}