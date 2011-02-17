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
import org.eclipse.debug.ui.DebugUITools;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.core.elements.attributes.ElementAttributes;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugger;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.launch.internal.RuntimeProcess;
import org.eclipse.ptp.launch.messages.Messages;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;

/**
 * A launch configuration delegate for launching jobs via the PTP resource
 * manager mechanism.
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
				 * NOTE: we assume these have already been verified prior to
				 * launch
				 */
				String app = getProgramName(fLaunch.getLaunchConfiguration());
				String path = getProgramPath(fLaunch.getLaunchConfiguration());
				String cwd = getWorkingDirectory(fLaunch.getLaunchConfiguration());
				String[] args = getProgramArguments(fLaunch.getLaunchConfiguration());

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
	 * @see
	 * org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.
	 * eclipse.debug.core.ILaunchConfiguration, java.lang.String,
	 * org.eclipse.debug.core.ILaunch,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor)
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

			verifyLaunchAttributes(configuration, mode, progress.newChild(10));

			// All copy pre-"job submission" occurs here
			copyExecutable(configuration, progress.newChild(10));
			doPreLaunchSynchronization(configuration, progress.newChild(10));

			// switch perspective
			switchPerspective(DebugUITools.getLaunchPerspective(configuration.getType(), mode));

			IPDebugger debugger = null;

			try {
				if (mode.equals(ILaunchManager.DEBUG_MODE)) {
					// show ptp debug view
					showPTPDebugView(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
					progress.subTask(Messages.ParallelLaunchConfigurationDelegate_6);

					/*
					 * Create the debugger extension, then the connection point
					 * for the debug server. The debug server is launched via
					 * the submitJob() command.
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

				submitJob(configuration, mode, (IPLaunch) launch, debugger, progress.newChild(40));

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
	 * Terminate a job.
	 * 
	 * @param job
	 *            job to terminate
	 */
	private void terminateJob(final IResourceManagerControl rm, final String jobId) {
		try {
			rm.control(jobId, IResourceManagerControl.TERMINATE_OPERATION, null);
		} catch (CoreException e1) {
			// Ignore, but log
			PTPLaunchPlugin.log(e1);
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
	 * doCompleteJobLaunch(org.eclipse.ptp.debug.core.launch.IPLaunch,
	 * org.eclipse.ptp.debug.core.IPDebugger)
	 */
	@Override
	protected void doCompleteJobLaunch(final IPLaunch launch, final IPDebugger debugger) {
		final String jobId = launch.getJobId();
		final IResourceManagerControl rm = launch.getResourceManager();
		final ILaunchConfiguration configuration = launch.getLaunchConfiguration();

		/*
		 * Used by org.eclipse.ptp.ui.IJobManager#removeJob
		 */
		launch.setAttribute(ElementAttributes.getIdAttributeDefinition().getId(), jobId);

		/*
		 * Create process that is used by the DebugPlugin for handling console
		 * output. This process gets added to the debug session so that it is
		 * also displayed in the Debug View as the system process.
		 */
		new RuntimeProcess(launch, rm, jobId, null);

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
							terminateJob(rm, jobId);
						} catch (InvocationTargetException e) {
							PTPLaunchPlugin.errorDialog(Messages.ParallelLaunchConfigurationDelegate_0, e.getTargetException());
							PTPLaunchPlugin.log(e.getCause());
							terminateJob(rm, jobId);
						}
					}
				});
			} catch (final CoreException e) {
				/*
				 * Completion of launch fails, then terminate the job and
				 * display error message.
				 */
				Display.getDefault().asyncExec(new Runnable() {
					public void run() {
						PTPLaunchPlugin.errorDialog(Messages.ParallelLaunchConfigurationDelegate_1, e.getStatus());
						PTPLaunchPlugin.log(e);
						terminateJob(rm, jobId);
					}
				});
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
	 * Used to force switching to the PTP Debug perspective
	 * 
	 * @param perspectiveID
	 */
	protected void switchPerspective(final String perspectiveID) {
		if (perspectiveID != null) {
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
								if (page.getPerspective().getId().equals(perspectiveID))
									return;

								try {
									window.getWorkbench().showPerspective(perspectiveID, window);
								} catch (WorkbenchException e) {
								}
							}
						}
					}
				});
			}
		}
	}
}
