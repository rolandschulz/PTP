/*******************************************************************************
 * Copyright (c) 2012 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rm.launch;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.resources.IProject;
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
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.jobs.IJobAddedEvent;
import org.eclipse.ptp.core.jobs.IJobChangedEvent;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.LaunchUtils;
import org.eclipse.ptp.launch.rulesengine.IRuleAction;
import org.eclipse.ptp.launch.rulesengine.ISynchronizationRule;
import org.eclipse.ptp.launch.rulesengine.RuleActionFactory;
import org.eclipse.ptp.launch.rulesengine.RuleFactory;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.rm.jaxb.control.IJAXBLaunchControl;
import org.eclipse.ptp.rm.launch.internal.messages.Messages;

/**
 *
 */
public abstract class AbstractParallelLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	private final class JobListener implements IJobListener {
		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.listeners.IJobListener#handleEvent(org.eclipse.ptp.core.events.IJobAddedEvent)
		 */
		@Override
		public void handleEvent(IJobAddedEvent e) {
			// TODO Auto-generated method stub

		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see org.eclipse.ptp.core.listeners.IJobListener#handleEvent(org.eclipse .ptp.core.events.IJobChangeEvent)
		 */
		@Override
		public void handleEvent(IJobChangedEvent e) {
			JobSubmission jobSub;
			synchronized (jobSubmissions) {
				jobSub = jobSubmissions.get(e.getJobStatus().getJobId());
			}
			if (jobSub != null) {
				jobSub.statusChanged();
			}
		}
	}

	/**
	 * Wait for job to begin running, then perform post launch operations. The job is guaranteed not to be in the UNDERTERMINED
	 * state.
	 * 
	 * <pre>
	 * Job state transition is:
	 * 
	 *  SUBMITTED ----> RUNNING ----> COMPLETED
	 *             ^              |
	 *             |- SUSPENDED <-|
	 * </pre>
	 * 
	 * We must call completion method when job state is RUNNING, however it is possible that the job may get to COMPLETED or
	 * SUSPENDED before we are started. If either of these states is reached, assume that RUNNING has also been reached.
	 */
	private class JobSubmission extends Job {
		private final IPLaunch fLaunch;
		private final IPDebugger fDebugger;
		private final IJobControl fJobControl;
		private final ReentrantLock fSubLock = new ReentrantLock();
		private final Condition fSubCondition = fSubLock.newCondition();

		public JobSubmission(IPLaunch launch, IJobControl control, IPDebugger debugger) {
			super(launch.getJobId());
			fLaunch = launch;
			fJobControl = control;
			fDebugger = debugger;
			setSystem(true);
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
		 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime. IProgressMonitor)
		 */
		@Override
		protected IStatus run(IProgressMonitor monitor) {
			SubMonitor subMon = SubMonitor.convert(monitor, 100);
			try {
				String jobId = fLaunch.getJobId();
				fSubLock.lock();
				try {
					while (fJobControl.getJobStatus(jobId, subMon.newChild(50)).getState().equals(IJobStatus.SUBMITTED)
							&& !subMon.isCanceled()) {
						try {
							fSubCondition.await(100, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							// Expect to be interrupted if monitor is canceled
						}
					}
				} catch (CoreException e) {
				} finally {
					fSubLock.unlock();
				}

				if (!subMon.isCanceled()) {
					doCompleteJobLaunch(fLaunch, fDebugger);

					fSubLock.lock();
					try {
						while (!fJobControl.getJobStatus(jobId, subMon.newChild(50)).getState().equals(IJobStatus.COMPLETED)
								&& !subMon.isCanceled()) {
							try {
								fSubCondition.await(1000, TimeUnit.MILLISECONDS);
							} catch (InterruptedException e) {
								// Expect to be interrupted if monitor is
								// canceled
							}
						}
					} catch (CoreException e) {
					} finally {
						fSubLock.unlock();
					}

					if (!subMon.isCanceled()) {
						/*
						 * When the job terminates, do any post launch data synchronization.
						 */
						// If needed, copy data back.
						try {
							// Get the list of paths to be copied back.
							doPostLaunchSynchronization(fLaunch.getLaunchConfiguration());
						} catch (CoreException e) {
							RMLaunchPlugin.log(e);
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
								JobManager.getInstance().removeListener(fJobListener);
							}
						}
					}
				}
				return Status.OK_STATUS;
			} finally {
				if (monitor != null) {
					monitor.done();
				}
			}
		}
	}

	/*
	 * Model listeners
	 */
	private final IJobListener fJobListener = new JobListener();

	/*
	 * HashMap used to keep track of job submissions
	 */
	protected Map<String, JobSubmission> jobSubmissions = Collections.synchronizedMap(new HashMap<String, JobSubmission>());

