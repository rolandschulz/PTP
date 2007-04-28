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
import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.QueueAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.rmsystem.IResourceManager;
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
	protected IPath getProgramFile(ILaunchConfiguration configuration) throws CoreException {
		IProject project = verifyProject(configuration);
		String fileName = getProgramName(configuration);
		if (fileName == null)
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Application_file_not_specified"), null, IStatus.INFO);

		IPath programPath = new Path(fileName);
		if (!programPath.isAbsolute()) {
			programPath = project.getFile(programPath).getLocation();
		}
		if (!programPath.toFile().exists()) {
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Application_file_does_not_exist"), new FileNotFoundException(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.PROGRAM_PATH_not_found")), IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
		/* --old
		IFile programPath = project.getFile(fileName);
		if (programPath == null || !programPath.exists() || !programPath.getLocation().toFile().exists())
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Application_file_does_not_exist"), new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Application_path_not_found", programPath.getLocation().toString())), IStatus.INFO);
		*/
		return programPath;
	}
	protected String[] getProgramParameters(ILaunchConfiguration configuration) throws CoreException {
		List arguments = new ArrayList();
		String temp = getArgument(configuration);
		if (temp != null && temp.length() > 0) 
			arguments.add(temp);
		return (String[]) arguments.toArray(new String[arguments.size()]);
	}
	/**
	 * Get all the attributes specified in the launch configuration.
	 * 
	 * @param configuration
	 * @return AttributeManager
	 * @throws CoreException
	 */
	protected AttributeManager getAttributeManager(ILaunchConfiguration configuration) throws CoreException {
		AttributeManager attrMgr = new AttributeManager();
		IResourceManager rm = getResourceManager(configuration);
		if (rm != null) {
			try {
				IPQueue queue = rm.getQueue(getQueueName(configuration));
				if (queue != null) {
						attrMgr.setAttribute(QueueAttributes.getIdAttributeDefinition().create(queue.getIDString()));
				}
				
				IPath programFile = getProgramFile(configuration);
				attrMgr.setAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(programFile.lastSegment()));
				
				String path = programFile.removeLastSegments(1).toOSString();
				if (path != null) {
					attrMgr.setAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(path));
				}
				
				String wd = verifyWorkDirectory(configuration);
				if (wd != null) {
					attrMgr.setAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(wd));
				}
				
				String[] argArr = getProgramParameters(configuration);
				if (argArr != null) {
					attrMgr.setAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(argArr));
				}
				
				String[] envArr = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
				if (envArr != null) {
					attrMgr.setAttribute(JobAttributes.getEnvironmentAttributeDefinition().create(envArr));
				}
			} catch (IllegalValueException e) {
			}
			attrMgr.setAttributes(getLaunchAttributes(configuration));
		} 
		return attrMgr;
	}
	/**
	 * Get the attributes from the resource manager specific launch page.
	 * 
	 * @param configuration
	 * @return IAttribute[]
	 * @throws CoreException
	 */
	private IAttribute[] getLaunchAttributes(ILaunchConfiguration configuration)
	throws CoreException {

		String queueName = getQueueName(configuration);	
		IResourceManager rm = getResourceManager(configuration);

		final AbstractRMLaunchConfigurationFactory rmFactory =
			PTPLaunchPlugin.getDefault().getRMLaunchConfigurationFactory(rm);
		if (rmFactory == null) {
			return new IAttribute[0];
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = rmFactory.create(rm);
		IPQueue queue = rm.getQueue(queueName);
		if (queue != null) {
			return rmDynamicTab.getAttributes(rm, queue, configuration);
		}
		return new IAttribute[0];
	}
	
   protected String verifyWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
        String workPath = getWorkDirectory(configuration);
        if (workPath == null) {
			IProject project = verifyProject(configuration);
			if (project != null)
				return project.getLocation().toOSString();
		} else {
			IPath path = new Path(workPath);
			if (path.isAbsolute()) {
				File dir = new File(path.toOSString());
				if (dir.isDirectory())
					return path.toOSString();

				abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Working_directory_does_not_exist"), new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Application_path_not_found", path.toOSString())), IStatus.INFO);
			} else {
				IResource res = getWorkspaceRoot().findMember(path);
				if (res instanceof IContainer && res.exists())
					return res.getLocation().toOSString();

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
	protected static String getQueueName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.QUEUE_NAME, (String)null);
	}
	protected static String getResourceManagerName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.RESOURCE_MANAGER_NAME, (String)null);
	}
	/*
	protected static String getCommunication(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.NETWORK_TYPE, (String)null);
	}
	*/
	protected static String getArgument(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENT, (String)null);
	}
	protected static String getWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, (String)null);
	}
	protected static String getDebuggerID(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, (String)null);
	}
	protected static String getDebuggerExePath(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String)null);
	}
	protected static String getDebuggerWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String)null);
	}
	
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new PLaunch(configuration, mode, null);
	}
	/***
	 * Debug
	 */
	protected IPDebugConfiguration getDebugConfig(ILaunchConfiguration config) throws CoreException {
		IPDebugConfiguration dbgCfg = null;
		try {
			dbgCfg = PTPDebugCorePlugin.getDefault().getDebugConfiguration(getDebuggerID(config));
		} catch (CoreException e) {
			System.out.println("ParallelLaunchConfigurationDelegate.getDebugConfig() Error");
			throw e;
		}
		return dbgCfg;
	}
	protected void verifyDebuggerPath(String path) throws CoreException {
		if (!verifyPath(path)) {
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Debugger_path_not_found"), new FileNotFoundException(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Debugger_path_not_found")), IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
	}
	protected boolean verifyPath(String path) {
		IPath programPath = new Path(path);
		if (programPath == null || programPath.isEmpty() || !programPath.toFile().exists()) {
			return false;
		}
		return true;
	}
	protected IBinaryObject verifyBinary(ILaunchConfiguration configuration) throws CoreException {
		IProject project = verifyProject(configuration);
		String fileName = getProgramName(configuration);
		IPath programPath = new Path(fileName);
		if (!programPath.isAbsolute()) {
			programPath = project.getFile(programPath).getLocation();
		}
		return verifyBinary(project, programPath);
	}
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
		Throwable exception = new FileNotFoundException(LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Program_is_not_a_recongnized_executable"));
		int code = IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY;
		MultiStatus status = new MultiStatus(PTPCorePlugin.getUniqueIdentifier(), code, LaunchMessages.getResourceString("AbstractParallelLaunchDelegate.Program_is_not_a_recongnized_executable"), exception);
		status.add(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), code, exception == null ? "" : exception.getLocalizedMessage(), exception));
		throw new CoreException(status);
	}
	/****
	 * Source
	 ****/
	protected void setSourceLocator(ILaunch launch, ILaunchConfiguration config) throws CoreException {
		setDefaultSourceLocator(launch, config);
	}
	protected void setDefaultSourceLocator(ILaunch launch, ILaunchConfiguration configuration) throws CoreException {
		//  set default source locator if none specified
		if (launch.getSourceLocator() == null) {
			IPersistableSourceLocator sourceLocator;
			String id = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_ID, (String)null);
			if (id == null) {
				sourceLocator = PTPDebugUIPlugin.createDefaultSourceLocator();
				sourceLocator.initializeDefaults(configuration);
			} else {
				sourceLocator = DebugPlugin.getDefault().getLaunchManager().newSourceLocator(id);
				String memento = configuration.getAttribute(ILaunchConfiguration.ATTR_SOURCE_LOCATOR_MEMENTO, (String)null);
				if (memento == null) {
					sourceLocator.initializeDefaults(configuration);
				} else {
					sourceLocator.initializeFromMemento(memento);
				}
			}
			launch.setSourceLocator(sourceLocator);
		}
	}
	protected IResourceManager getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		String rmName = getResourceManagerName(configuration);
		for (int i = 0; i < rms.length; ++i) {
			if (rms[i].getState() == ResourceManagerAttributes.State.STARTED &&
					rms[i].getName().equals(rmName)) {
				return rms[i];
			}
		}
		return null;
	}
}
