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
package org.eclipse.ptp.launch;

import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialogWithToggle;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PreferencesAdapter;
import org.eclipse.ptp.core.jobs.IJobListener;
import org.eclipse.ptp.core.jobs.IJobStatus;
import org.eclipse.ptp.core.jobs.JobManager;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.internal.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.internal.debug.core.launch.PLaunch;
import org.eclipse.ptp.internal.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.launch.rulesengine.IRuleAction;
import org.eclipse.ptp.launch.rulesengine.ISynchronizationRule;
import org.eclipse.ptp.launch.rulesengine.RuleActionFactory;
import org.eclipse.ptp.launch.rulesengine.RuleFactory;
import org.eclipse.ptp.rm.jaxb.control.core.ILaunchController;
import org.eclipse.ptp.rm.lml.monitor.core.IMonitorControl;
import org.eclipse.ptp.rm.lml.monitor.core.MonitorControlManager;
import org.eclipse.ptp.rm.lml.ui.ILMLUIConstants;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteFileManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;

/**
 * Abstract base class for launch configuration delegates that launch parallel jobs
 */
public abstract class AbstractParallelLaunchConfigurationDelegate extends LaunchConfigurationDelegate {

	private final class JobListener implements IJobListener {

		public void jobAdded(IJobStatus status) {
			// Nothing to do
		}

		public void jobChanged(IJobStatus status) {
			JobSubmission jobSub;
			synchronized (jobSubmissions) {
				jobSub = jobSubmissions.get(status.getJobId());
			}
			if (jobSub != null) {
				jobSub.statusChanged();
			}
		}
	}

	/**
	 * Wait for job to begin running, then perform post launch operations. The job is guaranteed not to be in the UNDETERMINED
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
		private final ILaunchController fLaunchControl;
		private final ReentrantLock fSubLock = new ReentrantLock();
		private final Condition fSubCondition = fSubLock.newCondition();

		public JobSubmission(IPLaunch launch, ILaunchController control, IPDebugger debugger) {
			super(launch.getJobId());
			fLaunch = launch;
			fLaunchControl = control;
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
			String jobId = fLaunch.getJobId();
			try {
				while (fLaunchControl.getJobStatus(jobId, subMon.newChild(50)).getState().equals(IJobStatus.SUBMITTED)
						&& !subMon.isCanceled()) {
					fSubLock.lock();
					try {
						fSubCondition.await(500, TimeUnit.MILLISECONDS);
					} catch (InterruptedException e) {
						// Expect to be interrupted if monitor is canceled
					} finally {
						fSubLock.unlock();
					}
				}
			} catch (CoreException e) {
				// Ignore
			}

			if (!subMon.isCanceled()) {
				doCompleteJobLaunch(fLaunch, fDebugger);

				try {
					while (!fLaunchControl.getJobStatus(jobId, subMon.newChild(50)).getState().equals(IJobStatus.COMPLETED)
							&& !subMon.isCanceled()) {
						fSubLock.lock();
						try {
							fSubCondition.await(1000, TimeUnit.MILLISECONDS);
						} catch (InterruptedException e) {
							// Expect to be interrupted if monitor is
							// canceled
						} finally {
							fSubLock.unlock();
						}
					}
				} catch (CoreException e) {
					// Ignore
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
							JobManager.getInstance().removeListener(fJobListener);
						}
					}
				}
			}
			return Status.OK_STATUS;
		}
	}

	/**
	 * @since 5.0
	 */
	public static IRemoteFileManager getLocalFileManager(ILaunchConfiguration configuration) throws CoreException {
		IRemoteServices localServices = RemoteServices.getLocalServices();
		IRemoteConnectionManager lconnMgr = localServices.getConnectionManager();
		IRemoteConnection lconn = lconnMgr.getConnection(IRemoteConnectionManager.LOCAL_CONNECTION_NAME);
		return lconn.getFileManager();
	}

