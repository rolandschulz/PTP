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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.rtsystem.JobRunConfiguration;

/**
 *
 */
public abstract class AbstractParallelLaunchConfigurationDelegate extends LaunchConfigurationDelegate {
    public static final String HYPHEN = "-";
    public static final String NUM_PROC = HYPHEN + "p";
    public static final String PROC_PER_NODE = HYPHEN + "N";
    public static final String START_NODE = HYPHEN + "o";
    public static final String PROG_NAME = HYPHEN + HYPHEN;

    protected IModelManager getLaunchManager() {
        return PTPCorePlugin.getDefault().getModelManager();
    }
    
    protected IWorkspaceRoot getWorkspaceRoot() {
    	return ResourcesPlugin.getWorkspace().getRoot();
    }    
    protected IProject getProject(String proName) {
        return getWorkspaceRoot().getProject(proName);
    }
    
    protected IProject verifyProject(ILaunchConfiguration configuration) throws CoreException {
        String proName = getProjectName(configuration);
        if (proName == null)
   			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Project_not_specified"), null, IStatus.INFO);

        IProject project = getProject(proName);
        if (project == null || !project.exists() || !project.isOpen())
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Project_does_not_exist_or_is_not_a_project"), null, IStatus.INFO);
        
        return project;
    }

	protected IFile getProgramFile(ILaunchConfiguration configuration) throws CoreException {
		IProject project = verifyProject(configuration);
		String fileName = getProgramName(configuration);
		if (fileName == null)
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Application_file_not_specified"), null, IStatus.INFO);

		IFile programPath = project.getFile(fileName);
		if (programPath == null || !programPath.exists() || !programPath.getLocation().toFile().exists())
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Application_file_does_not_exist"), new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Application_path_not_found", programPath.getLocation().toString())), IStatus.INFO);

		return programPath;
	}
	
	protected IPath verifyProgramFile(ILaunchConfiguration configuration) throws CoreException {
		return getProgramFile(configuration).getLocation();
	}	

	protected String[] getProgramParameters(ILaunchConfiguration configuration) throws CoreException {
		List arguments = new ArrayList();
		String temp = getNumberOfProcesses(configuration);
		if (temp == null) 
		    abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Number_of_process_not_specified"), null, IStatus.INFO);
		
		arguments.add(NUM_PROC);		
		arguments.add(temp);
		//arguments.add(HYPHEN + getCommunication(configuration));
		temp = getNumberOfProcessesPerNode(configuration);
		if (temp != null  && !temp.equals("0")) {
			arguments.add(PROC_PER_NODE);		
		    arguments.add(temp);
		}
		temp = getFirstNodeNumber(configuration);
		if (temp != null  && !temp.equals("0")) {
			arguments.add(START_NODE);		
		    arguments.add(temp);
		}
		
		return (String[]) arguments.toArray(new String[arguments.size()]);
	}
	
	protected JobRunConfiguration getJobRunConfiguration(ILaunchConfiguration configuration) throws CoreException
	{
		IFile programFile = getProgramFile(configuration);
		String nprocs_str, nprocpnode_str, firstnode_str, machineName;
		
		machineName = getMachineName(configuration);
		//path = programFile.getLocation().toString();
		nprocs_str = getNumberOfProcesses(configuration);
		nprocpnode_str = getNumberOfProcessesPerNode(configuration);
		firstnode_str = getFirstNodeNumber(configuration);
		
		int nprocs, nprocpnode, firstnode;
		nprocs = nprocpnode = firstnode = -1;
		
		try {
			nprocs = (new Integer(nprocs_str)).intValue();
			nprocpnode = (new Integer(nprocpnode_str)).intValue();
			firstnode = (new Integer(firstnode_str)).intValue();
		}
		catch(NumberFormatException e) {	}
		
		JobRunConfiguration jrc = new JobRunConfiguration(
			programFile.getLocation().toString(), machineName, nprocs, nprocpnode, firstnode);
		
		return jrc;
	}
	
	protected String[] verifyArgument(ILaunchConfiguration configuration) throws CoreException {
		IFile programFile = getProgramFile(configuration);
		String[] arguments = getProgramParameters(configuration);
		List cmdLine = new ArrayList(arguments.length);
		cmdLine.addAll(Arrays.asList(arguments));
		cmdLine.add(PROG_NAME);
		
		cmdLine.add(programFile.getLocation().toString());
		
		String temp = getArgument(configuration);
		if (temp != null && temp.length() > 0) 
		    cmdLine.add(temp);
		
		return (String[]) cmdLine.toArray(new String[cmdLine.size()]);
	}
	 
    protected File vertifyWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
        String workPath = getWorkDirectory(configuration);
        if (workPath == null) {
			IProject project = verifyProject(configuration);
			if (project != null)
				return project.getLocation().toFile();
		} else {
			IPath path = new Path(workPath);
			if (path.isAbsolute()) {
				File dir = new File(path.toOSString());
				if (dir.isDirectory())
					return dir;

				abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Working_directory_does_not_exist"), new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Application_path_not_found", path.toOSString())), IStatus.INFO);
			} else {
				IResource res = getWorkspaceRoot().findMember(path);
				if (res instanceof IContainer && res.exists())
					return res.getLocation().toFile();

				abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Working_directory_does_not_exist"), new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Application_path_not_found", path.toOSString())), IStatus.INFO);
			}
		}
		return null;        
    }
    	
	protected void abort(String message, Throwable exception, int code) throws CoreException {
	    throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), code, message, exception));
	}
	
	protected static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}
	protected static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, (String)null);
	}
	protected static String getMachineName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.MACHINE_NAME, (String)null);
	}
	protected static String getNumberOfProcesses(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.NUMBER_OF_PROCESSES, (String)null);
	}
	/*
	protected static String getCommunication(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.NETWORK_TYPE, (String)null);
	}
	*/
	protected static String getNumberOfProcessesPerNode(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.PROCESSES_PER_NODE, (String)null);
	}
	protected static String getFirstNodeNumber(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.FIRST_NODE_NUMBER, (String)null);
	}
	protected static String getArgument(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENT, (String)null);
	}
	protected static String getWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, (String)null);
	}
	protected static String getDebuggerID(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, (String)null);
	}
	
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new PLaunch(configuration, mode, null);
	}
}
