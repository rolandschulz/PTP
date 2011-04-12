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
import org.eclipse.ptp.core.PTPCorePlugin;

/**
 * @since 5.0
 * 
 */
public abstract class AbstractResourceManagerControl implements IResourceManagerControl {
	private final AbstractResourceManagerConfiguration fConfig;
	private final ModelManager fModelManager = (ModelManager) PTPCorePlugin.getDefault().getModelManager();
	private IResourceManager fResourceManager = null;

	public AbstractResourceManagerControl(AbstractResourceManagerConfiguration config) {
		fConfig = config;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#control(java.lang.String
	 * , java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
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
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#getControlConfiguration
	 * ()
	 */
	public IResourceManagerComponentConfiguration getControlConfiguration() {
		return fConfig;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#getJobStatus(java.lang
	 * .String)
	 */
	public IJobStatus getJobStatus(String jobId) {
		IJobStatus status = null;
		try {
			status = doGetJobStatus(jobId);
		} catch (CoreException e) {
		}
		if (status == null) {
			status = new IJobStatus() {
				public String getJobId() {
					return null;
				}

				public ILaunchConfiguration getLaunchConfiguration() {
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
			};
		}
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#start(org.eclipse.core
	 * .runtime.IProgressMonitor)
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
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl#submitJob(org.eclipse
	 * .debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public String submitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor) throws CoreException {
		IJobStatus status = doSubmitJob(configuration, mode, monitor);
		String jobId = status.getJobId();
		getResourceManager().fireJobChanged(jobId);
		return jobId;
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
	 */
	protected abstract IJobStatus doGetJobStatus(String jobId) throws CoreException;

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
	 * Submit a job to the resource manager. Returns a job ID that represents
	 * the submitted job. Throws a core exception if there was an error
	 * submitting the job or if the progress monitor was canceled.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param mode
	 *            launch mode
	 * @param monitor
	 *            progress monitor
	 * @return job status representing the status of the submitted job
	 * @throws CoreException
	 */
	protected abstract IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException;

	protected ModelManager getModelManager() {
		return fModelManager;
	}

	protected AbstractResourceManager getResourceManager() {
		if (fResourceManager == null) {
			fResourceManager = fModelManager.getResourceManagerFromUniqueName(fConfig.getUniqueName());
		}
		return (AbstractResourceManager) fResourceManager;
	}
}