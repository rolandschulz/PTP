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
package org.eclipse.ptp.launch.internal;

import java.text.MessageFormat;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPUniverse;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.ui.IPTPDebugUIConstants;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;
import org.eclipse.ptp.ui.IPTPUIConstants;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.WorkbenchException;
/**
 * 
 */
public class ParallelLaunchConfigurationDelegate extends AbstractParallelLaunchConfigurationDelegate {
	/*
	 * protected IPath getProgramPath(ILaunchConfiguration configuration) throws CoreException { String path = getProgramName(configuration); if (path == null) { return null; } return new Path(path); } protected IPath verifyProgramPath(ILaunchConfiguration config) throws CoreException { IProject project = verifyProject(config); IPath programPath = getProgramPath(config); if (programPath == null || programPath.isEmpty()) { return null; } if (!programPath.isAbsolute()) { programPath =
	 * project.getFile(programPath).getLocation(); } if (!programPath.toFile().exists()) { abort(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Program_file_does_not_exist"), new FileNotFoundException(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.PROGRAM_PATH_not_found")), IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST); } return programPath; } protected IProject getProject(ILaunchConfiguration configuration) throws CoreException { String projectName =
	 * getProjectName(configuration); if (projectName != null) { projectName = projectName.trim(); if (projectName.length() > 0) { return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName); // ICProject cProject = // CCorePlugin.getDefault().getCoreModel().create(project); // if (cProject != null && cProject.exists()) { // return cProject; // } } } return null; }
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		if (!(launch instanceof IPLaunch)) {
			abort(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Invalid_launch_object"), null, 0);
		}
		IPLaunch pLaunch = (IPLaunch) launch;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("", 250);
		monitor.setTaskName(MessageFormat.format("{0} . . .", new String[] { "Launching " + configuration.getName() }));
		if (monitor.isCanceled()) {
			return;
		}
		IAbstractDebugger debugger = null;
		IPJob job = null;
		// done the verification phase
		JobRunConfiguration jrunconfig = getJobRunConfiguration(configuration);
		/* Assuming we have parsed the configuration */
		try {
			IPreferenceStore store = PTPDebugUIPlugin.getDefault().getPreferenceStore();
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				monitor.subTask("Configuring debug setting . . .");
				String dbgFile = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_FILE);
				String dbgArgs = "--host=" + store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_HOST);
				dbgArgs += " --debugger=" + store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND);
				String dbgPath = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND_PATH);
				if (dbgPath.length() > 0)
					dbgArgs += " --debugger_path=" + dbgPath;
				String dbgExtraArgs = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_ARGS);
				if (dbgExtraArgs.length() > 0)
					dbgArgs += " " + dbgExtraArgs;
				verifyDebuggerPath(dbgFile);
				IPDebugConfiguration debugConfig = getDebugConfig(configuration);
				debugger = debugConfig.createDebugger();
				jrunconfig.setDebuggerPath(dbgFile);
				dbgArgs += " --port=" + debugger.getDebuggerPort();
				jrunconfig.setDebuggerArgs(dbgArgs);
				jrunconfig.setDebug();
			}
			monitor.worked(10);
			monitor.subTask("Starting the job . . .");
			final String resourceManagerName = jrunconfig.getResourceManagerName();
			final IResourceManager launchManager = getLaunchManager(resourceManagerName);
			if (launchManager == null) {
				abort(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.No_ResourceManager"), null, 0);
			}
			job = launchManager.run(launch, jrunconfig, new SubProgressMonitor(monitor, 150));
			launch.setAttribute("JOB_ID", job.getIDString());
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				// show ptp debug view
				showPTPDebugView(IPTPDebugUIConstants.ID_VIEW_PARALLELDEBUG);
				// Switch the perspective
				// switchPerspectiveTo(DebugUITools.getLaunchPerspective(configuration.getType(), mode));
				monitor.setTaskName("Starting the debugger . . .");
				String dbgExe = getDebuggerExePath(configuration);
				if (dbgExe != null) {
					// remote setting
					IPath dbgExePath = new Path(dbgExe);
					job.setAttribute(PreferenceConstants.JOB_APP_NAME, dbgExePath.lastSegment());
					job.setAttribute(PreferenceConstants.JOB_APP_PATH, dbgExePath.removeLastSegments(1).toOSString());
				} else {
					job.setAttribute(PreferenceConstants.JOB_APP_NAME, jrunconfig.getExecName());
					job.setAttribute(PreferenceConstants.JOB_APP_PATH, jrunconfig.getPathToExec());
				}
				String wd = getDebuggerWorkDirectory(configuration);
				if (wd != null) {
					job.setAttribute(PreferenceConstants.JOB_WORK_DIR, wd);
				} else {
					job.setAttribute(PreferenceConstants.JOB_WORK_DIR, jrunconfig.getWorkingDir());
				}
				job.setAttribute(PreferenceConstants.JOB_ARGS, jrunconfig.getArguments());
				// job.setAttribute(PreferenceConstants.JOB_DEBUG_DIR, exePath.removeLastSegments(1).toOSString());
				job.setAttribute(PreferenceConstants.JOB_DEBUG_DIR, jrunconfig.getPathToExec());
				pLaunch.setPJob(job);
				IBinaryObject exeFile = verifyBinary(configuration);
				setDefaultSourceLocator(launch, configuration);
				int timeout = store.getInt(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT);
				PTPDebugCorePlugin.getDebugModel().createDebuggerSession(debugger, pLaunch, exeFile, timeout, new SubProgressMonitor(monitor, 40));
				monitor.worked(10);
				if (monitor.isCanceled()) {
					PTPDebugCorePlugin.getDebugModel().shutdownSession(job);
				}
			} else {
				// showPTPDebugView(IPTPUIConstants.VIEW_PARALLELJOB);
				new RuntimeProcess(pLaunch, job, null);
				monitor.worked(40);
			}
		} catch (CoreException e) {
			if (e.getStatus().getPlugin().equals(PTPCorePlugin.PLUGIN_ID)) {
				String msg = e.getMessage();
				if (msg == null)
					msg = "";
				else
					msg = msg + "\n\n";
				abort(msg + LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Control_system_does_not_exist"), null, 0);
			}
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				PTPDebugCorePlugin.getDebugModel().shutdownSession(job);
				/*
				if (debugger != null) {
					debugger.stopDebugger();
				}
				*/
			}
			if (e.getStatus().getCode() != IStatus.CANCEL) {
				throw e;
			}
		} finally {
			monitor.done();
		}
	}
	private IResourceManager getLaunchManager(String rmName) {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		for (int i = 0; i < rms.length; ++i) {
			if (rms[i].getElementName().equals(rmName)) {
				return rms[i];
			}
		}
		return null;
	}
	private void switchPerspectiveTo(final String perspectiveID) {
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
						if (!page.getPerspective().getId().equals(perspectiveID)) {
							try {
								window.getWorkbench().showPerspective(perspectiveID, window);
							} catch (WorkbenchException e) {
								e.printStackTrace();
							}
						}
					}
				}
			});
		}
	}
	private void showPTPDebugView(final String viewID) {
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
							} catch (PartInitException e) {}
						}
					}
				}
			});
		}
	}
}