	/**
	 * @param configuration
	 * @param monitor
	 * @return
	 * @throws CoreException
	 */
	public static IRemoteFileManager getRemoteFileManager(ILaunchConfiguration configuration, IProgressMonitor monitor)
			throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		IRemoteConnection conn = RMLaunchUtils.getRemoteConnection(configuration, progress.newChild(5));
		if (conn != null) {
			if (!conn.isOpen()) {
				conn.open(progress.newChild(5));
				if (!progress.isCanceled() && !conn.isOpen()) {
					throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
							Messages.AbstractParallelLaunchConfigurationDelegate_Connection_is_not_open));
				}
			}
			return conn.getFileManager();
		}
		return null;
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
	 * Check if the copy local file is enabled. If it is, copy the executable file from the local host to the remote host.
	 * 
	 * @param configuration
	 *            launch configuration
	 * @throws CoreException
	 *             if the copy fails or is cancelled
	 */
	protected void copyExecutable(ILaunchConfiguration configuration, IProgressMonitor monitor) throws CoreException {
		boolean copyExecutable = LaunchUtils.getCopyExecutable(configuration);

		if (copyExecutable) {
			// Get remote and local paths
			String remotePath = LaunchUtils.getExecutablePath(configuration);
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
	 * Copy a data from a path (can be a file or directory) from the remote host to the local host.
	 * 
	 * @param remotePath
	 * @param localPath
	 * @param configuration
	 * @throws CoreException
	 */
	protected void copyFileFromRemoteHost(String remotePath, String localPath, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 15);
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
	}

	/**
	 * Copy a data from a path (can be a file or directory) from the local host to the remote host.
	 * 
	 * @param localPath
	 * @param remotePath
	 * @param configuration
	 * @throws CoreException
	 */
	protected void copyFileToRemoteHost(String localPath, String remotePath, ILaunchConfiguration configuration,
			IProgressMonitor monitor) throws CoreException {
		SubMonitor progress = SubMonitor.convert(monitor, 15);
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
			SubMonitor progress = SubMonitor.convert(monitor, 10);
			// This faction generate action objects which execute according to
			// rules
			RuleActionFactory ruleActFactory = new RuleActionFactory(configuration, progress.newChild(10));

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
		}
	}

	/**
	 * Get the debugger configuration
	 * 
	 * @param configuration
	 *            launch configuration
	 * @return debugger configuration
	 * @throws CoreException
	 * @since 6.0
	 */
	protected IPDebugConfiguration getDebugConfig(ILaunchConfiguration config) throws CoreException {
		return PTPDebugCorePlugin.getDefault().getDebugConfiguration(LaunchUtils.getDebuggerID(config));
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
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
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
	 * @param mode
	 * @param launch
	 * @param debugger
	 * @param monitor
	 * @throws CoreException
	 * @since 5.0
	 */
	protected void submitJob(String mode, IPLaunch launch, IPDebugger debugger, IProgressMonitor progress) throws CoreException {
		SubMonitor subMon = SubMonitor.convert(progress, 50);
		final ILaunchController control = RMLaunchUtils.getLaunchController(launch.getLaunchConfiguration());
		if (control == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Specified_resource_manager_not_found));
		}
		/*
		 * Make sure controller is started before we submit the job
		 */
		control.start(subMon.newChild(10));

		if (!mode.equals(ILaunchManager.DEBUG_MODE) && LaunchUtils.getSystemType(launch.getLaunchConfiguration()) != null) {
			IMonitorControl monitor = MonitorControlManager.getInstance().getMonitorControl(control.getControlId());
			if (monitor == null) {
				if (switchPerspective(ILMLUIConstants.ID_SYSTEM_MONITORING_PERSPECTIVE,
						Messages.AbstractParallelLaunchConfigurationDelegate_launchType1,
						PreferenceConstants.PREF_SWITCH_TO_MONITORING_PERSPECTIVE, true)) {

					monitor = MonitorControlManager.getInstance().createMonitorControl(control);
					monitor.start(subMon.newChild(10));
				}
			} else {
				if (!monitor.isActive()) {
					if (switchPerspective(ILMLUIConstants.ID_SYSTEM_MONITORING_PERSPECTIVE,
							Messages.AbstractParallelLaunchConfigurationDelegate_launchType2,
							PreferenceConstants.PREF_SWITCH_TO_MONITORING_PERSPECTIVE, true)) {

						monitor.start(subMon.newChild(10));
					}
				} else {
					switchPerspective(ILMLUIConstants.ID_SYSTEM_MONITORING_PERSPECTIVE,
							Messages.AbstractParallelLaunchConfigurationDelegate_launchType3,
							PreferenceConstants.PREF_SWITCH_TO_MONITORING_PERSPECTIVE, false);
				}
			}
		}

		JobManager.getInstance().addListener(control.getControlId(), fJobListener);
		String jobId = control.submitJob(launch.getLaunchConfiguration(), mode, subMon.newChild(10));
		if (subMon.isCanceled()) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Launch_was_cancelled));
		}
		if (control.getJobStatus(jobId, subMon.newChild(10)).equals(IJobStatus.UNDETERMINED)) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_UnableToDetermineJobStatus));
		}
		launch.setJobControl(control);
		launch.setJobId(jobId);
		JobSubmission jobSub = new JobSubmission(launch, control, debugger);
		synchronized (jobSubmissions) {
			jobSubmissions.put(jobId, jobSub);
		}
		jobSub.schedule();
	}

	/**
	 * Used to force switching to the supplied perspective
	 * 
	 * @param perspectiveID
	 */
	protected boolean switchPerspective(final String perspectiveId, final String message, final String preferenceKey,
			final boolean alwaysDisplayMessage) {
		final boolean[] result = new boolean[1];
		result[0] = false;
		if (perspectiveId != null) {
			final Display display = Display.getDefault();
			if (display != null && !display.isDisposed()) {
				display.syncExec(new Runnable() {

					public void run() {
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						if (window != null) {
							IWorkbenchPage page = window.getActivePage();
							if (page != null) {
								if (!alwaysDisplayMessage && page.getPerspective().getId().equals(perspectiveId)) {
									result[0] = true;
									return;
								}

								IPreferenceStore store = new PreferencesAdapter(PTPLaunchPlugin.getUniqueIdentifier());
								result[0] = !store.getString(PreferenceConstants.PREF_SWITCH_TO_MONITORING_PERSPECTIVE).equals(
										MessageDialogWithToggle.NEVER);
								if (store.getString(PreferenceConstants.PREF_SWITCH_TO_MONITORING_PERSPECTIVE).equals(
										MessageDialogWithToggle.PROMPT)) {
									MessageDialogWithToggle dialog = MessageDialogWithToggle.openYesNoQuestion(
											display.getActiveShell(),
											Messages.AbstractParallelLaunchConfigurationDelegate_ConfirmActions, message, null,
											false, store, preferenceKey);
									result[0] = (dialog.getReturnCode() == IDialogConstants.YES_ID);
								}

								if (result[0]) {
									try {
										PlatformUI.getWorkbench().showPerspective(perspectiveId, window);
									} catch (WorkbenchException e) {
										// Ignore
									}
								}
							}
						}
					}
				});
			}
		}
		return result[0];
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
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_debuggerPathNotSpecified));
		}
		try {
			return verifyResource(dbgPath, configuration, monitor);
		} catch (CoreException e) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
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
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
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
		String proName = LaunchUtils.getProjectName(configuration);
		if (proName == null) {
			throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
					Messages.AbstractParallelLaunchConfigurationDelegate_Project_not_specified));
		}

		IProject project = LaunchUtils.getProject(proName);
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
		SubMonitor progress = SubMonitor.convert(monitor, 10);
		IRemoteFileManager fileManager = getRemoteFileManager(configuration, progress.newChild(5));
		if (!monitor.isCanceled()) {
			if (fileManager == null) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.AbstractParallelLaunchConfigurationDelegate_unableToObtainConnectionInfo));
			}
			boolean exists = fileManager.getResource(path).fetchInfo(EFS.NONE, progress.newChild(5)).exists();
			if (!progress.isCanceled()) {
				if (!exists) {
					throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), NLS.bind(
							Messages.AbstractParallelLaunchConfigurationDelegate_Path_not_found, new Object[] { path })));
				}
				return new Path(path);
			}
		}
		return null;
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
