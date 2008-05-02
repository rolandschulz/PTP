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
import org.eclipse.ptp.core.IModelManager;
import org.eclipse.ptp.core.IPTPLaunchConfigurationConstants;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.attributes.AttributeManager;
import org.eclipse.ptp.core.attributes.BooleanAttribute;
import org.eclipse.ptp.core.attributes.EnumeratedAttribute;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.attributes.JobAttributes;
import org.eclipse.ptp.core.elements.attributes.ResourceManagerAttributes;
import org.eclipse.ptp.core.elements.events.IChangedJobEvent;
import org.eclipse.ptp.core.elements.events.IChangedMachineEvent;
import org.eclipse.ptp.core.elements.events.IChangedQueueEvent;
import org.eclipse.ptp.core.elements.events.INewJobEvent;
import org.eclipse.ptp.core.elements.events.INewMachineEvent;
import org.eclipse.ptp.core.elements.events.INewQueueEvent;
import org.eclipse.ptp.core.elements.events.IRemoveJobEvent;
import org.eclipse.ptp.core.elements.events.IRemoveMachineEvent;
import org.eclipse.ptp.core.elements.events.IRemoveQueueEvent;
import org.eclipse.ptp.core.elements.listeners.IQueueChildListener;
import org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener;
import org.eclipse.ptp.core.events.IChangedResourceManagerEvent;
import org.eclipse.ptp.core.events.INewResourceManagerEvent;
import org.eclipse.ptp.core.events.IRemoveResourceManagerEvent;
import org.eclipse.ptp.core.listeners.IModelManagerChildListener;
import org.eclipse.ptp.core.util.ArgumentParser;
import org.eclipse.ptp.debug.core.IPDebugConfiguration;
import org.eclipse.ptp.debug.core.IPDebugger;
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
		LaunchConfigurationDelegate {
	
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
		private IPDebugger debugger;
		
		public JobSubmission(ILaunchConfiguration configuration, String mode, IPLaunch launch,
				AttributeManager attrMgr, IPDebugger debugger) {
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
		public IPDebugger getDebugger() {
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

	private final class MMChildListener implements IModelManagerChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.IChangedResourceManagerEvent)
		 */
		public void handleEvent(IChangedResourceManagerEvent e) {
			// Don't need to do anything
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.INewResourceManagerEvent)
		 */
		public void handleEvent(INewResourceManagerEvent e) {
			/*
			 * Add resource manager child listener so we get notified when new
			 * machines are added to the model.
			 */
			final IResourceManager rm = e.getResourceManager();
	        rm.addChildListener(resourceManagerChildListener);
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.listeners.IModelManagerChildListener#handleEvent(org.eclipse.ptp.core.events.IRemoveResourceManagerEvent)
		 */
		public void handleEvent(IRemoveResourceManagerEvent e) {
			/*
			 * Removed resource manager child listener when resource manager is removed.
			 */
			e.getResourceManager().removeChildListener(resourceManagerChildListener);
		}		
	}

	private final class QueueChildListener implements IQueueChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IChangedJobEvent)
		 */
		public void handleEvent(IChangedJobEvent e) {
			for (IPJob job : e.getJobs()) {
				/*
				 * If the job state has changed to running, find the JobSubmission that 
				 * corresponds to this job and perform remainder of job launch actions
				 */
				IAttribute<?,?,?> attr = job.getAttribute(JobAttributes.getStateAttributeDefinition());
				if (attr != null) {
					JobAttributes.State state = (JobAttributes.State)((EnumeratedAttribute<?>)attr).getValue();
					if (state == JobAttributes.State.RUNNING) {
						synchronized (jobSubmissions) {
							JobSubmission jobSub = jobSubmissions.get(job);
							if (jobSub != null) {
								doCompleteJobLaunch(jobSub.getConfiguration(), jobSub.getMode(), jobSub.getLaunch(), jobSub.getAttrMgr(), jobSub.getDebugger(), job);
								jobSubmissions.remove(job);
							}
						}
					}
				}
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewJobEvent)
		 */
		public void handleEvent(INewJobEvent e) {
			for (IPJob job : e.getJobs()) {
				/*
				 * If the new job is one that we launched, check to see if it's in our list
				 * of job submissions. If not, assume that we have reconnected to a session
				 * so we need to re-create the launch configuration that was used launch it.
				 * If it's a debug job, and it has not yet started running, then
				 * start a debug session. It's possible the job will never run, so we
				 * need to clean up any debug sessions before exiting Eclipse.
				 */
				IAttribute<?,?,?> launchAttr = job.getAttribute(JobAttributes.getLaunchedByPTPFlagAttributeDefinition());
				if (launchAttr != null && ((BooleanAttribute)launchAttr).getValue()) {
					synchronized (jobSubmissions) {
						JobSubmission jobSub = jobSubmissions.get(job);
						if (jobSub == null) {
							// recreate launch configuration
							// jobSub = ....;
							// jobSubmissions.put(job, jobSub);
						} 
						IAttribute<?,?,?> debugAttr = job.getAttribute(JobAttributes.getDebugFlagAttributeDefinition());
						if (debugAttr != null && ((BooleanAttribute)debugAttr).getValue()) {
						}
					}
				}
			}
		}

		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IQueueChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IRemoveJobEvent)
		 */
		public void handleEvent(IRemoveJobEvent e) {
		}
	}

	private final class RMChildListener implements IResourceManagerChildListener {
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedMachineEvent)
		 */
		public void handleEvent(IChangedMachineEvent e) {
			// Don't need to do anything
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent)
		 */
		public void handleEvent(IChangedQueueEvent e) {
			// Can safely ignore
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerNewMachineEvent)
		 */
		public void handleEvent(INewMachineEvent e) {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.INewQueueEvent)
		 */
		public void handleEvent(INewQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queue.addChildListener(queueChildListener);
			}
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerMachineListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveMachineEvent)
		 */
		public void handleEvent(IRemoveMachineEvent e) {
		}
		
		/* (non-Javadoc)
		 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener#handleEvent(org.eclipse.ptp.core.elements.events.IResourceManagerRemoveQueueEvent)
		 */
		public void handleEvent(IRemoveQueueEvent e) {
			for (IPQueue queue : e.getQueues()) {
				queue.removeChildListener(queueChildListener);
			}
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
     * Get the debugger "stop in main" flag
     * 
	 * @param configuration
	 * @return "stop in main" flag
	 * @throws CoreException
	 */
	protected static boolean getDebuggerStopInMainFlag(ILaunchConfiguration configuration) throws CoreException {
	    return configuration.getAttribute(IPTPLaunchConfigurationConstants.ATTR_STOP_IN_MAIN, false);
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
	
	/*
	 * Model listeners
	 */
	private final IModelManagerChildListener modelManagerChildListener = new MMChildListener();
	private final IResourceManagerChildListener resourceManagerChildListener = new RMChildListener();
	private final IQueueChildListener queueChildListener = new QueueChildListener();
	/*
	 * HashMap used to keep track of job submissions
	 */
	protected Map<IPJob, JobSubmission> jobSubmissions = new HashMap<IPJob, JobSubmission>();

	public AbstractParallelLaunchConfigurationDelegate() {
		IModelManager mm = PTPCorePlugin.getDefault().getModelManager();
		synchronized (mm) {
		    for (IResourceManager rm : mm.getUniverse().getResourceManagers()) {
		    	for (IPQueue queue : rm.getQueues()) {
		    		queue.addChildListener(queueChildListener);
		    	}
		        rm.addChildListener(resourceManagerChildListener);
		    }
		    mm.addListener(modelManagerChildListener);
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.debug.core.model.LaunchConfigurationDelegate#getLaunch(org.eclipse.debug.core.ILaunchConfiguration, java.lang.String)
	 */
	public ILaunch getLaunch(ILaunchConfiguration configuration, String mode) throws CoreException {
		return new PLaunch(configuration, mode, null);
	}
	
	/**
	 * Get the attributes from the resource manager specific launch page.
	 * 
	 * @param configuration
	 * @return IAttribute[]
	 * @throws CoreException
	 */
	private IAttribute<?,?,?>[] getResourceAttributes(ILaunchConfiguration configuration)
		throws CoreException {

		IResourceManager rm = getResourceManager(configuration);

		final AbstractRMLaunchConfigurationFactory rmFactory =
			PTPLaunchPlugin.getDefault().getRMLaunchConfigurationFactory(rm);
		if (rmFactory == null) {
			return new IAttribute[0];
		}
		IRMLaunchConfigurationDynamicTab rmDynamicTab = rmFactory.create(rm);
		return rmDynamicTab.getAttributes(rm, null, configuration);
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
			AttributeManager mgr, IPDebugger debugger, IPJob job);
	
	/**
	 * Get all the attributes specified in the launch configuration.
	 * 
	 * @param configuration
	 * @return AttributeManager
	 * @throws CoreException
	 */
	protected AttributeManager getAttributeManager(ILaunchConfiguration configuration) throws CoreException {
		IResourceManager rm = getResourceManager(configuration);
		if (rm == null) {
			abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.No_ResourceManager"), null, 0); //$NON-NLS-1$
		}
		
		AttributeManager attrMgr = new AttributeManager();

		/*
		 * Collect attributes from Resources tab
		 */
		attrMgr.addAttributes(getResourceAttributes(configuration));
		
		/*
		 * Make sure there is a queue, even if the resources tab doesn't require
		 * one to be specified.
		 */
		if (attrMgr.getAttribute(JobAttributes.getQueueIdAttributeDefinition()) == null) {
			IPQueue queue = getQueueDefault(rm);
			attrMgr.addAttribute(JobAttributes.getQueueIdAttributeDefinition().create(queue.getID()));
		}

		/*
		 * Collect attributes from Application tab
		 */
		IPath programPath = verifyExecutablePath(configuration);
		attrMgr.addAttribute(JobAttributes.getExecutableNameAttributeDefinition().create(programPath.lastSegment()));
		
		String path = programPath.removeLastSegments(1).toString();
		if (path != null) {
			attrMgr.addAttribute(JobAttributes.getExecutablePathAttributeDefinition().create(path));
		}
		
		/*
		 * Collect attributes from Debugger tab
		 */
		Boolean stopInMainFlag = getDebuggerStopInMainFlag(configuration);
		attrMgr.addAttribute(JobAttributes.getDebuggerStopInMainFlagAttributeDefinition().create(stopInMainFlag));
	
		/*
		 * Collect attributes from Arguments tab
		 */
		String wd = verifyWorkDirectory(configuration);
		if (wd != null) {
			attrMgr.addAttribute(JobAttributes.getWorkingDirectoryAttributeDefinition().create(wd));
		}
		
		String[] argArr = getProgramArguments(configuration);
		if (argArr != null) {
			attrMgr.addAttribute(JobAttributes.getProgramArgumentsAttributeDefinition().create(argArr));
		}
		
		/*
		 * Collect attributes from Environment tab
		 */
		String[] envArr = DebugPlugin.getDefault().getLaunchManager().getEnvironment(configuration);
		if (envArr != null) {
			attrMgr.addAttribute(JobAttributes.getEnvironmentAttributeDefinition().create(envArr));
		}
		
		/*
		 * PTP launched this job
		 */
		attrMgr.addAttribute(JobAttributes.getLaunchedByPTPFlagAttributeDefinition().create(true));
		
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
	 * Convert application arguments to an array of strings.
	 * 
	 * @param configuration launch configuration
	 * @return array of strings containing the program arguments
	 * @throws CoreException
	 */
	protected String[] getProgramArguments(ILaunchConfiguration configuration) throws CoreException {
		String temp = getArguments(configuration);
		if (temp != null && temp.length() > 0) {
			ArgumentParser ap = new ArgumentParser(temp);
			List<String> args = ap.getArguments();
			if (args != null) {
				return (String[]) args.toArray(new String[args.size()]);
			}
		}
		return new String[0];
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
	 * Get the IProject object from the project name.
	 * 
     * @param project name of the project
     * @return IProject resource
     */
    protected IProject getProject(String project) {
        return getWorkspaceRoot().getProject(project);
    }
	
	/**
	 * Get the default queue for the given resource manager
	 * 
	 * @param rm resource manager
	 * @return default queue
	 */
	protected IPQueue getQueueDefault(IResourceManager rm) {
		final IPQueue[] queues = rm.getQueues();
		if (queues.length == 0) {
			return null;
		}
		return queues[0];
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
		for (IResourceManager rm : rms) {
			if (rm.getState() == ResourceManagerAttributes.State.STARTED &&
					rm.getUniqueName().equals(rmUniqueName)) {
				return rm;
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
			AttributeManager attrMgr, IPDebugger debugger, IProgressMonitor monitor) throws CoreException {
		
		synchronized (jobSubmissions) {
			final IResourceManager rm = getResourceManager(configuration);
			if (rm == null) {
				abort(LaunchMessages.getResourceString("AbstractParallelLaunchConfigurationDelegate.No_ResourceManager"), null, 0);
			}
	
			IPJob job = rm.submitJob(configuration, attrMgr, monitor);

			if (job != null) {
				JobSubmission jobSub = new JobSubmission(configuration, mode, launch, attrMgr, debugger);
				jobSubmissions.put(job, jobSub);
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
}
