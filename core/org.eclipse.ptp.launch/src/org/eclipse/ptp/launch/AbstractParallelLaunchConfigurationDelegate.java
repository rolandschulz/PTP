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
package org.eclipse.ptp.launch;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.StringAttribute;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.IJobChangeEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangeEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent;
import org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent;
import org.eclipse.ptp.core.elements.listeners.IJobListener;
import org.eclipse.ptp.core.elements.listeners.IQueueChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerListener;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.data.ISynchronizationRule;
import org.eclipse.ptp.launch.data.RuleFactory;
import org.eclipse.ptp.launch.messages.Messages;
import org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback;
import org.eclipse.ptp.launch.rulesengine.IRuleAction;
import org.eclipse.ptp.launch.rulesengine.RuleActionFactory;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.utils.core.ArgumentParser;

/**
 *
 */
public abstract class AbstractParallelLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements
		ILaunchProcessCallback {
	/**
	 * Status of the job submission (NOT the job itself)
	 */
	public enum JobSubStatus {
		/**
		 * @since 4.0
		 */
		UNSUBMITTED, SUBMITTED, ERROR
	}

	/**
	 * The JobSubmission class encapsulates all the information used in a job
	 * submission. Once the job is created *and starts running*, this
	 * information is used to complete the launch.
	 * 
	 * TODO: Add persistence to job submissions so that Eclipse sessions can be
	 * restarted without losing the submission information. The persistence will
	 * also need to deal with starting the debugger for persisted debug jobs.
	 */
	private class JobSubmission {
		private final ILaunchConfiguration configuration;
		private final String mode;
		private final String id;
		private String error = null;
		private final IPLaunch launch;
		private final AttributeManager attrMgr;
		private final IPDebugger debugger;
		private JobSubStatus status = JobSubStatus.UNSUBMITTED;
		private final ReentrantLock subLock = new ReentrantLock();;
		private final Condition subCondition = subLock.newCondition();

		public JobSubmission(int count, ILaunchConfiguration configuration, String mode, IPLaunch launch, AttributeManager attrMgr,
				IPDebugger debugger) {
			this.configuration = configuration;
			this.mode = mode;
			this.launch = launch;
			this.attrMgr = attrMgr;
			this.debugger = debugger;
			this.id = "JOB_" + Long.toString(System.currentTimeMillis()) + Integer.toString(count); //$NON-NLS-1$
		}

		/**
		 * @return the attrMgr
		 */
		public AttributeManager getAttrMgr() {
			return attrMgr;
		}

		/**
		 * @return the configuration
		 */
		public ILaunchConfiguration getConfiguration() {
			return configuration;
		}

		/**
		 * @return the debugger
		 */
		public IPDebugger getDebugger() {
			return debugger;
		}

		/**
		 * @return the error
		 */
		public String getError() {
			return error;
		}

		/**
		 * @return the job submission id
		 */
		public String getId() {
			return id;
		}

		/**
		 * @return the launch
		 */
		public IPLaunch getLaunch() {
			return launch;
		}

		/**
		 * @return the mode
		 */
		public String getMode() {
			return mode;
		}

		/**
		 * set the error
		 */
		public void setError(String error) {
			this.error = error;
			setStatus(JobSubStatus.ERROR);
		}

		/**
		 * set the current status
		 */
		public void setStatus(JobSubStatus status) {
			subLock.lock();
			try {
				this.status = status;
				subCondition.signalAll();
			} finally {
				subLock.unlock();
			}
		}

		/**
		 * Wait for the job state to change
		 * 
		 * @return the state
		 */
		public JobSubStatus waitFor(IProgressMonitor monitor) {
			subLock.lock();
			try {
				while (!monitor.isCanceled() && status == JobSubStatus.UNSUBMITTED) {
					try {
						subCondition.await(100, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// Expect to be interrupted if monitor is canceled
					}
				}

				return status;
			} finally {
				subLock.unlock();
			}
		}
	}

	private final class JobListener implements IJobListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IJobListener#handleEvent(
		 * org.eclipse.ptp.core.elements.events.IJobChangeEvent)
		 */
		public void handleEvent(IJobChangeEvent e) {
			IPJob job = e.getSource();
			AttributeManager attrMgr = e.getAttributes();
			StringAttribute subIdAttr = job.getAttribute(JobAttributes.getSubIdAttributeDefinition());
			if (subIdAttr != null) {
				EnumeratedAttribute<JobAttributes.State> stateAttr = attrMgr.getAttribute(JobAttributes
						.getStateAttributeDefinition());
				if (stateAttr != null) {
					JobSubmission jobSub = jobSubmissions.get(subIdAttr.getValue());
					if (jobSub != null) {
						/*
						 * The job must now be SUBMITTED as we've been called by
						 * a job event handler. Set the status so that anyone
						 * waiting on the job submission will be notified.
						 */
						jobSub.setStatus(JobSubStatus.SUBMITTED);

						switch (stateAttr.getValue()) {
						case RUNNING:
							/*
							 * When the job starts running call back to notify
							 * that job submission is completed.
							 */
							doCompleteJobLaunch(jobSub.getConfiguration(), jobSub.getMode(), jobSub.getLaunch(),
									jobSub.getAttrMgr(), jobSub.getDebugger(), job);
							break;

						case COMPLETED:
							/*
							 * When the job terminates, do any post launch data
							 * synchronization.
							 */
							ILaunchConfiguration lconf = jobSub.getConfiguration();
							// If needed, copy data back.
							try {
								// Get the list of paths to be copied back.
								doPostLaunchSynchronization(lconf);
							} catch (CoreException e1) {
								PTPLaunchPlugin.log(e1);
							}

							/*
							 * Clean up any launch activities.
							 */
							doCleanupLaunch(jobSub.getConfiguration(), jobSub.getMode(), jobSub.getLaunch());

							jobSubmissions.remove(jobSub);
							break;
						}
					}
					PTPLaunchPlugin.getDefault().notifyJobStateChange(job, stateAttr.getValue());
				}
			}
		}
	}

	private final class MMChildListener implements IModelManagerChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.IChangedResourceManagerEvent)
		 */
		public void handleEvent(IChangedResourceManagerEvent e) {
			// Don't need to do anything
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.INewResourceManagerEvent)
		 */
		public void handleEvent(INewResourceManagerEvent e) {
			/*
			 * Add resource manager child listener so we get notified when new
			 * machines are added to the model.
			 */
			final IResourceManager rm = e.getResourceManager();
			rm.addChildListener(resourceManagerChildListener);
			rm.addElementListener(resourceManagerListener);
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent
		 * (org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
		 */
		public void handleEvent(IRemoveResourceManagerEvent e) {
			/*
			 * Removed resource manager child listener when resource manager is
			 * removed.
			 */
			final IResourceManager rm = e.getResourceManager();
			rm.removeChildListener(resourceManagerChildListener);
			rm.removeElementListener(resourceManagerListener);
		}
	}

	private final class QueueChildListener implements IQueueChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IChangedJobEvent)
		 */
		public void handleEvent(IChangedJobEvent e) {
			// Handled by IJobListener
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.INewJobEvent)
		 */
		public void handleEvent(INewJobEvent e) {
			/*
			 * Notify listeners that the job state has changed (in this case the
			 * job should be in pending state)
			 */
			for (IPJob job : e.getJobs()) {
				StringAttribute subIdAttr = job.getAttribute(JobAttributes.getSubIdAttributeDefinition());
				if (subIdAttr != null) {
					EnumeratedAttribute<JobAttributes.State> stateAttr = job.getAttribute(JobAttributes
							.getStateAttributeDefinition());
					if (stateAttr != null) {
						PTPLaunchPlugin.getDefault().notifyJobStateChange(job, stateAttr.getValue());
					}
				}
				job.addElementListener(jobListener);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent
		 * (org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
			for (IPJob job : e.getJobs()) {
				job.removeElementListener(jobListener);
			}
		}
	}

	private final class RMChildListener implements IResourceManagerChildListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerChangedMachineEvent)
		 */
		public void handleEvent(IChangedMachineEvent e) {
			// Don't need to do anything
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerChangedQueueEvent)
		 */
		public void handleEvent(IChangedQueueEvent e) {
			// Can safely ignore
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerNewMachineEvent)
		 */
		public void handleEvent(INewMachineEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queue.addChildListener(queueChildListener);
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerRemoveMachineEvent)
		 */
		public void handleEvent(IRemoveMachineEvent e) {
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
		 * #handleEvent(org.eclipse.ptp.core.elements.events.
		 * IResourceManagerRemoveQueueEvent)
		 */
		public void handleEvent(IRemoveQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queue.removeChildListener(queueChildListener);
			}
		}
	}

	private final class RMListener implements IResourceManagerListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.elements.events.IResourceManagerChangeEvent)
		 */
		public void handleEvent(IResourceManagerChangeEvent e) {
			// Ignore
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.elements.events.IResourceManagerErrorEvent)
		 */
		public void handleEvent(IResourceManagerErrorEvent e) {
			// TODO Auto-generated method stub
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.elements.listeners.IResourceManagerListener#
		 * handleEvent
		 * (org.eclipse.ptp.core.elements.events.IResourceManagerSubmitJobErrorEvent
		 * )
		 */
		public void handleEvent(IResourceManagerSubmitJobErrorEvent e) {
			JobSubmission jobSub = jobSubmissions.remove(e.getJobSubmissionId());
			jobSub.setError(e.getMessage());
		}
	}

	/**
	 * Get the program arguments specified in the Arguments tab
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getArguments(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, (String) null);
	}

	/**
	 * Get the debugger executable path
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getDebuggerExePath(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String) null);
	}

	/**
	 * Get the ID of the debugger for this launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getDebuggerID(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, (String) null);
	}

	/**
	 * Get the debugger "stop in main" flag
	 * 
	 * @param configuration
	 * @return "stop in main" flag
	 * @throws CoreException
	 */
	protected static boolean getDebuggerStopInMainFlag(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
	}

	/**
	 * Get the working directory for this debug session
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getDebuggerWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String) null);
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String[] getEnvironmentToAppend(ILaunchConfiguration configuration) throws CoreException {
		Map<?, ?> defaultEnv = null;
		Map<?, ?> configEnv = configuration.getAttribute(ILaunchManager.ATTR_ENVIRONMENT_VARIABLES, defaultEnv);
		if (configEnv == null) {
			return null;
		}
		if (!configuration.getAttribute(ILaunchManager.ATTR_APPEND_ENVIRONMENT_VARIABLES, true)) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Parallel_launcher_does_not_support));
		}

		List<String> strings = new ArrayList<String>(configEnv.size());
		Iterator<?> iter = configEnv.entrySet().iterator();
		while (iter.hasNext()) {
			Entry<?, ?> entry = (Entry<?, ?>) iter.next();
			String key = (String) entry.getKey();
			String value = (String) entry.getValue();
			strings.add(key + "=" + value); //$NON-NLS-1$

		}
		return strings.toArray(new String[strings.size()]);
	}

	/**
	 * Get the absolute path of the executable to launch. If the executable is
	 * on a remote machine, this is the path to the executable on that machine.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getExecutablePath(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String) null);
	}

	/**
	 * Get the name of the application to launch
	 * 
	 * @deprecated
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	@Deprecated
	protected static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, (String) null);
	}

	/**
	 * Get the name of the project
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String) null);
	}

	/**
	 * Get the name of the queue for the launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getQueueName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_QUEUE_NAME, (String) null);
	}

	/**
	 * Get the resource manager to use for the launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getResourceManagerUniqueName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String) null);
	}

	/**
	 * Get the working directory for the application launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, (String) null);
	}

	/*
	 * Total number of jobs submitted
	 */
	private int jobCount = 0;
	/*
	 * Model listeners
	 */
	private final IModelManagerChildListener modelManagerChildListener = new MMChildListener();
	private final IResourceManagerChildListener resourceManagerChildListener = new RMChildListener();
	private final IResourceManagerListener resourceManagerListener = new RMListener();
	private final IQueueChildListener queueChildListener = new QueueChildListener();
	private final IJobListener jobListener = new JobListener();
	/*
	 * HashMap used to keep track of job submissions
	 */
	protected Map<String, JobSubmission> jobSubmissions = Collections.synchronizedMap(new HashMap<String, JobSubmission>());
	/*
	 * Data synchronization rules
	 */
	private List<ISynchronizationRule> extraSynchronizationRules;

	/**
	 * Constructor
	 */
	public AbstractParallelLaunchConfigurationDelegate() {
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		synchronized (mm) {
			for (IResourceManager rm : mm.getUniverse().getResourceManagers()) {
				for (IPQueue queue : rm.getQueues()) {
					queue.addChildListener(queueChildListener);
				}
				rm.addChildListener(resourceManagerChildListener);
				rm.addElementListener(resourceManagerListener);
			}
			mm.addListener(modelManagerChildListener);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback#
	 * addSynchronizationRule(org.eclipse.ptp.launch.data.ISynchronizationRule)
	 */
	public void addSynchronizationRule(ISynchronizationRule rule) {
		extraSynchronizationRules.add(rule);
	}

	/**
	 * Get if the executable shall be copied to remote target before launch.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public boolean getCopyExecutable(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_COPY_EXECUTABLE, false);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.model.LaunchConfigurationDelegate#getLaunch(org
	 * .eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new PLaunch(configuration, mode, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback#getLocalFileManager
	 * (org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRemoteFileManager getLocalFileManager(ILaunchConfiguration configuration) throws CoreException {
		IRemoteServices localServices = PTPRemoteCorePlugin.getDefault().getDefaultServices();
		assert (localServices != null);
		IRemoteConnectionManager lconnMgr = localServices.getConnectionManager();
		assert (lconnMgr != null);
		IRemoteConnection lconn = lconnMgr.getConnection(""); //$NON-NLS-1$ // Since it's a local service, doesn't matter which parameter is passed 
		assert (lconn != null);
		IRemoteFileManager localFileManager = localServices.getFileManager(lconn);
		assert (localFileManager != null);
		return localFileManager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback#
	 * getRemoteFileManager(org.eclipse.debug.core.ILaunchConfiguration)
	 */
	public IRemoteFileManager getRemoteFileManager(ILaunchConfiguration configuration) throws CoreException {
		IResourceManagerControl rm = (IResourceManagerControl) getResourceManager(configuration);
		if (rm != null) {
			IResourceManagerConfiguration conf = rm.getConfiguration();
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault().getRemoteServices(conf.getRemoteServicesId());
			if (remoteServices != null) {
				IRemoteConnectionManager rconnMgr = remoteServices.getConnectionManager();
				if (rconnMgr != null) {
					IRemoteConnection rconn = rconnMgr.getConnection(conf.getConnectionName());
					if (rconn != null && rconn.isOpen()) {
						return remoteServices.getFileManager(rconn);
					}
				}
			}
		}
		return null;
	}

	/**
	 * Check if the copy local file is enabled. If it is, copy the executable
	 * file from the local host to the remote host.
	 * 
	 * @param configuration
	 * @throws CoreException
	 */
	protected void copyExecutable(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		boolean copyExecutable = getCopyExecutable(configuration);

		if (copyExecutable) {
			// Get remote and local paths
			String remotePath = getExecutablePath(configuration);
			String localPath = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_LOCAL_EXECUTABLE_PATH,
					(String) null);

			// Check if local path is valid
			if (localPath == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
						Messages.AbstractParallelLaunchConfigurationDelegate_1));
			}

			// Copy data
			copyFileToRemoteHost(localPath, remotePath, configuration, monitor);
		}
	}

	/**
	 * Copy a data from a path (can be a file or directory) from the remote host
	 * to the local host.
	 * 
	 * @param remotePath
	 * @param localPath
	 * @param configuration
	 * @throws CoreException
	 */
	protected void copyFileFromRemoteHost(String remotePath, String localPath, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		IRemoteFileManager localFileManager = getLocalFileManager(configuration);
		IRemoteFileManager remoteFileManager = getRemoteFileManager(configuration);
		if (remoteFileManager == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_0));
		}

		IFileStore rres = remoteFileManager.getResource(remotePath);
		if (!rres.fetchInfo(EFS.NONE, monitor).exists()) {
			// Local file not found!
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_Remote_resource_does_not_exist));
		}
		IFileStore lres = localFileManager.getResource(localPath);

		// Copy file
		rres.copy(lres, EFS.OVERWRITE, monitor);
	}

	/**
	 * Copy a data from a path (can be a file or directory) from the local host
	 * to the remote host.
	 * 
	 * @param localPath
	 * @param remotePath
	 * @param configuration
	 * @throws CoreException
	 */
	protected void copyFileToRemoteHost(String localPath, String remotePath, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		IRemoteFileManager localFileManager = getLocalFileManager(configuration);
		IRemoteFileManager remoteFileManager = getRemoteFileManager(configuration);
		if (remoteFileManager == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_0));
		}

		IFileStore lres = localFileManager.getResource(localPath);
		if (!lres.fetchInfo(EFS.NONE, monitor).exists()) {
			// Local file not found!
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_Local_resource_does_not_exist));
		}
		IFileStore rres = remoteFileManager.getResource(remotePath);

		// Copy file
		lres.copy(rres, EFS.OVERWRITE, monitor);
	}

	/**
	 * Called to cleanup once the job terminates
	 * 
	 * @param config
	 * @param mode
	 * @param launch
	 */
	protected abstract void doCleanupLaunch(ILaunchConfiguration config, String mode, IPLaunch launch);

	/**
	 * This method is called when the job state changes to RUNNING. This allows
	 * the launcher to complete the job launch.
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param mgr
	 * @param debugger
	 * @param job
	 */
	protected abstract void doCompleteJobLaunch(ILaunchConfiguration configuration, String mode, IPLaunch launch,
			AttributeManager mgr, IPDebugger debugger, IPJob job);

	/**
	 * @param configuration
	 * @throws CoreException
	 */
	protected void doPostLaunchSynchronization(ILaunchConfiguration configuration) throws CoreException {
		boolean syncAfter;
		syncAfter = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_AFTER, false);
		if (!syncAfter)
			return;
		List<?> rulesList = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES, new ArrayList<String>());

		// This faction generate action objects which execute according to rules
		RuleActionFactory ruleActFactory = new RuleActionFactory(configuration, this, new NullProgressMonitor());

		for (Object ruleObj : rulesList) {
			ISynchronizationRule syncRule = RuleFactory.createRuleFromString((String) ruleObj);
			if (syncRule.isDownloadRule()) {
				// Execute the action
				IRuleAction action = ruleActFactory.getAction(syncRule);
				action.run();
			}
		}
	}

	/**
	 * This method does the synchronization step before the job submission
	 * 
	 * @param configuration
	 * @param monitor
	 */
	protected void doPreLaunchSynchronization(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		boolean syncBefore = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_BEFORE, false);
		if (!syncBefore)
			return;

		// This faction generate action objects which execute according to rules
		RuleActionFactory ruleActFactory = new RuleActionFactory(configuration, this, monitor);

		List<?> rulesList = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES, new ArrayList<String>());

		// Iterate over rules executing them
		for (Object ruleObj : rulesList) {
			ISynchronizationRule syncRule = RuleFactory.createRuleFromString((String) ruleObj);
			if (syncRule.isUploadRule()) {
				// Execute the action
				IRuleAction action = ruleActFactory.getAction(syncRule);
				action.run();
			}

		}
	}

	/**
	 * Get all the attributes specified in the launch configuration.
	 * 
	 * @param configuration
	 * @param mode
	 * @return AttributeManager
	 * @throws CoreException
	 */
	protected AttributeManager getAttributeManager(ILaunchConfiguration configuration, String mode) throws CoreException {
		IResourceManager rm = getResourceManager(configuration);
		if (rm == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_No_ResourceManager));
		}

		AttributeManager attrMgr = new AttributeManager();

		/*
		 * Collect attributes from Resources tab
		 */
		attrMgr.addAttributes(getResourceAttributes(configuration, mode));

		/*
		 * Make sure there is a queue, even if the resources tab doesn't require
		 * one to be specified.
		 */
		if (attrMgr.getAttribute(JobAttributes.getQueueIdAttributeDefinition()) == null) {
			IPQueue queue = getQueueDefault(rm);
			attrMgr.addAttribute(JobAttributes.getQueueIdAttributeDefinition().create(queue.getID()));
		}

		/*
		 * Collect attributes from Application tab
		 */
		IPath programPath = verifyExecutablePath(configuration);
		attrMgr.addAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(programPath.lastSegment()));

		String path = programPath.removeLastSegments(1).toString();
		if (path != null) {
			attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(path));
		}

		/*
		 * Collect attributes from Debugger tab
		 */
		verifyDebuggerPath(configuration);
		Boolean stopInMainFlag = getDebuggerStopInMainFlag(configuration);
		attrMgr.addAttribute(JobAttributes.getDebuggerStopInMainFlagAttributeDefinition().create(stopInMainFlag));

		/*
		 * Collect attributes from Arguments tab
		 */
		String wd = verifyWorkDirectory(configuration);
		if (wd != null) {
			attrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(wd));
		}

		String[] argArr = getProgramArguments(configuration);
		if (argArr != null) {
			attrMgr.addAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(argArr));
		}

		/*
		 * Collect attributes from Environment tab
		 */
		String[] envArr = getEnvironmentToAppend(configuration);
		if (envArr != null) {
			attrMgr.addAttribute(JobAttributes.getEnvironmentAttributeDefinition().create(envArr));
		}

		/*
		 * PTP launched this job
		 */
		attrMgr.addAttribute(JobAttributes.getLaunchedByPTPFlagAttributeDefinition().create(true));

		return attrMgr;
	}

	/**
	 * Get the debugger configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return debugger configuration
	 * @throws CoreException
	 */
	protected IPDebugConfiguration getDebugConfig(ILaunchConfiguration config) throws CoreException {
		return PTPDebugCorePlugin.getDefault().getDebugConfiguration(getDebuggerID(config));
	}

	/**
	 * Convert application arguments to an array of strings.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return array of strings containing the program arguments
	 * @throws CoreException
	 */
	protected String[] getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String temp = getArguments(configuration);
		if (temp != null && temp.length() > 0) {
			ArgumentParser ap = new ArgumentParser(temp);
			List<String> args = ap.getTokenList();
			if (args != null) {
				return args.toArray(new String[args.size()]);
			}
		}
		return new String[0];
	}

	/**
	 * Get the path of the program to launch. No longer used since the program
	 * may not be on the local machine.
	 * 
	 * @deprecated
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return IPath corresponding to program executable
	 * @throws CoreException
	 */
	@Deprecated
	protected IPath getProgramFile(ILaunchConfiguration configuration) throws CoreException {
		IProject project = verifyProject(configuration);
		String fileName = getProgramName(configuration);
		if (fileName == null)
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_Application_file_not_specified));

		IPath programPath = new Path(fileName);
		if (!programPath.isAbsolute()) {
			programPath = project.getFile(programPath).getLocation();
		}
		if (!programPath.toFile().exists()) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST,
					Messages.AbstractParallelLaunchConfigurationDelegate_Application_file_does_not_exist,
					new FileNotFoundException(NLS.bind(Messages.AbstractParallelLaunchConfigurationDelegate_Path_not_found,
							new Object[] { programPath.toString() }))));
		}
		/*
		 * --old IFile programPath = project.getFile(fileName); if (programPath
		 * == null || !programPath.exists() ||
		 * !programPath.getLocation().toFile().exists())
		 * abort(LaunchMessages.getResourceString(
		 * "AbstractParallelLaunchConfigurationDelegate.Application_file_does_not_exist"
		 * ), new
		 * FileNotFoundException(LaunchMessages.getFormattedResourceString
		 * ("AbstractParallelLaunchConfigurationDelegate.Application_path_not_found"
		 * , programPath.getLocation().toString())), IStatus.INFO);
		 */
		return programPath;
	}

	/**
	 * Get the IProject object from the project name.
	 * 
	 * @param project
	 *            name of the project
	 * @return IProject resource
	 */
	protected IProject getProject(String project) {
		return getWorkspaceRoot().getProject(project);
	}

	/**
	 * Get the default queue for the given resource manager
	 * 
	 * @param rm
	 *            resource manager
	 * @return default queue
	 */
	protected IPQueue getQueueDefault(IResourceManager rm) {
		final IPQueue[] queues = rm.getQueues();
		if (queues.length == 0) {
			return null;
		}
		return queues[0];
	}

	/**
	 * Get the attributes from the resource manager specific launch page.
	 * 
	 * @param configuration
	 * @param mode
	 * @return IAttribute[]
	 * @throws CoreException
	 */
	protected IAttribute<?, ?, ?>[] getResourceAttributes(ILaunchConfiguration configuration, String mode) throws CoreException {

		IResourceManager rm = getResourceManager(configuration);

		final AbstractRMLaunchConfigurationFactory rmFactory = PTPLaunchPlugin.getDefault().getRMLaunchConfigurationFactory(rm);
		if (rmFactory == null) {
			return new IAttribute[0];
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = rmFactory.create(rm);
		return rmDynamicTab.getAttributes(rm, null, configuration, mode);
	}

	/**
	 * Find the resource manager that corresponds to the unique name specified
	 * in the configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return resource manager
	 * @throws CoreException
	 */
	protected IResourceManager getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		String rmUniqueName = getResourceManagerUniqueName(configuration);
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED && rm.getUniqueName().equals(rmUniqueName)) {
				return rm;
			}
		}
		return null;
	}

	/**
	 * Returns the (possible empty) list of synchronization rule objects
	 * according to the rules described in the configuration.
	 */
	protected ISynchronizationRule[] getSynchronizeRules(ILaunchConfiguration configuration) throws CoreException {
		List<?> ruleStrings = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES, new ArrayList<String>());
		List<ISynchronizationRule> result = new ArrayList<ISynchronizationRule>();

		for (Object ruleObj : ruleStrings) {
			String element = (String) ruleObj;
			try {
				ISynchronizationRule rule = RuleFactory.createRuleFromString(element);
				result.add(rule);
			} catch (RuntimeException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
						Messages.AbstractParallelLaunchConfigurationDelegate_Error_converting_rules));
			}
		}

		return result.toArray(new ISynchronizationRule[result.size()]);
	}

	/**
	 * Get the workspace root.
	 * 
	 * @return workspace root
	 */
	protected IWorkspaceRoot getWorkspaceRoot() {
		return ResourcesPlugin.getWorkspace().getRoot();
	}

	/**
	 * Create a source locator from the ID specified in the configuration, or
	 * create a default one if it hasn't been specified.
	 * 
	 * @param launch
	 * @param configuration
	 * @throws CoreException
	 */
	protected void setDefaultSourceLocator(ILaunch launch, ILaunchConfiguration configuration) throws CoreException {
		// set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			IPersistableSourceLocator sourceLocator;
			String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String) null);
			if (id == null) {
				sourceLocator = PTPDebugUIPlugin.createDefaultSourceLocator();
				sourceLocator.initializeDefaults(configuration);
			} else {
				sourceLocator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(id);
				String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String) null);
				if (memento == null) {
					sourceLocator.initializeDefaults(configuration);
				} else {
					sourceLocator.initializeFromMemento(memento);
				}
			}
			launch.setSourceLocator(sourceLocator);
		}
	}

	/**
	 * Set the source locator for this application
	 * 
	 * @param launch
	 * @param config
	 * @throws CoreException
	 */
	protected void setSourceLocator(ILaunch launch, ILaunchConfiguration config) throws CoreException {
		setDefaultSourceLocator(launch, config);
	}

	/**
	 * Submit a job to the resource manager. Keeps track of the submission so we
	 * know when the job actually starts running. When this happens, the
	 * abstract method doCompleteJobLaunch() is invoked.
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param attrMgr
	 * @param debugger
	 * @param monitor
	 * @throws CoreException
	 */
	protected void submitJob(ILaunchConfiguration configuration, String mode, IPLaunch launch, AttributeManager attrMgr,
			IPDebugger debugger, IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			final IResourceManager rm = getResourceManager(configuration);
			if (rm == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
						Messages.AbstractParallelLaunchConfigurationDelegate_No_ResourceManager));
			}

			JobSubmission jobSub = new JobSubmission(jobCount++, configuration, mode, launch, attrMgr, debugger);
			jobSubmissions.put(jobSub.getId(), jobSub);

			JobSubStatus status;

			try {
				rm.submitJob(jobSub.getId(), configuration, attrMgr, progress.newChild(5));
				status = jobSub.waitFor(progress.newChild(5));
			} catch (CoreException e) {
				jobSub.setError(e.getMessage());
				jobSub.setStatus(JobSubStatus.ERROR);
				status = JobSubStatus.ERROR;
			}

			if (status == JobSubStatus.ERROR) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID, jobSub.getError()));
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * @param project
	 * @param exePath
	 * @return
	 * @throws CoreException
	 * @deprecated
	 */
	@Deprecated
	protected IBinaryObject verifyBinary(IProject project, IPath exePath) throws CoreException {
		ICExtensionReference[] parserRef = CCorePlugin.getDefault().getBinaryParserExtensions(project);
		for (int i = 0; i < parserRef.length; i++) {
			try {
				IBinaryParser parser = (IBinaryParser) parserRef[i].createExtension();
				IBinaryObject exe = (IBinaryObject) parser.getBinary(exePath);
				if (exe != null) {
					return exe;
				}
			} catch (ClassCastException e) {
			} catch (IOException e) {
			}
		}
		IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
		try {
			return (IBinaryObject) parser.getBinary(exePath);
		} catch (ClassCastException e) {
		} catch (IOException e) {
		}
		Throwable exception = new FileNotFoundException(
				Messages.AbstractParallelLaunchConfigurationDelegate_Program_is_not_a_recongnized_executable);
		int code = IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY;
		MultiStatus status = new MultiStatus(PTPCorePlugin.getUniqueIdentifier(), code,
				Messages.AbstractParallelLaunchConfigurationDelegate_Program_is_not_a_recongnized_executable, exception);
		status.add(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), code,
				exception == null ? "" : exception.getLocalizedMessage(), exception)); //$NON-NLS-1$
		throw new CoreException(status);
	}

	/**
	 * @param path
	 * @throws CoreException
	 */
	protected void verifyDebuggerPath(ILaunchConfiguration configuration) throws CoreException {
		String dbgPath = getDebuggerExePath(configuration);
		try {
			verifyResource(dbgPath, configuration);
		} catch (CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_Debugger_path_not_found, new FileNotFoundException(
							e.getLocalizedMessage())));
		}
	}

	/**
	 * Verify the validity of executable path. If the executable is to be
	 * copied, then no additional verification is required. Otherwise, the path
	 * must point to an existing file.
	 * 
	 * @param configuration
	 * @return
	 */
	protected IPath verifyExecutablePath(ILaunchConfiguration configuration) throws CoreException {
		if (getCopyExecutable(configuration)) {
			return new Path(getExecutablePath(configuration));
		} else {
			String exePath = getExecutablePath(configuration);
			try {
				return verifyResource(exePath, configuration);
			} catch (CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
						Messages.AbstractParallelLaunchConfigurationDelegate_Application_file_does_not_exist,
						new FileNotFoundException(e.getLocalizedMessage())));
			}
		}
	}

	// Methods below implement the ILaunchProcessCallback interface

	/**
	 * @param path
	 * @return
	 * @deprecated
	 */
	@Deprecated
	protected boolean verifyPath(String path) {
		IPath programPath = new Path(path);
		if (programPath == null || programPath.isEmpty() || !programPath.toFile().exists()) {
			return false;
		}
		return true;
	}

	/**
	 * Verify that the project exists prior to the launch.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected IProject verifyProject(ILaunchConfiguration configuration) throws CoreException {
		String proName = getProjectName(configuration);
		if (proName == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_Project_not_specified));
		}

		IProject project = getProject(proName);
		if (project == null || !project.exists() || !project.isOpen()) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.PLUGIN_ID,
					Messages.AbstractParallelLaunchConfigurationDelegate_Project_does_not_exist_or_is_not_a_project));
		}

		return project;
	}

	/**
	 * @param path
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected IPath verifyResource(String path, ILaunchConfiguration configuration) throws CoreException {
		return PTPLaunchPlugin.getDefault().verifyResource(path, configuration);
	}

	/**
	 * Verify the working directory. If no working directory is specified, the
	 * default is the location of the executable.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected String verifyWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
		IPath path;
		String workPath = getWorkDirectory(configuration);
		if (workPath == null) {
			path = verifyExecutablePath(configuration).removeLastSegments(1);
		} else {
			path = verifyResource(workPath, configuration);
		}
		return path.toString();
	}

}
