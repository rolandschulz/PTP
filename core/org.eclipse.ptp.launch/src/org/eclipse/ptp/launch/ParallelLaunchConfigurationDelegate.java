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

import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.jobs.IJobControl;
import org.eclipse.ptp.core.util.LaunchUtils;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.launch.internal.RuntimeProcess;
import org.eclipse.ptp.launch.internal.messages.Messages;
import org.eclipse.ptp.ui.model.IElementHandler;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;

/**
 * A launch configuration delegate for launching jobs via the PTP resource manager mechanism.
 */
public class ParallelLaunchConfigurationDelegate extends AbstractParallelLaunchConfigurationDelegate {
	private class DebuggerSession implements IRunnableWithProgress {
		private final String fJobId;
		private final IPLaunch fLaunch;
		private final IProject fProject;
		private final IPDebugger fDebugger;

		public DebuggerSession(String jobId, IPLaunch launch, IProject project, IPDebugger debugger) {
			fJobId = jobId;
			fLaunch = launch;
			fProject = project;
			fDebugger = debugger;
		}

		public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
			monitor.beginTask(Messages.ParallelLaunchConfigurationDelegate_5, 10);
			SubMonitor subMon = SubMonitor.convert(monitor, 10);
			try {
				IPSession session = PTPDebugCorePlugin.getDebugModel().createDebugSession(fDebugger, fLaunch, fProject,
						subMon.newChild(2));

				/*
				 * NOTE: we assume these have already been verified prior to launch
				 */
				String app = LaunchUtils.getProgramName(fLaunch.getLaunchConfiguration());
				String path = LaunchUtils.getProgramPath(fLaunch.getLaunchConfiguration());
				String cwd = LaunchUtils.getWorkingDirectory(fLaunch.getLaunchConfiguration());
				String[] args = LaunchUtils.getProgramArguments(fLaunch.getLaunchConfiguration());

				switchPerspective(IPTPDebugUIConstants.ID_PERSPECTIVE_DEBUG,
						Messages.ParallelLaunchConfigurationDelegate_OpenDebugPerspective,
						PreferenceConstants.PREF_SWITCH_TO_DEBUG_PERSPECTIVE, false);

				session.connectToDebugger(subMon.newChild(8), app, path, cwd, args);
			} catch (CoreException e) {
				PTPDebugCorePlugin.getDebugModel().shutdownSession(fJobId);
				throw new InvocationTargetException(e, e.getLocalizedMessage());
			} finally {
				monitor.done();
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.AbstractParallelLaunchConfigurationDelegate#
	 * doCleanupLaunch(org.eclipse.ptp.debug.core.launch.IPLaunch)
	 */

	@Override
	protected void doCleanupLaunch(IPLaunch launch) {
		if (launch.getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
			try {
				terminateDebugSession(launch.getJobId());
				IPDebugConfiguration debugConfig = getDebugConfig(launch.getLaunchConfiguration());
				IPDebugger debugger = debugConfig.getDebugger();
				debugger.cleanup(launch);
			} catch (CoreException e) {
				PTPLaunchPlugin.log(e);
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.launch.AbstractParallelLaunchConfigurationDelegate#
	 * doCompleteJobLaunch(org.eclipse.ptp.debug.core.launch.IPLaunch, org.eclipse.ptp.debug.core.IPDebugger)
	 */

	@Override
	protected void doCompleteJobLaunch(final IPLaunch launch, final IPDebugger debugger) {
		final String jobId = launch.getJobId();
		final ILaunchConfiguration configuration = launch.getLaunchConfiguration();

		/*
		 * Create process that is used by the DebugPlugin for handling console output. This process gets added to the debug session
		 * so that it is also displayed in the Debug View as the system process.
		 */
		new RuntimeProcess(launch, null);

		if (launch.getLaunchMode().equals(ILaunchManager.DEBUG_MODE)) {
			try {
				setDefaultSourceLocator(launch, configuration);
				final IProject project = verifyProject(configuration);

				final DebuggerSession session = new DebuggerSession(jobId, launch, project, debugger);
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						try {
							new ProgressMonitorDialog(PTPLaunchPlugin.getActiveWorkbenchShell()).run(true, true, session);
						} catch (InterruptedException e) {
							terminateJob(launch);
						} catch (InvocationTargetException e) {
							PTPLaunchPlugin.errorDialog(Messages.ParallelLaunchConfigurationDelegate_0, e.getTargetException());
							PTPLaunchPlugin.log(e.getCause());
							terminateJob(launch);
						}
					}
				});
			} catch (final CoreException e) {
				/*
				 * Completion of launch fails, then terminate the job and display error message.
				 */
				Display.getDefault().asyncExec(new Runnable() {

					public void run() {
						PTPLaunchPlugin.errorDialog(Messages.ParallelLaunchConfigurationDelegate_1, e.getStatus());
						PTPLaunchPlugin.log(e);
						terminateJob(launch);
					}
				});
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org. eclipse.debug.core.ILaunchConfiguration,
	 * java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */

	public void launch(final ILaunchConfiguration configuration, String mode, ILaunch launch, final IProgressMonitor monitor)
			throws CoreException {
		try {
			if (!(launch instanceof IPLaunch)) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(),
						Messages.ParallelLaunchConfigurationDelegate_Invalid_launch_object));
			}
			SubMonitor progress = SubMonitor.convert(monitor, 110);
			//		progress.beginTask("", 25); //$NON-NLS-1$
			progress.setTaskName(NLS.bind(Messages.ParallelLaunchConfigurationDelegate_3, configuration.getName()));
			progress.setWorkRemaining(90);
			if (progress.isCanceled()) {
				return;
			}

			progress.worked(10);
			progress.subTask(Messages.ParallelLaunchConfigurationDelegate_4);

			if (!verifyLaunchAttributes(configuration, mode, progress.newChild(10)) || progress.isCanceled()) {
				return;
			}

			// All copy pre-"job submission" occurs here
			copyExecutable(configuration, progress.newChild(10));
			if (progress.isCanceled()) {
				return;
			}

			doPreLaunchSynchronization(configuration, progress.newChild(10));
			if (progress.isCanceled()) {
				return;
			}

			IPDebugger debugger = null;

			try {
				if (mode.equals(ILaunchManager.DEBUG_MODE)) {
					/*
					 * Show ptp debug view
					 */
					showPTPDebugView(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
					progress.subTask(Messages.ParallelLaunchConfigurationDelegate_6);

					/*
					 * Create the debugger extension, then the connection point for the debug server. The debug server is launched
					 * via the submitJob() command.
					 */

					IPDebugConfiguration debugConfig = getDebugConfig(configuration);
					debugger = debugConfig.getDebugger();
					debugger.initialize(configuration, progress.newChild(10));
					if (progress.isCanceled()) {
						return;
					}
				}

				progress.worked(10);
				progress.subTask(Messages.ParallelLaunchConfigurationDelegate_7);

				submitJob(mode, (IPLaunch) launch, debugger, progress.newChild(40));

				progress.worked(10);
			} catch (CoreException e) {
				if (debugger != null) {
					debugger.cleanup((IPLaunch) launch);
				}
				if (e.getStatus().getCode() != IStatus.CANCEL) {
					throw e;
				}
			}
		} finally {
			if (monitor != null) {
				monitor.done();
			}
		}
	}

	/**
	 * Show the PTP Debug view
	 * 
	 * @param viewID
	 */
	protected void showPTPDebugView(final String viewID) {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		if (display != null && !display.isDisposed()) {
			display.syncExec(new Runnable() {

				public void run() {
					IWorkbenchWindow window = PTPLaunchPlugin.getActiveWorkbenchWindow();
					if (window != null) {
						IWorkbenchPage page = window.getActivePage();
						if (page != null) {
							try {
								page.showView(viewID, null, IWorkbenchPage.VIEW_CREATE);
							} catch (PartInitException e) {
							}
						}
					}
				}
			});
		}
	}

	/**
	 * Terminates a debug session
	 * 
	 * @param jobId
	 *            id of the session to terminate
	 * @throws CoreException
	 */
	private void terminateDebugSession(String jobId) throws CoreException {
		PDebugModel model = PTPDebugCorePlugin.getDebugModel();
		IPSession session = model.getSession(jobId);
		if (session != null) {
			TaskSet tasks = model.getTasks(session, IElementHandler.SET_ROOT_ID);
			try {
				session.getPDISession().terminate(tasks);
			} catch (PDIException e) {
				throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), IStatus.ERROR,
						e.getMessage(), null));
			}
		}
	}

	/**
	 * Terminate a job.
	 * 
	 * @param job
	 *            job to terminate
	 */
	private void terminateJob(IPLaunch launch) {
		try {
			launch.getJobControl().control(launch.getJobId(), IJobControl.TERMINATE_OPERATION, null);
		} catch (CoreException e1) {
			// Ignore, but log
			PTPLaunchPlugin.log(e1);
		}
	}

}
