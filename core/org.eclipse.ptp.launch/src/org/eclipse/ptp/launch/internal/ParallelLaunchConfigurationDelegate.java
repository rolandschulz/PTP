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

import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

/**
 * 
 */
public class ParallelLaunchConfigurationDelegate extends AbstractParallelLaunchConfigurationDelegate {
	private IBinaryObject verifyBinary(IProject project, IPath exePath) throws CoreException {
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
		Throwable exception = new FileNotFoundException(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Program_is_not_a_recongnized_executable"));
		int code = IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY;
		MultiStatus status = new MultiStatus(PTPCorePlugin.getUniqueIdentifier(), code, LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Program_is_not_a_recongnized_executable"), exception);
		status.add(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), code, exception == null ? "" : exception.getLocalizedMessage(), exception));
		throw new CoreException(status);
	}
	private static IPath getProgramPath(ILaunchConfiguration configuration) throws CoreException {
		String path = getProgramName(configuration);
		if (path == null) {
			return null;
		}
		return new Path(path);
	}
	private IPath verifyProgramPath(ILaunchConfiguration config) throws CoreException {
		IProject project = verifyProject(config);
		IPath programPath = getProgramPath(config);
		if (programPath == null || programPath.isEmpty()) {
			return null;
		}
		if (!programPath.isAbsolute()) {
			programPath = project.getFile(programPath).getLocation();
		}
		if (!programPath.toFile().exists()) {
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Program_file_does_not_exist"), new FileNotFoundException(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.PROGRAM_PATH_not_found")), IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
		return programPath;
	}
	public static IProject getProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				return ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				//ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				//if (cProject != null && cProject.exists()) {
					//return cProject;
				//}
			}
		}
		return null;
	}
	private IPDebugConfiguration getDebugConfig(ILaunchConfiguration config) throws CoreException {
		IPDebugConfiguration dbgCfg = null;
		try {
			dbgCfg = PTPDebugCorePlugin.getDefault().getDebugConfiguration(getDebuggerID(config));
		} catch (CoreException e) {
			System.out.println("ParallelLaunchConfigurationDelegate.getDebugConfig() Error");
			throw e;
		}
		return dbgCfg;
	}
	private void verifyDebuggerPath(String path) throws CoreException {
		IPath programPath = new Path(path);
		if (programPath == null || programPath.isEmpty() || !programPath.toFile().exists()) {
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Debugger_path_not_found"), new FileNotFoundException(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Debugger_path_not_found")), IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
	}
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IBinaryObject exeFile = null;
		if (monitor == null) {
			monitor = new NullProgressMonitor();
		}
		monitor.beginTask("",  250);
		monitor.setTaskName(MessageFormat.format("{0} . . .", new String[] { "Launching " + configuration.getName() }));
		if (monitor.isCanceled()) {
			return;
		}
		PTPCorePlugin.getDefault().getModelManager().refreshRuntimeSystems(new SubProgressMonitor(monitor, 50), false);
		
		// Switch the perspective
		// LaunchUtils.switchPerspectiveTo(LaunchUtils.PPerspectiveFactory_ID);
		IAbstractDebugger debugger = null;
		IPJob job = null;
		
		// done the verification phase
		JobRunConfiguration jrunconfig = getJobRunConfiguration(configuration);
		/* Assuming we have parsed the configuration */
		
		IPath exePath = null;
		
		try {
			exePath = verifyProgramPath(configuration);
			IProject project = verifyProject(configuration);
			if (exePath != null) {
				exeFile = verifyBinary(project, exePath);
			}
		} catch(CoreException e) {
			abort(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Invalid_binary"), null, 0);
		}
		
		try {
			IPreferenceStore store = PTPDebugUIPlugin.getDefault().getPreferenceStore();
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				monitor.subTask("Configuring debug setting . . .");
				String dbgFile = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_FILE);
				String dbgArgs = "--host=" + store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_HOST);
				dbgArgs += " --debugger=" + store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND);
				String dbgPath = store.getString(IPDebugConstants.PREF_PTP_DEBUGGER_BACKEND_PATH);
				if (dbgPath.length() > 0)
					dbgArgs += " --debugger_path="+dbgPath;

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
			job = getLaunchManager().run(launch, jrunconfig, new SubProgressMonitor(monitor, 150));
			
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				monitor.setTaskName("Starting the debugger . . .");
				job.setAttribute(PreferenceConstants.JOB_APP_NAME, jrunconfig.getExecName());
				job.setAttribute(PreferenceConstants.JOB_APP_PATH, jrunconfig.getPathToExec());
				job.setAttribute(PreferenceConstants.JOB_WORK_DIR, jrunconfig.getWorkingDir());
				job.setAttribute(PreferenceConstants.JOB_ARGS, jrunconfig.getArguments());
				job.setAttribute(PreferenceConstants.JOB_DEBUG_DIR, exePath.removeLastSegments(1).toOSString());
				PLaunch pLaunch = (PLaunch) launch;
				pLaunch.setPJob(job);
				int timeout = store.getInt(IPDebugConstants.PREF_PTP_DEBUG_COMM_TIMEOUT);
				PTPDebugCorePlugin.getDebugModel().createDebuggerSession(debugger, pLaunch, exeFile, timeout, new SubProgressMonitor(monitor, 40));
				monitor.worked(10);
				if (monitor.isCanceled()) {
					PTPDebugCorePlugin.getDebugModel().shutdownSession(job);
				}
			}
			else {
				monitor.worked(40);
			}
		} catch (CoreException e) {
			if (e.getStatus().getPlugin().equals(PTPCorePlugin.PLUGIN_ID))
				abort(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Control_system_does_not_exist"), null, 0);
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				PTPDebugCorePlugin.getDebugModel().shutdownSession(job);
				if (debugger != null) {
					debugger.stopDebugger();
				}
			}
			if (e.getStatus().getCode() != IStatus.CANCEL) {
				throw e;
			}
		} finally {
			monitor.done();
		}
	}
}
