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

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.MessageFormat;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.cdt.debug.core.ICDTLaunchConfigurationConstants;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.core.resources.IFile;
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
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.PCDIDebugModel;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.internal.ui.LaunchUtils;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

/**
 * 
 */
public class ParallelLaunchConfigurationDelegate extends AbstractParallelLaunchConfigurationDelegate {
	
	private IBinaryObject verifyBinary(ICProject project, IPath exePath) throws CoreException {
		ICExtensionReference[] parserRef = CCorePlugin.getDefault().getBinaryParserExtensions(project.getProject());
		for (int i = 0; i < parserRef.length; i++) {
			try {
				IBinaryParser parser = (IBinaryParser)parserRef[i].createExtension();
				IBinaryObject exe = (IBinaryObject)parser.getBinary(exePath);
				if (exe != null) {
					return exe;
				}
			} catch (ClassCastException e) {
			} catch (IOException e) {
			}
		}
		IBinaryParser parser = CCorePlugin.getDefault().getDefaultBinaryParser();
		try {
			return (IBinaryObject)parser.getBinary(exePath);
		} catch (ClassCastException e) {
		} catch (IOException e) {
		}
		Throwable exception = new FileNotFoundException(
				"AbstractCLaunchDelegate.Program_is_not_a_recongnized_executable"); //$NON-NLS-1$
		int code = ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY;
		MultiStatus status = new MultiStatus("PluginID", code, "AbstractCLaunchDelegate.Program_is_not_a_recongnized_executable", exception); //$NON-NLS-1$
		status.add(new Status(IStatus.ERROR, "PluginID", code, exception == null ? "" : exception.getLocalizedMessage(), //$NON-NLS-1$
				exception));
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
		ICProject cproject = verifyCProject(config);
		IPath programPath = getProgramPath(config);
		if (programPath == null || programPath.isEmpty()) {
			return null;
		}
		if (!programPath.isAbsolute()) {
			IFile wsProgramPath = cproject.getProject().getFile(programPath);
			programPath = wsProgramPath.getLocation();
		}
		if (!programPath.toFile().exists()) {
			abort(
					"AbstractCLaunchDelegate.Program_file_does_not_exist", //$NON-NLS-1$
					new FileNotFoundException(
							"AbstractCLaunchDelegate.PROGRAM_PATH_not_found"), //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
		return programPath;
	}

	public static ICProject getCProject(ILaunchConfiguration configuration) throws CoreException {
		String projectName = getProjectName(configuration);
		if (projectName != null) {
			projectName = projectName.trim();
			if (projectName.length() > 0) {
				IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
				ICProject cProject = CCorePlugin.getDefault().getCoreModel().create(project);
				if (cProject != null && cProject.exists()) {
					return cProject;
				}
			}
		}
		return null;
	}

	private ICProject verifyCProject(ILaunchConfiguration config) throws CoreException {
		String name = getProjectName(config);
		if (name == null) {
			abort("AbstractCLaunchDelegate.C_Project_not_specified", null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_UNSPECIFIED_PROJECT);
		}
		ICProject cproject = getCProject(config);
		if (cproject == null) {
			IProject proj = ResourcesPlugin.getWorkspace().getRoot().getProject(name);
			if (!proj.exists()) {
				abort(
						"AbstractCLaunchDelegate.Project_NAME_does_not_exist", null, //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
			} else if (!proj.isOpen()) {
				abort("AbstractCLaunchDelegate.Project_NAME_is_closed", null, //$NON-NLS-1$
						ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
			}
			abort("AbstractCLaunchDelegate.Not_a_C_CPP_project", null, //$NON-NLS-1$
					ICDTLaunchConfigurationConstants.ERR_NOT_A_C_PROJECT);
		}
		return cproject;
	}

    private IPDebugConfiguration getDebugConfig(ILaunchConfiguration config) throws CoreException {
        IPDebugConfiguration dbgCfg = null;
        try {
            dbgCfg = PTPDebugCorePlugin.getDefault().getDebugConfiguration("org.eclipse.ptp.debug.external.PTPDebugger");
        } catch (CoreException e) {
            System.out.println("ParallelLaunchConfigurationDelegate.getDebugConfig() Error");
            throw e;
        }
        return dbgCfg;
    }

	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.ILaunchConfigurationDelegate#launch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String, org.eclipse.debug.core.ILaunch, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		IBinaryObject exeFile = null;
		if (monitor == null)
		    monitor = new NullProgressMonitor();		
				
		monitor.beginTask(MessageFormat.format("{0}...", new String[]{configuration.getName()}), 10);
		
		LaunchUtils.switchPerspectiveTo(LaunchUtils.PPerspectiveFactory_ID);    		
		monitor.worked(1);

		//check for cancellation
		if (monitor.isCanceled())
			return;
		
		monitor.subTask(LaunchMessages.getResourceString("ParallelLaunchConfigurationDelegate.Verifying_launch_attributes"));
		
		// done the verification phase
		String projectName = verifyProject(configuration).getName();
		JobRunConfiguration jrunconfig = getJobRunConfiguration(configuration); 
		//String[] args = verifyArgument(configuration);
		File workDirectory = vertifyWorkDirectory(configuration);

		/* Assuming we have parsed the configuration */
		IPath exePath = verifyProgramPath(configuration);
		ICProject project = verifyCProject(configuration);
		if (exePath != null) {
			exeFile = verifyBinary(project, exePath);
		}
		
		getLaunchManager().run(launch, workDirectory, null, jrunconfig, monitor);
		IPJob[] jobs = getLaunchManager().getUniverse().getJobs();
		
		String[] commandLine = new String[] {"/bin/date"};
		
		try {	
			if (mode.equals(ILaunchManager.DEBUG_MODE)) {
				IPDebugConfiguration debugConfig = getDebugConfig(configuration);
				ICDISession dsession = null;
				
				dsession = (ICDISession) debugConfig.createDebugger().createDebuggerSession(jobs, launch, (File) null, monitor);
				
				boolean stopInMain = launch.getLaunchConfiguration().getAttribute( IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false );

				IPCDITarget[] targets = (IPCDITarget[]) dsession.getTargets();
				
				/* Even though ICDISession supports multiple targets but it only creates one target */
				
				for (int i = 0; i < targets.length; i++) {
					Process process = targets[i].getProcess(0);
					IProcess[] iprocesses = new IProcess[1];
					iprocesses[0] = DebugPlugin.newProcess(launch, process, "Launch Label - 0");
					PCDIDebugModel.newDebugTarget(launch, null, targets[i], "Name", iprocesses, exeFile, true, false, stopInMain, true);
				}
			}
			else if (mode.equals(ILaunchManager.RUN_MODE)) {
				Process process = DebugPlugin.exec(commandLine, null);
				IProcess p = DebugPlugin.newProcess(launch, process, "Launch Label");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		monitor.worked(5);
		
		getLaunchManager().setPTPConfiguration(configuration);
				
		if (monitor.isCanceled())
			return;

		monitor.done();
	}
}