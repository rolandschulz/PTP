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

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.events.IJobChangedEvent;
import org.eclipse.ptp.core.listeners.IJobListener;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.messages.Messages;
import org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback;
import org.eclipse.ptp.launch.rulesengine.IRuleAction;
import org.eclipse.ptp.launch.rulesengine.ISynchronizationRule;
import org.eclipse.ptp.launch.rulesengine.RuleActionFactory;
import org.eclipse.ptp.launch.rulesengine.RuleFactory;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.utils.core.ArgumentParser;

/**
 *
 */
public abstract class AbstractParallelLaunchConfigurationDelegate extends LaunchConfigurationDelegate implements
		ILaunchProcessCallback {

	/**
	 * Wait for job to begin running, then call completion method
	 * 
	 * <pre>
	 * Job state transition is STARTING--->RUNNING---->COMPLETED
	 *                                  ^            |
	 *                                  |-SUSPENDED<-|
	 * </pre>
	 * 
	 * We must call completion method when job state is RUNNING, however it is
	 * possible that the job may get to COMPLETED or SUSPENDED before we are
	 * started. If either of these states is reached, assume that RUNNING has
	 * also been reached.
	 */
	private class JobSubmission extends Job {
		private final IPLaunch fLaunch;
		private final AttributeManager fAttrMgr;
		private final IPDebugger fDebugger;
		private final ReentrantLock fSubLock = new ReentrantLock();
		private final Condition fSubCondition = fSubLock.newCondition();

		public JobSubmission(IPLaunch launch, AttributeManager attrMgr, IPDebugger debugger) {
			super(launch.getJobId());
			fLaunch = launch;
			fAttrMgr = attrMgr;
			fDebugger = debugger;
		}

		public void statusChanged() {
			fSubLock.lock();
			try {
				fSubCondition.signalAll();
			} finally {
				fSubLock.unlock();
			}
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.
		 * IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			IResourceManagerControl rm = fLaunch.getResourceManager();
			String jobId = fLaunch.getJobId();
			fSubLock.lock();
			try {
				while (rm.getJobStatus(jobId).getState() == JobAttributes.State.STARTING) {
					try {
						fSubCondition.await(100, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// Expect to be interrupted if monitor is canceled
					}
				}
			} finally {
				fSubLock.unlock();
			}

			doCompleteJobLaunch(fLaunch, fAttrMgr, fDebugger);

			fSubLock.lock();
			try {
				while (rm.getJobStatus(jobId).getState() != JobAttributes.State.COMPLETED) {
					try {
						fSubCondition.await(100, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// Expect to be interrupted if monitor is canceled
					}
				}
			} finally {
				fSubLock.unlock();
			}

			/*
			 * When the job terminates, do any post launch data synchronization.
			 */
			// If needed, copy data back.
			try {
				// Get the list of paths to be copied back.
				doPostLaunchSynchronization(fLaunch.getLaunchConfiguration());
			} catch (CoreException e) {
				PTPLaunchPlugin.log(e);
			}

			/*
			 * Clean up any launch activities.
			 */
			doCleanupLaunch(fLaunch);

			/*
			 * Remove job submission
			 */
			synchronized (jobSubmissions) {
				jobSubmissions.remove(jobId);
				if (jobSubmissions.size() == 0) {
					rm.removeJobListener(fJobListener);
				}
			}
			return Status.OK_STATUS;
		}
	}

	private final class JobListener implements IJobListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see
		 * org.eclipse.ptp.core.listeners.IJobListener#handleEvent(org.eclipse
		 * .ptp.core.events.IJobChangeEvent)
		 */
		public void handleEvent(IJobChangedEvent e) {
			JobSubmission jobSub;
			synchronized (jobSubmissions) {
				jobSub = jobSubmissions.get(e.getJobId());
			}
			if (jobSub != null) {
				jobSub.statusChanged();
			}
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
	 * Model listeners
	 */
	private final IJobListener fJobListener = new JobListener();
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
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.rulesengine.ILaunchProcessCallback#
	 * addSynchronizationRule(org.eclipse.ptp.launch.data.ISynchronizationRule)
	 */
	/**
	 * @since 5.0
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
	 * getRemoteFileManager(org.eclipse.debug.core.ILaunchConfiguration,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 5.0
	 */
	public IRemoteFileManager getRemoteFileManager(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		IResourceManagerControl rm = getResourceManager(configuration);
		if (rm != null) {
			IResourceManagerConfiguration conf = rm.getConfiguration();
			IRemoteServices remoteServices = PTPRemoteCorePlugin.getDefault()
					.getRemoteServices(conf.getRemoteServicesId(), monitor);
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
	 *            launch configuration
	 * @throws CoreException
	 *             if the copy fails or is cancelled
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
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
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
		SubMonitor progress = SubMonitor.convert(monitor, 15);
		try {
			IRemoteFileManager localFileManager = getLocalFileManager(configuration);
			IRemoteFileManager remoteFileManager = getRemoteFileManager(configuration, progress.newChild(5));
			if (progress.isCanceled()) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user, null));
			}
			if (remoteFileManager == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_0));
			}

			IFileStore rres = remoteFileManager.getResource(remotePath);
			if (!rres.fetchInfo(EFS.NONE, progress.newChild(5)).exists()) {
				// Local file not found!
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Remote_resource_does_not_exist));
			}
			IFileStore lres = localFileManager.getResource(localPath);

			// Copy file
			rres.copy(lres, EFS.OVERWRITE, progress.newChild(5));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
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
		SubMonitor progress = SubMonitor.convert(monitor, 15);
		try {
			IRemoteFileManager localFileManager = getLocalFileManager(configuration);
			IRemoteFileManager remoteFileManager = getRemoteFileManager(configuration, progress.newChild(5));
			if (progress.isCanceled()) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user, null));
			}
			if (remoteFileManager == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_0));
			}

			IFileStore lres = localFileManager.getResource(localPath);
			if (!lres.fetchInfo(EFS.NONE, progress.newChild(5)).exists()) {
				// Local file not found!
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Local_resource_does_not_exist));
			}
			IFileStore rres = remoteFileManager.getResource(remotePath);

			// Copy file
			lres.copy(rres, EFS.OVERWRITE, progress.newChild(5));
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Called to cleanup once the job terminates
	 * 
	 * @param config
	 * @param mode
	 * @param launch
	 * @since 5.0
	 */
	protected abstract void doCleanupLaunch(IPLaunch launch);

	/**
	 * This method is called when the job state changes to RUNNING. This allows
	 * the launcher to complete the job launch.
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param mgr
	 * @param debugger
	 * @param rm
	 * @param jobId
	 * @since 5.0
	 */
	protected abstract void doCompleteJobLaunch(IPLaunch launch, AttributeManager mgr, IPDebugger debugger);

	/**
	 * @param configuration
	 * @throws CoreException
	 */
	protected void doPostLaunchSynchronization(ILaunchConfiguration configuration) throws CoreException {
		if (configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_AFTER, false)) {
			List<?> rulesList = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES,
					new ArrayList<String>());

			// This faction generate action objects which execute according to
			// rules
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
	}

	/**
	 * This method does the synchronization step before the job submission
	 * 
	 * @param configuration
	 * @param monitor
	 */
	protected void doPreLaunchSynchronization(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		if (configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_BEFORE, false)) {
			// This faction generate action objects which execute according to
			// rules
			RuleActionFactory ruleActFactory = new RuleActionFactory(configuration, this, monitor);

			try {
				List<?> rulesList = configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_SYNC_RULES,
						new ArrayList<String>());

				// Iterate over rules executing them
				for (Object ruleObj : rulesList) {
					ISynchronizationRule syncRule = RuleFactory.createRuleFromString((String) ruleObj);
					if (syncRule.isUploadRule()) {
						// Execute the action
						IRuleAction action = ruleActFactory.getAction(syncRule);
						action.run();
					}

				}
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}
	}

	/**
	 * Validate and return the attributes specified in the launch configuration.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param mode
	 *            launch mode (run or debug)
	 * @param monitor
	 *            progress monitor
	 * @return AttributeManager containing the validated attributes from the
	 *         launch configuration
	 * @throws CoreException
	 *             if the validation fails or the progress monitor is cancelled
	 * @since 5.0
	 */
	protected AttributeManager getAttributeManager(ILaunchConfiguration configuration, String mode, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 30);

		try {
			IResourceManagerControl rmc = getResourceManager(configuration);
			if (rmc == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_No_ResourceManager));
			}
			IPResourceManager rm = (IPResourceManager) rmc.getAdapter(IPResourceManager.class);

			AttributeManager attrMgr = new AttributeManager();

			/*
			 * Collect attributes from Resources tab
			 */
			attrMgr.addAttributes(getResourceAttributes(configuration, mode));

			/*
			 * Make sure there is a queue, even if the resources tab doesn't
			 * require one to be specified.
			 */
			if (attrMgr.getAttribute(JobAttributes.getQueueIdAttributeDefinition()) == null) {
				IPQueue queue = getQueueDefault(rm);
				if (queue == null) {
					throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
							Messages.AbstractParallelLaunchConfigurationDelegate_2));
				}
				attrMgr.addAttribute(JobAttributes.getQueueIdAttributeDefinition().create(queue.getID()));
			}

			/*
			 * Collect attributes from Application tab
			 */
			IPath programPath = verifyExecutablePath(configuration, progress.newChild(10));
			attrMgr.addAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(programPath.lastSegment()));

			String path = programPath.removeLastSegments(1).toString();
			if (path != null) {
				attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(path));
			}

			/*
			 * Collect attributes from Arguments tab
			 */
			String wd = verifyWorkDirectory(configuration, progress.newChild(10));
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
			 * Collect attributes from Debugger tab if this is a debug launch
			 */
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				verifyDebuggerPath(configuration, progress.newChild(10));
				Boolean stopInMainFlag = getDebuggerStopInMainFlag(configuration);
				attrMgr.addAttribute(JobAttributes.getDebuggerStopInMainFlagAttributeDefinition().create(stopInMainFlag));
			}

			/*
			 * PTP launched this job
			 */
			attrMgr.addAttribute(JobAttributes.getLaunchedByPTPFlagAttributeDefinition().create(true));

			return attrMgr;
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
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
	 * @since 5.0
	 */
	protected IPQueue getQueueDefault(IPResourceManager rm) {
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

		IResourceManagerControl rm = getResourceManager(configuration);

		final AbstractRMLaunchConfigurationFactory rmFactory = PTPLaunchPlugin.getDefault().getRMLaunchConfigurationFactory(rm);
		if (rmFactory == null) {
			return new IAttribute[0];
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = rmFactory.create(rm, null);
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
	 * @since 5.0
	 */
	protected IResourceManagerControl getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		String rmUniqueName = getResourceManagerUniqueName(configuration);
		IResourceManagerControl rm = PTPCorePlugin.getDefault().getModelManager().getResourceManagerFromUniqueName(rmUniqueName);
		if (rm.getState() == ResourceManagerAttributes.State.STARTED) {
			return rm;
		}
		return null;
	}

	/**
	 * Returns the (possible empty) list of synchronization rule objects
	 * according to the rules described in the configuration.
	 * 
	 * @since 5.0
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
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
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
			final IResourceManagerControl rm = getResourceManager(configuration);
			if (rm == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_No_ResourceManager));
			}
			rm.addJobListener(fJobListener);
			String jobId = rm.submitJob(configuration, attrMgr, progress.newChild(5));
			launch.setJobId(jobId);
			launch.setResourceManager(rm);
			JobSubmission jobSub = new JobSubmission(launch, attrMgr, debugger);
			synchronized (jobSubmissions) {
				jobSubmissions.put(jobId, jobSub);
			}
			jobSub.schedule();
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Verify the validity of the debugger path.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @throws CoreException
	 *             if the path is invalid or the monitor was canceled.
	 * @since 5.0
	 */
	protected void verifyDebuggerPath(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		String dbgPath = getDebuggerExePath(configuration);
		if (dbgPath == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_debuggerPathNotSpecified));
		}
		try {
			verifyResource(dbgPath, configuration, monitor);
			if (monitor.isCanceled()) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user, null));
			}
		} catch (CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
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
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return IPath representing path to the executable (either local or
	 *         remote)
	 * @throws CoreException
	 *             if the resource can't be found or the monitor was canceled.
	 * @since 5.0
	 */
	protected IPath verifyExecutablePath(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		if (getCopyExecutable(configuration)) {
			return new Path(getExecutablePath(configuration));
		} else {
			String exePath = getExecutablePath(configuration);
			try {
				IPath path = verifyResource(exePath, configuration, monitor);
				if (monitor.isCanceled()) {
					throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
							Messages.AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user, null));
				}
				return path;
			} catch (CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Application_file_does_not_exist,
						new FileNotFoundException(e.getLocalizedMessage())));
			}
		}
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
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Project_not_specified));
		}

		IProject project = getProject(proName);
		if (project == null || !project.exists() || !project.isOpen()) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Project_does_not_exist_or_is_not_a_project));
		}

		return project;
	}

	/**
	 * @param path
	 * @param configuration
	 * @return
	 * @throws CoreException
	 * @since 5.0
	 */
	protected IPath verifyResource(String path, ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		return PTPLaunchPlugin.getDefault().verifyResource(path, configuration, monitor);
	}

	/**
	 * Verify the working directory. If no working directory is specified, the
	 * default is the location of the executable.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return path of working directory
	 * @throws CoreException
	 *             if the working directory is invalid or the monitor was
	 *             canceled.
	 * @since 5.0
	 */
	protected String verifyWorkDirectory(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		IPath path;
		String workPath = getWorkDirectory(configuration);
		if (workPath == null) {
			path = verifyExecutablePath(configuration, monitor).removeLastSegments(1);
		} else {
			path = verifyResource(workPath, configuration, monitor);
		}
		if (monitor.isCanceled()) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Operation_cancelled_by_user, null));
		}
		return path.toString();
	}

}