	/**
	 * Constructor
	 */
	public AbstractParallelLaunchConfigurationDelegate() {
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getLaunch(org .eclipse.debug.core.ILaunchConfiguration,
	 * java.lang.String)
	 */
	@Override
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new PLaunch(configuration, mode, null);
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
	 * This method is called when the job state changes to RUNNING. This allows the launcher to complete the job launch.
	 * 
	 * @param launch
	 * @param debugger
	 * @since 5.0
	 */
	protected abstract void doCompleteJobLaunch(IPLaunch launch, IPDebugger debugger);

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
			RuleActionFactory ruleActFactory = new RuleActionFactory(configuration, new NullProgressMonitor());

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
			RuleActionFactory ruleActFactory = new RuleActionFactory(configuration, monitor);

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
	 * Returns the (possible empty) list of synchronization rule objects according to the rules described in the configuration.
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
				throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Error_converting_rules));
			}
		}

		return result.toArray(new ISynchronizationRule[result.size()]);
	}

	/**
	 * Create a source locator from the ID specified in the configuration, or create a default one if it hasn't been specified.
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
	 * Submit a job to the resource manager. Keeps track of the submission so we know when the job actually starts running. When
	 * this happens, the abstract method doCompleteJobLaunch() is invoked.
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param debugger
	 * @param monitor
	 * @throws CoreException
	 * @since 5.0
	 */
	protected void submitJob(ILaunchConfiguration configuration, String mode, IPLaunch launch, IPDebugger debugger,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		try {
			final IJAXBLaunchControl control = RMLaunchUtils.getLaunchControl(configuration);
			if (control == null) {
				throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Specified_resource_manager_not_found));
			}
			control.start(progress.newChild(3));
			JobManager.getInstance().addListener(control.getControlId(), fJobListener);
			String jobId = control.submitJob(configuration, mode, progress.newChild(5));
			if (control.getJobStatus(jobId, progress.newChild(2)).equals(IJobStatus.UNDETERMINED)) {
				throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_UnableToDetermineJobStatus));
			}
			launch.setJobControl(control);
			launch.setJobId(jobId);
			JobSubmission jobSub = new JobSubmission(launch, control, debugger);
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
	protected IPath verifyDebuggerPath(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		String dbgPath = LaunchUtils.getDebuggerExePath(configuration);
		if (dbgPath == null) {
			throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_debuggerPathNotSpecified));
		}
		try {
			return verifyResource(dbgPath, configuration, monitor);
		} catch (CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Debugger_path_not_found, new FileNotFoundException(
							e.getLocalizedMessage())));
		}
	}

	/**
	 * Verify the validity of executable path. If the executable is to be copied, then no additional verification is required.
	 * Otherwise, the path must point to an existing file.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return IPath representing path to the executable (either local or remote)
	 * @throws CoreException
	 *             if the resource can't be found or the monitor was canceled.
	 * @since 5.0
	 */
	protected IPath verifyExecutablePath(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		if (LaunchUtils.getCopyExecutable(configuration)) {
			return new Path(LaunchUtils.getExecutablePath(configuration));
		} else {
			String exePath = LaunchUtils.getExecutablePath(configuration);
			try {
				return verifyResource(exePath, configuration, monitor);
			} catch (CoreException e) {
				throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_Application_file_does_not_exist,
						new FileNotFoundException(e.getLocalizedMessage())));
			}
		}
	}

	/**
	 * @since 5.0
	 */
	protected boolean verifyLaunchAttributes(final ILaunchConfiguration configuration, String mode, final IProgressMonitor monitor)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 30);

		try {
			/*
			 * Verify executable path
			 */
			IPath path = verifyExecutablePath(configuration, progress.newChild(10));
			if (progress.isCanceled() || path == null) {
				return false;
			}

			/*
			 * Verify working directory. Use the executable path if no working directory has been set.
			 */
			String workPath = LaunchUtils.getWorkingDirectory(configuration);
			if (workPath != null) {
				path = verifyResource(workPath, configuration, progress.newChild(10));
				if (progress.isCanceled() || path == null) {
					return false;
				}
			}

			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				path = verifyDebuggerPath(configuration, progress.newChild(10));
				if (progress.isCanceled() || path == null) {
					return false;
				}
			}
			return true;
		} finally {
			if (monitor != null) {
				monitor.done();
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
		String proName = LaunchUtils.getProjectName(configuration);
		if (proName == null) {
			throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Project_not_specified));
		}

		IProject project = LaunchUtils.getProject(proName);
		if (project == null || !project.exists() || !project.isOpen()) {
			throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(),
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
		IRemoteFileManager fileManager = RMLaunchUtils.getRemoteFileManager(configuration, monitor);
		if (monitor.isCanceled() || fileManager == null) {
			return null;
		}
		if (!fileManager.getResource(path).fetchInfo().exists()) {
			throw new CoreException(new Status(IStatus.ERROR, RMLaunchPlugin.getUniqueIdentifier(), NLS.bind(
					Messages.AbstractParallelLaunchConfigurationDelegate_Path_not_found, new Object[] { path })));
		}
		return new Path(path);
	}

	/**
	 * Verify the working directory. If no working directory is specified, the default is the location of the executable.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @param monitor
	 *            progress monitor
	 * @return path of working directory or null if the monitor was canceled
	 * @throws CoreException
	 *             if the working directory is invalid.
	 * @since 5.0
	 */
	protected IPath verifyWorkDirectory(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		String workPath = LaunchUtils.getWorkingDirectory(configuration);
		if (workPath == null) {
			return verifyExecutablePath(configuration, monitor).removeLastSegments(1);
		}
		return verifyResource(workPath, configuration, monitor);
	}

}
