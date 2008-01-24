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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IBinaryParser;
import org.eclipse.cdt.core.ICExtensionReference;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
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
import org.eclipse.debug.core.model.IPersistableSourceLocator;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.attributes.JobAttributes.State;
import org.eclipse.ptp.core.elements.events.IJobChangeEvent;
import org.eclipse.ptp.core.elements.listeners.IJobListener;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPTPDebugger;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.launch.PLaunch;
import org.eclipse.ptp.debug.ui.PTPDebugUIPlugin;
import org.eclipse.ptp.launch.PTPLaunchPlugin;
import org.eclipse.ptp.launch.internal.ui.LaunchMessages;
import org.eclipse.ptp.launch.ui.extensions.AbstractRMLaunchConfigurationFactory;
import org.eclipse.ptp.launch.ui.extensions.IRMLaunchConfigurationDynamicTab;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteServices;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.ptp.rm.remote.core.AbstractRemoteResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 *
 */
public abstract class AbstractParallelLaunchConfigurationDelegate extends
		LaunchConfigurationDelegate implements IJobListener {
	
	/**
	 * The JobSubmission class encapsulates all the information used in 
	 * a job submission. Once the job is created *and starts running*,
	 * this information is used to complete the launch.
	 */
	private class JobSubmission {
		private ILaunchConfiguration configuration;
		private String mode;
		private IPLaunch launch;
		private AttributeManager attrMgr;
		private IPTPDebugger debugger;
		
		public JobSubmission(ILaunchConfiguration configuration, String mode, IPLaunch launch,
				AttributeManager attrMgr, IPTPDebugger debugger) {
			this.configuration = configuration;
			this.mode = mode;
			this.launch = launch;
			this.attrMgr = attrMgr;
			this.debugger = debugger;
		}

		/**
		 * @return the attrMgr
		 */
		public AttributeManager getAttrMgr() {
			return attrMgr;
		}

		/**
		 * @return the configuration
		 */
		public ILaunchConfiguration getConfiguration() {
			return configuration;
		}

		/**
		 * @return the debugger
		 */
		public IPTPDebugger getDebugger() {
			return debugger;
		}

		/**
		 * @return the launch
		 */
		public IPLaunch getLaunch() {
			return launch;
		}

		/**
		 * @return the mode
		 */
		public String getMode() {
			return mode;
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
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_ARGUMENTS, (String)null);
	}
    
	/**
	 * Get the debugger executable path
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getDebuggerExePath(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_EXECUTABLE_PATH, (String)null);
	}
	
	/**
	 * Get the ID of the debugger for this launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getDebuggerID(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_ID, (String)null);
	}
	/**
	 * Get the working directory for this debug session
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getDebuggerWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_DEBUGGER_WORKING_DIR, (String)null);
	}
	
	/**
     * Get the absolute path of the executable to launch. If the executable is on a remote machine,
     * this is the path to the executable on that machine.
     * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getExecutablePath(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_EXECUTABLE_PATH, (String)null);
	}
	
	/**
	 * Get the name of the application to launch
	 * 
	 * @deprecated
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getProgramName(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_APPLICATION_NAME, (String)null);
	}

	/**
	 * Get the name of the project
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getProjectName(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_PROJECT_NAME, (String)null);
	}
	
    /**
	 * Get the name of the queue for the launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getQueueName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_QUEUE_NAME, (String)null);
	} 
	
	/**
	 * Get the resource manager to use for the launch
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getResourceManagerUniqueName(ILaunchConfiguration configuration) throws CoreException {
		return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_RESOURCE_MANAGER_UNIQUENAME, (String)null);
	}

	/**
     * Get the working directory for the application launch
     * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static String getWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_WORK_DIRECTORY, (String)null);
	}

	/**
     * Get the console display option
     * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected static boolean getConsoleDisplayOption(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_CONSOLE, false);
	}
	
	/*
	 * HashMap used to keep track of job submissions
	 */
	protected Map<IPJob, JobSubmission> jobSubmissions = new HashMap<IPJob, JobSubmission>();
	
    public AbstractParallelLaunchConfigurationDelegate() {
	}    
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new PLaunch(configuration, mode, null);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elements.listeners.IJobListener#handleEvent(org.eclipse.ptp.core.elements.events.IJobChangeEvent)
	 */
	public void handleEvent(IJobChangeEvent e) {
		/*
		 * If the job state has changed to running, find the JobSubmission that 
		 * corresponds to this job and perform remainder of job launch actions
		 */
		IAttribute<?,?,?> attr = e.getAttributes().get(JobAttributes.getStateAttributeDefinition());
		if (attr != null) {
			JobAttributes.State state = (State)((EnumeratedAttribute<?>)attr).getValue();
			if (state == JobAttributes.State.RUNNING) {
				synchronized (jobSubmissions) {
					IPJob job = e.getSource();
					JobSubmission jobSub = jobSubmissions.get(job);
					if (jobSub != null) {
						doCompleteJobLaunch(jobSub.getConfiguration(), jobSub.getMode(), jobSub.getLaunch(), jobSub.getAttrMgr(), jobSub.getDebugger(), job);
						jobSubmissions.remove(job);
						job.removeElementListener(this);
					}
				}
			}
		}
	}
	
	/**
	 * Get the attributes from the resource manager specific launch page.
	 * 
	 * @param configuration
	 * @return IAttribute[]
	 * @throws CoreException
	 */
	private IAttribute<?,?,?>[] getLaunchAttributes(ILaunchConfiguration configuration)
		throws CoreException {

		String queueName = getQueueName(configuration);	
		IResourceManager rm = getResourceManager(configuration);

		final AbstractRMLaunchConfigurationFactory rmFactory =
			PTPLaunchPlugin.getDefault().getRMLaunchConfigurationFactory(rm);
		if (rmFactory == null) {
			return new IAttribute[0];
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = rmFactory.create(rm);
		IPQueue queue = rm.getQueueByName(queueName);
		if (queue != null) {
			return rmDynamicTab.getAttributes(rm, queue, configuration);
		}
		return new IAttribute[0];
	}
	
	/**
	 * Abort the job launch if anything goes wrong.
	 * 
	 * @param message
	 * @param exception
	 * @param code
	 * @throws CoreException
	 */
	protected void abort(String message, Throwable exception, int code) throws CoreException {
	    throw new CoreException(new Status(IStatus.ERROR, PTPLaunchPlugin.getUniqueIdentifier(), code, message, exception));
	}
	
	/**
	 * This method is called when the job state changes to RUNNING. This allows the launcher to
	 * complete the job launch.
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param mgr
	 * @param debugger
	 * @param job
	 */
	protected abstract void doCompleteJobLaunch(ILaunchConfiguration configuration, String mode, IPLaunch launch,  
			AttributeManager mgr, IPTPDebugger debugger, IPJob job);
	
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
			IPQueue queue = rm.getQueueByName(getQueueName(configuration));
			if (queue != null) {
					attrMgr.addAttribute(JobAttributes.getQueueIdAttributeDefinition().create(queue.getID()));
			}
			
			IPath programPath = verifyExecutablePath(configuration);
			attrMgr.addAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(programPath.lastSegment()));
			
			String path = programPath.removeLastSegments(1).toString();
			if (path != null) {
				attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(path));
			}
			
			String wd = verifyWorkDirectory(configuration);
			if (wd != null) {
				attrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(wd));
			}
			
			String[] argArr = getProgramParameters(configuration);
			if (argArr != null) {
				attrMgr.addAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(argArr));
			}
			
			String[] envArr = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
			if (envArr != null) {
				attrMgr.addAttribute(JobAttributes.getEnvironmentAttributeDefinition().create(envArr));
			}
			attrMgr.addAttributes(getLaunchAttributes(configuration));
		} 
		return attrMgr;
	}
	
	/**
	 * Get the debugger configuration
	 * 
	 * @param configuration launch configuration
	 * @return debugger configuration
	 * @throws CoreException
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
	
	/**
	 * Get the path of the program to launch. No longer used since the program may not be
	 * on the local machine.
	 * 
	 * @deprecated
	 * 
	 * @param configuration launch configuration
	 * @return IPath corresponding to program executable
	 * @throws CoreException
	 */
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
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Application_file_does_not_exist"), 
					new FileNotFoundException(
							LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Path_not_found", programPath.toString())), 
							IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
		/* --old
		IFile programPath = project.getFile(fileName);
		if (programPath == null || !programPath.exists() || !programPath.getLocation().toFile().exists())
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Application_file_does_not_exist"), new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Application_path_not_found", programPath.getLocation().toString())), IStatus.INFO);
		*/
		return programPath;
	}
	
	/**
	 * Convert application arguments to an array of strings.
	 * 
	 * @param configuration launch configuration
	 * @return array of strings containing the program arguments
	 * @throws CoreException
	 */
	protected String[] getProgramParameters(ILaunchConfiguration configuration) throws CoreException {
		List<String> arguments = new ArrayList<String>();
		String temp = getArguments(configuration);
		if (temp != null && temp.length() > 0) 
			arguments.add(temp);
		return (String[]) arguments.toArray(new String[arguments.size()]);
	}
	
	/**
	 * Get the IProject object from the project name.
	 * 
     * @param project name of the project
     * @return IProject resource
     */
    protected IProject getProject(String project) {
        return getWorkspaceRoot().getProject(project);
    }
	
	/**
	 * Find the resource manager that corresponds to the unique name specified in the configuration
	 * 
	 * @param configuration launch configuration
	 * @return resource manager
	 * @throws CoreException
	 */
	protected IResourceManager getResourceManager(ILaunchConfiguration configuration) throws CoreException {
		IPUniverse universe = PTPCorePlugin.getDefault().getUniverse();
		IResourceManager[] rms = universe.getResourceManagers();
		String rmUniqueName = getResourceManagerUniqueName(configuration);
		for (int i = 0; i < rms.length; ++i) {
			if (rms[i].getState() == ResourceManagerAttributes.State.STARTED &&
					rms[i].getUniqueName().equals(rmUniqueName)) {
				return rms[i];
			}
		}
		return null;
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
	 * Create a source locator from the ID specified in the configuration, or create
	 * a default one if it hasn't been specified.
	 * 
	 * @param launch
	 * @param configuration
	 * @throws CoreException
	 */
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
	 * Submit a job to the resource manager. Keeps track of the submission so we know when the
	 * job actually starts running. When this happens, the abstract method doCompleteJobLaunch()
	 * is invoked.
	 * 
	 * @param configuration
	 * @param mode
	 * @param launch
	 * @param attrMgr
	 * @param debugger
	 * @param monitor
	 * @throws CoreException
	 */
	protected void submitJob(ILaunchConfiguration configuration, String mode, IPLaunch launch,
			AttributeManager attrMgr, IPTPDebugger debugger, IProgressMonitor monitor) throws CoreException {
		
		synchronized (jobSubmissions) {
			final IResourceManager rm = getResourceManager(configuration);
			if (rm == null) {
				abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.No_ResourceManager"), null, 0);
			}
	
			IPJob job = rm.submitJob(attrMgr, monitor);

			if (job != null) {
				JobSubmission jobSub = new JobSubmission(configuration, mode, launch, attrMgr, debugger);
				jobSubmissions.put(job, jobSub);
				job.addElementListener(this);
			}
		}
	}

	/**
	 * @param configuration
	 * @return
	 * @throws CoreException
	 * @deprecated
	 */
	protected IBinaryObject verifyBinary(ILaunchConfiguration configuration) throws CoreException {
		IProject project = verifyProject(configuration);
		String fileName = getProgramName(configuration);
		IPath programPath = new Path(fileName);
		if (!programPath.isAbsolute()) {
			programPath = project.getFile(programPath).getLocation();
		}
		return verifyBinary(project, programPath);
	}
	
	/**
	 * @param project
	 * @param exePath
	 * @return
	 * @throws CoreException
	 * @deprecated
	 */
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
		Throwable exception = new FileNotFoundException(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Program_is_not_a_recongnized_executable"));
		int code = IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_BINARY;
		MultiStatus status = new MultiStatus(PTPCorePlugin.getUniqueIdentifier(), code, LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Program_is_not_a_recongnized_executable"), exception);
		status.add(new Status(IStatus.ERROR, PTPCorePlugin.getUniqueIdentifier(), code, exception == null ? "" : exception.getLocalizedMessage(), exception));
		throw new CoreException(status);
	}
	
	/**
	 * @param path
	 * @throws CoreException
	 */
	protected void verifyDebuggerPath(String dbgPath, ILaunchConfiguration configuration) throws CoreException {
		IPath path = verifyResource(dbgPath, configuration);
		if (path == null) {
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Debugger_path_not_found"), 
					new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Path_not_found", 
							dbgPath)), IPTPLaunchConfigurationConstants.ERR_PROGRAM_NOT_EXIST);
		}
	}

	/**
	 * @param path
	 * @return
	 * @deprecated
	 */
	protected boolean verifyPath(String path) {
		IPath programPath = new Path(path);
		if (programPath == null || programPath.isEmpty() || !programPath.toFile().exists()) {
			return false;
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
        String proName = getProjectName(configuration);
        if (proName == null)
   			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Project_not_specified"), null, IStatus.INFO);

        IProject project = getProject(proName);
        if (project == null || !project.exists() || !project.isOpen())
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Project_does_not_exist_or_is_not_a_project"), null, IStatus.INFO);
        
        return project;
    }

	/**
	 * Verify the working directory. If no working directory is specified, the default is
	 * the location of the executable.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected String verifyWorkDirectory(ILaunchConfiguration configuration) throws CoreException {
		IPath path;
        String workPath = getWorkDirectory(configuration);
        if (workPath == null) {
        	path = verifyExecutablePath(configuration).removeLastSegments(1);
		} else {
			path = verifyResource(workPath, configuration);
		}
        if (path == null) {
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Working_directory_does_not_exist"), 
					new FileNotFoundException(LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Path_not_found", path.toString())), IStatus.INFO);
		}
		return path.toString();        
    }
	
	/**
	 * @param configuration
	 * @return
	 */
	protected IPath verifyExecutablePath(ILaunchConfiguration configuration)  throws CoreException {
		String exePath = getExecutablePath(configuration);
		IPath path = verifyResource(exePath, configuration);
		if (path == null) {
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.Application_file_does_not_exist"), 
					new FileNotFoundException(
							LaunchMessages.getFormattedResourceString("AbstractParallelLaunchConfigurationDelegate.Path_not_found", 
							exePath)), IStatus.INFO);
		}
		return path;
	}
	
	/**
	 * @param path
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	protected IPath verifyResource(String path, ILaunchConfiguration configuration) throws CoreException {
		IResourceManagerControl rm = (IResourceManagerControl)getResourceManager(configuration);
		if (rm != null) {
			IResourceManagerConfiguration conf = rm.getConfiguration();
			if (conf instanceof AbstractRemoteResourceManagerConfiguration) {
				AbstractRemoteResourceManagerConfiguration remConf = (AbstractRemoteResourceManagerConfiguration)conf;
				IRemoteServices remoteServices = PTPRemotePlugin.getDefault().getRemoteServices(remConf.getRemoteServicesId());
				if (remoteServices != null) {
					IRemoteConnectionManager connMgr = remoteServices.getConnectionManager();
					IRemoteConnection conn = connMgr.getConnection(remConf.getConnectionName());
					IRemoteFileManager fileManager = remoteServices.getFileManager(conn);
					try {
						IPath resPath = new Path(path);
						IFileStore res = fileManager.getResource(resPath, new NullProgressMonitor());
						if (res.fetchInfo().exists()) {
							return resPath;
						}
					} catch (IOException e) {
					}
				}
			} else {
				// FIXME: work out what to do for RM's that don't extend AbstractRemoteResourceManagerConfiguration
			}
		}
		return null;
	}
}
