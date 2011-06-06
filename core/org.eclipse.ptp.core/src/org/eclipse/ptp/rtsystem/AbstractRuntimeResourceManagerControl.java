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
package org.eclipse.ptp.rtsystem;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.AbstractJobSubmission;
import org.eclipse.ptp.core.AbstractJobSubmission.JobSubStatus;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.ElementAttributeManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.messages.Messages;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerControl;
import org.eclipse.ptp.rmsystem.IJobStatus;
import org.eclipse.ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeConnectedStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeErrorStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeJobChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMachineChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeMessageEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNewQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeNodeChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeProcessChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeQueueChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRMChangeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveAllEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveJobEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveMachineEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveNodeEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveProcessEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRemoveQueueEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeRunningStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeShutdownStateEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeStartupErrorEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent;
import org.eclipse.ptp.utils.core.RangeSet;

/**
 * @author greg
 * @since 5.0
 * 
 */
public abstract class AbstractRuntimeResourceManagerControl extends AbstractResourceManagerControl implements IRuntimeEventListener {

	private class JobStatus implements IJobStatus {
		private IPJob fJob = null;
		private final String fJobId;
		private final ILaunchConfiguration fConfig;

		public JobStatus(String jobId, ILaunchConfiguration config) {
			fJobId = jobId;
			fConfig = config;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getErrorPath()
		 */
		public String getErrorPath() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getJobId()
		 */
		public String getJobId() {
			return fJobId;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getLaunchConfiguration()
		 */
		public ILaunchConfiguration getLaunchConfiguration() {
			return fConfig;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getOutputPath()
		 */
		public String getOutputPath() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getOwner()
		 */
		public String getOwner() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getQueueName()
		 */
		public String getQueueName() {
			return null;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getRmUniqueName()
		 */
		public String getRmUniqueName() {
			return getResourceManager().getUniqueName();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getState()
		 */
		public String getState() {
			IPJob job = getJob();
			if (job != null) {
				switch (fJob.getState()) {
				case COMPLETED:
					return IJobStatus.COMPLETED;
				case RUNNING:
					return IJobStatus.RUNNING;
				case STARTING:
					return IJobStatus.SUBMITTED;
				case SUSPENDED:
					return IJobStatus.SUSPENDED;
				}
			}
			return IJobStatus.UNDETERMINED;
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getStateDetail()
		 */
		public String getStateDetail() {
			IPJob job = getJob();
			if (job != null) {
				StringAttribute statusAttr = job.getAttribute(JobAttributes.getStatusAttributeDefinition());
				if (statusAttr != null) {
					return statusAttr.getValue();
				}
			}
			return getState();
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#getStreamsProxy()
		 */
		public IStreamsProxy getStreamsProxy() {
			AbstractRuntimeResourceManagerMonitor monitor = (AbstractRuntimeResourceManagerMonitor) getResourceManager()
					.getMonitor();
			return monitor.getProxy(fJobId);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.rmsystem.IJobStatus#isInteractive()
		 */
		public boolean isInteractive() {
			return false;
		}

		private IPJob getJob() {
			if (fJob == null) {
				fJob = getPResourceManager().getJobById(fJobId);
			}
			return fJob;
		}

	}

	private class JobSubmission extends AbstractJobSubmission {
		private final ILaunchConfiguration fConfiguration;
		private IJobStatus fJobStatus = null;

		public JobSubmission(int count, ILaunchConfiguration configuration) {
			super(count);
			fConfiguration = configuration;
		}

		/**
		 * @return the job status
		 */
		public IJobStatus getJobStatus() {
			return fJobStatus;
		}

		/**
		 * @param job
		 *            the job to set
		 */
		public void setJobId(String jobId) {
			fJobStatus = new JobStatus(jobId, fConfiguration);
		}
	}

	private final Map<String, JobSubmission> jobSubmissions = Collections.synchronizedMap(new HashMap<String, JobSubmission>());
	private final Map<String, IJobStatus> fJobStatus = new HashMap<String, IJobStatus>();

	private volatile int jobSubIdCounter = 0;

	/**
	 * @since 5.0
	 */
	public AbstractRuntimeResourceManagerControl(AbstractResourceManagerConfiguration config) {
		super(config);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeAttributeDefinitionEvent)
	 * 
	 * Note: this allows redefinition of attribute definitions. This is ok as
	 * long as they are only allowed during the initialization phase.
	 */
	public void handleEvent(IRuntimeAttributeDefinitionEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeConnectedStateEvent)
	 */
	public void handleEvent(IRuntimeConnectedStateEvent e) {
		// Ignore
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeErrorStateEvent)
	 */
	public void handleEvent(IRuntimeErrorStateEvent e) {
		/*
		 * Fatal error in the runtime system. Cancel any pending job submissions
		 * and inform upper levels of the problem.
		 */
		synchronized (jobSubmissions) {
			for (JobSubmission sub : jobSubmissions.values()) {
				sub.setError(Messages.AbstractRuntimeResourceManager_6);
			}
			jobSubmissions.clear();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeJobChangeEvent)
	 */
	public void handleEvent(IRuntimeJobChangeEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeMachineChangeEvent)
	 */
	public void handleEvent(IRuntimeMachineChangeEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeErrorEvent)
	 */
	public void handleEvent(IRuntimeMessageEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewJobEvent)
	 */
	public void handleEvent(IRuntimeNewJobEvent e) {
		ElementAttributeManager mgr = e.getElementAttributeManager();

		for (Map.Entry<RangeSet, AttributeManager> entry : mgr.getEntrySet()) {
			AttributeManager jobAttrs = entry.getValue();
			for (String elementId : entry.getKey()) {
				StringAttribute jobSubAttr = jobAttrs.getAttribute(JobAttributes.getSubIdAttributeDefinition());
				if (jobSubAttr != null) {
					/*
					 * Notify any submitJob() calls that the job has been
					 * created
					 */

					JobSubmission sub;
					synchronized (jobSubmissions) {
						sub = jobSubmissions.remove(jobSubAttr.getValue());
					}
					if (sub != null) {
						sub.setJobId(elementId);
						sub.setStatus(JobSubStatus.SUBMITTED);
					}
				}
			}

		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewMachineEvent)
	 */
	public void handleEvent(IRuntimeNewMachineEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewNodeEvent)
	 */
	public void handleEvent(IRuntimeNewNodeEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewProcessEvent)
	 */
	public void handleEvent(IRuntimeNewProcessEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNewQueueEvent)
	 */
	public void handleEvent(IRuntimeNewQueueEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeNodeChangeEvent)
	 */
	public void handleEvent(IRuntimeNodeChangeEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeProcessChangeEvent)
	 */
	public void handleEvent(IRuntimeProcessChangeEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeQueueChangeEvent)
	 */
	public void handleEvent(IRuntimeQueueChangeEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveAllEvent)
	 */
	public void handleEvent(IRuntimeRemoveAllEvent e) {
		cleanUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveJobEvent)
	 */
	public void handleEvent(IRuntimeRemoveJobEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveMachineEvent)
	 */
	public void handleEvent(IRuntimeRemoveMachineEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveNodeEvent)
	 */
	public void handleEvent(IRuntimeRemoveNodeEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveProcessEvent)
	 */
	public void handleEvent(IRuntimeRemoveProcessEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRemoveQueueEvent)
	 */
	public void handleEvent(IRuntimeRemoveQueueEvent e) {
		// Handled by monitor
	}

	public void handleEvent(IRuntimeRMChangeEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeRunningStateEvent)
	 */
	public void handleEvent(IRuntimeRunningStateEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeShutdownStateEvent)
	 */
	public void handleEvent(IRuntimeShutdownStateEvent e) {
		cleanUp();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeStartupErrorEvent)
	 */
	public void handleEvent(IRuntimeStartupErrorEvent e) {
		// Handled by monitor
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeSubmitJobErrorEvent)
	 */
	public void handleEvent(IRuntimeSubmitJobErrorEvent e) {
		if (e.getJobSubID() != null) {
			JobSubmission sub;
			synchronized (jobSubmissions) {
				sub = jobSubmissions.remove(e.getJobSubID());
			}
			if (sub != null) {
				sub.setError(e.getErrorMessage());
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rtsystem.IRuntimeEventListener#handleEvent(org.eclipse
	 * .ptp.rtsystem.events.IRuntimeTerminateJobErrorEvent)
	 */
	public void handleEvent(IRuntimeTerminateJobErrorEvent e) {
		IPJob job = getPResourceManager().getJobById(e.getJobID());
		String name = e.getJobID();
		if (job != null) {
			name = job.getName();
		}
		getResourceManager().fireResourceManagerError(
				NLS.bind(Messages.AbstractRuntimeResourceManager_4, new Object[] { name, e.getErrorMessage() }));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#doControlJob(java.lang
	 * .String, java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doControlJob(String jobId, String operation, IProgressMonitor monitor) throws CoreException {
		if (operation.equals(TERMINATE_OPERATION)) {
			getRuntimeSystem().terminateJob(jobId);
		} else {
			throw new CoreException(new Status(IStatus.CANCEL, PTPCorePlugin.getUniqueIdentifier(),
					Messages.AbstractRuntimeResourceManager_operationNotSupported));
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doDispose()
	 */
	@Override
	protected void doDispose() {
	}

	/*
	 * 
	 * 'Force' is ignored because there is no throttling here. (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#doGetJobStatus
	 * (java.lang.String, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IJobStatus doGetJobStatus(String jobId, boolean force, IProgressMonitor monitor) throws CoreException {
		synchronized (fJobStatus) {
			return fJobStatus.get(jobId);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManager#doShutdown()
	 */
	@Override
	protected void doShutdown() throws CoreException {
		getRuntimeSystem().removeRuntimeEventListener(this);
		cleanUp();
	};

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#doStartup(org.eclipse
	 * .core.runtime.IProgressMonitor)
	 */
	@Override
	protected void doStartup(IProgressMonitor monitor) throws CoreException {
		getRuntimeSystem().addRuntimeEventListener(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManager#doSubmitJob(org.eclipse
	 * .debug.core.ILaunchConfiguration,
	 * org.eclipse.ptp.core.attributes.AttributeManager,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected IJobStatus doSubmitJob(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}

		IJobStatus jobStatus = null;

		try {
			JobSubmission sub = new JobSubmission(jobSubIdCounter++, configuration);
			synchronized (jobSubmissions) {
				jobSubmissions.put(sub.getId(), sub);
			}

			getRuntimeSystem().submitJob(sub.getId(), configuration, mode);

			JobSubStatus state = sub.waitFor(monitor);

			switch (state) {
			case CANCELLED:
				/*
				 * Once a job has been sent to the RM, it can't be canceled, so
				 * this will just cause the submitJob command to throw an
				 * exception. The job will still eventually get created.
				 */
				synchronized (jobSubmissions) {
					jobSubmissions.remove(sub.getId());
				}
				throw new CoreException(new Status(IStatus.CANCEL, PTPCorePlugin.getUniqueIdentifier(),
						Messages.AbstractRuntimeResourceManager_cancelled));

			case SUBMITTED:
				jobStatus = sub.getJobStatus();
				synchronized (fJobStatus) {
					fJobStatus.put(jobStatus.getJobId(), jobStatus);
				}
				break;

			case ERROR:
				throw new CoreException(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), sub.getError()));
			}
		} finally {
			monitor.done();
		}

		return jobStatus;
	}

	protected IPResourceManager getPResourceManager() {
		return (IPResourceManager) getResourceManager().getAdapter(IPResourceManager.class);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerControl#getResourceManager
	 * ()
	 */
	@Override
	protected AbstractRuntimeResourceManager getResourceManager() {
		return (AbstractRuntimeResourceManager) super.getResourceManager();
	}

	protected IRuntimeSystem getRuntimeSystem() {
		return getResourceManager().getRuntimeSystem();
	}

	private void cleanUp() {
		/*
		 * Cancel any pending job submissions.
		 */
		synchronized (jobSubmissions) {
			for (JobSubmission sub : jobSubmissions.values()) {
				sub.setStatus(JobSubStatus.CANCELLED);
			}
			jobSubmissions.clear();
		}
		synchronized (fJobStatus) {
			fJobStatus.clear();
		}
	}
}