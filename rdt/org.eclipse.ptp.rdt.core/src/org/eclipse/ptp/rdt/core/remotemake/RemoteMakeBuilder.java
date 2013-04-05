/*******************************************************************************
 * Copyright (c) 2008, 2013 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.core.remotemake;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector;
import org.eclipse.cdt.make.core.scannerconfig.IScannerInfoCollector2;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig2.SCMarkerGenerator;
import org.eclipse.cdt.make.internal.core.scannerconfig2.ScannerConfigProfileManager;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IInputType;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ITool;
import org.eclipse.cdt.managedbuilder.core.IToolChain;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.cdt.managedbuilder.internal.core.InputType;
import org.eclipse.core.resources.ICommand;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRunnable;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.QualifiedName;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubProgressMonitor;
import org.eclipse.core.runtime.jobs.ISchedulingRule;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.internal.rdt.core.index.IndexBuildSequenceController;
import org.eclipse.ptp.internal.rdt.core.remotemake.RemoteProcessClosure;
import org.eclipse.ptp.internal.rdt.core.remotemake.ResourceRefreshJob;
import org.eclipse.ptp.rdt.core.RDTLog;
import org.eclipse.ptp.rdt.core.activator.Activator;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.services.core.IService;
import org.eclipse.ptp.services.core.IServiceConfiguration;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.ProjectNotConfiguredException;
import org.eclipse.ptp.services.core.ServiceModelManager;

/**
 * @author crecoskie
 * 
 * <strong>EXPERIMENTAL</strong>. This class or interface has been added as
 * part of a work in progress. There is no guarantee that this API will work or
 * that it will remain the same. Please do not use this API without consulting
 * with the RDT team.
 * 
 * A builder for remote "standard make" projects.
 *
 */
public class RemoteMakeBuilder extends MakeBuilder {

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.MakeBuilder#clean(org.eclipse.core.runtime.IProgressMonitor)
	 */
	@Override
	protected void clean(IProgressMonitor monitor) throws CoreException {
		final IMakeBuilderInfo info = MakeCorePlugin.createBuildInfo(getProject(), REMOTE_MAKE_BUILDER_ID);
		if (shouldBuild(CLEAN_BUILD, info)) {
						 
			// have to use the workspace as the rule as the scanner config update needs the workspace lock
			final ISchedulingRule rule = ResourcesPlugin.getWorkspace().getRoot();
			
			Job backgroundJob = new Job("Standard Make Builder"){  //$NON-NLS-1$
				/* (non-Javadoc)
				 * @see org.eclipse.core.runtime.jobs.Job#run(org.eclipse.core.runtime.IProgressMonitor)
				 */
				protected IStatus run(IProgressMonitor monitor) {
					try {
						ResourcesPlugin.getWorkspace().run(new IWorkspaceRunnable() {

							public void run(IProgressMonitor monitor) {
								invokeMake(CLEAN_BUILD, info, monitor);
							}
						}, rule, IWorkspace.AVOID_UPDATE, monitor);
					} catch (CoreException e) {
						return e.getStatus();
					}
					IStatus returnStatus = Status.OK_STATUS;
					return returnStatus;
				}
				
				
			};
			
			backgroundJob.setRule(rule);
			backgroundJob.schedule();
		}
	}
	
	
	

	public static final String REMOTE_MAKE_BUILDER_ID = "org.eclipse.ptp.rdt.core.remoteMakeBuilder"; //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.MakeBuilder#invokeMake(int, org.eclipse.cdt.make.core.IMakeBuilderInfo, org.eclipse.core.runtime.IProgressMonitor)
	 */
	@SuppressWarnings("unchecked")
	@Override
	protected boolean invokeMake(int kind, IMakeBuilderInfo info, IProgressMonitor progressMonitor) {
		
		final IndexBuildSequenceController projectStatus = IndexBuildSequenceController.getIndexBuildSequenceController(getProject());
		
		if(projectStatus!=null){
			projectStatus.setRuntimeBuildStatus(null);
			
		}
		boolean isClean = false;
		IProject currProject = getProject();

		if (progressMonitor == null) {
			progressMonitor = new NullProgressMonitor();
		}
		
		final IProgressMonitor monitor = progressMonitor;
		
		monitor.beginTask(MakeMessages.getString("MakeBuilder.Invoking_Make_Builder") + currProject.getName(), 100); //$NON-NLS-1$

		try {
			IManagedBuildInfo mbsInfo = ManagedBuildManager.getBuildInfo(currProject);
			
			String buildCommand = mbsInfo.getBuildCommand();
			if (buildCommand != null) {
				IConsole console = CCorePlugin.getDefault().getConsole();
				console.start(currProject);

				final OutputStream cos = console.getOutputStream();

				// remove all markers for this project
				removeAllMarkers(currProject);

				final IConfiguration configuration = mbsInfo.getDefaultConfiguration();
				
				final IBuilder builder = configuration.getBuilder();
				IPath workingDirectory = ManagedBuildManager.getBuildLocation(configuration, builder );
				
				if(workingDirectory==null){
					return false;
				}
				
				String[] targets = getTargets(kind, info);
				if (targets.length != 0 && targets[targets.length - 1].equals(info.getCleanBuildTarget()))
					isClean = true;

				String errMsg = null;

				// For the environment variables
				HashMap<String, String> envMap = new HashMap<String, String>();
				
				// Add variables from build info
				IEnvironmentVariable[] envVars = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(configuration, true);
				
				for (IEnvironmentVariable environmentVariable : envVars) {
					envMap.put(environmentVariable.getName(), environmentVariable.getValue());
				}
				
				Iterator<?> iter = envMap.entrySet().iterator();
				List<String> strings= new ArrayList<String>(envMap.size());
				while (iter.hasNext()) {
					Map.Entry entry = (Map.Entry) iter.next();
					StringBuffer buffer= new StringBuffer((String) entry.getKey());
					buffer.append('=').append((String) entry.getValue());
					strings.add(buffer.toString());
				}
				

				
				String[] buildArguments = targets;

				// get build arguments from build model
				// the stupid thing is that there is no way to differentiate
				// arguments here that have spaces in them...
				// oh well...
				String args = mbsInfo.getBuildArguments();
				if (args != null && !args.equals("")) { //$NON-NLS-1$
					String[] newArgs = makeArray(args);
					buildArguments = new String[targets.length + newArgs.length];
					System.arraycopy(newArgs, 0, buildArguments, 0, newArgs.length);
					System.arraycopy(targets, 0, buildArguments, newArgs.length, targets.length);
				}


				QualifiedName qName = new QualifiedName("org.eclipse.ptp.rdt.core", "progressMonitor"); //$NON-NLS-1$ //$NON-NLS-2$
				Integer last = (Integer)getProject().getSessionProperty(qName);
				if (last == null) {
					last = new Integer(100);
				}
				StreamMonitor streamMon = new StreamMonitor(new SubProgressMonitor(monitor, 100), cos, last.intValue());
				ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectory, this, configuration.getErrorParserList());
				epm.setOutputStream(streamMon);
				final OutputStream stdout = epm.getOutputStream();
				final OutputStream stderr = epm.getOutputStream();
				
				OutputStream consoleOut = null;
				OutputStream currentStdOut = stdout;
				OutputStream currentStdErr = stderr;
				OutputStream consoleErr = null;
				
				// add a scanner info sniffer for the inputs of each tool in the toolchain
				IToolChain tc = configuration.getToolChain();
				
				ITool[] tools = tc.getTools();
				
				Set<String> all_scannerIDs = new HashSet<String>();
				for (ITool tool : tools) {

					IInputType[] inputTypes = tool.getInputTypes();

					for (IInputType inputType : inputTypes) {
						
						InputType realInputType = (InputType) inputType;
						
						// get scanner disc IDs
						String scannerIDString = realInputType.getDiscoveryProfileIdAttribute();
						
						if(scannerIDString == null)
							continue;
						
						// IDs are delimited by the | character
						String[] scannerIDs = scannerIDString.split("\\|"); //$NON-NLS-1$
						
						for (String thisScannerid : scannerIDs) {

							//collect all of scanner id first to reduce duplicate scanner and then create ConsoleOutputSniffer.
							if(thisScannerid!=null){
								all_scannerIDs.add(thisScannerid);
							}
							
						}
					}
				}
				
				//create ConsoleOutputSniffer for each scanner id in the set.
				for(String id : all_scannerIDs){
					
					//IScannerConfigBuilderInfo2 scBuilderInfo = ScannerConfigProfileManager
					//		.createScannerConfigBuildInfo2(ManagedBuilderCorePlugin.getDefault()
					//				.getPluginPreferences(), id, false);
					//IScannerConfigBuilderInfo2 scBuilderInfo = ScannerConfigProfileManager.createScannerConfigBuildInfo2(currProject, id);
					IScannerInfoCollector collector = (IScannerInfoCollector) ScannerConfigProfileManager
							.getInstance().getSCProfileConfiguration(id)
							.getScannerInfoCollectorElement().createScannerInfoCollector();

					if (collector instanceof IScannerInfoCollector2) {
						IScannerInfoCollector2 s2 = (IScannerInfoCollector2) collector;
						s2.setProject(currProject);
					}

					SCMarkerGenerator markerGenerator = new SCMarkerGenerator();
					ConsoleOutputSniffer sniffer = ScannerInfoUtility.createBuildOutputSniffer(currentStdOut,
							currentStdErr, currProject, configuration, workingDirectory,
							markerGenerator, collector);
					currentStdOut = (sniffer == null ? currentStdOut : sniffer.getOutputStream());
					currentStdErr = (sniffer == null ? currentStdErr : sniffer.getErrorStream());
				}
				
				// hook the console up to the last sniffer created, or to the stdout/stderr of the process if none were created
				consoleOut = (currentStdOut == null ? stdout : currentStdOut);
				consoleErr = (currentStdErr == null ? stderr : currentStdErr);
				
				// Determine the service model for this configuration, and use the provider of the build
				// service to execute the build command.
				ServiceModelManager smm = ServiceModelManager.getInstance();
				
				try{
					IServiceConfiguration serviceConfig = smm.getActiveConfiguration(getProject());
					IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
					IServiceProvider provider = serviceConfig.getServiceProvider(buildService);
					IRemoteExecutionServiceProvider executionProvider = null;
					if(provider instanceof IRemoteExecutionServiceProvider) {
						executionProvider = (IRemoteExecutionServiceProvider) provider;
					}
					
					IRemoteServices remoteServices = executionProvider.getRemoteServices();
					
					if (remoteServices == null)
						return false;
					
					IRemoteConnection connection = executionProvider.getConnection();
					
					if(connection == null)
						return false;
					
					if(!connection.isOpen())
						connection.open(monitor);
										
					List<String> command = new LinkedList<String>();
					
					command.add(buildCommand);
					
					for(int k = 0; k <  buildArguments.length; k++)
						command.add(buildArguments[k]);
								
					IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, command);
					
					// set the environment for the builder
					Map<String, String> remoteEnvMap = processBuilder.environment();
					
					// Replace the remote environment with the one specified by the build properties.
					// It will already be a modified list that originated with the remote environment and was then
					// modified by the user, so we'll respect the user's edits and replace the environment entirely.
					remoteEnvMap.clear();

					remoteEnvMap.putAll(envMap);
					
					// set the directory in which to run the command
					IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
					if (fileManager != null) {
						processBuilder.directory(fileManager.getResource(workingDirectory.toString()));
					}
					
					// Before launching give visual cues via the monitor
					monitor.subTask(MakeMessages.getString("MakeBuilder.Invoking_Command") + command); //$NON-NLS-1$
					
					// combine stdout and stderr
					// TODO FIXME:  this doesn't currently work for the RSE provider
					processBuilder.redirectErrorStream(true);
					

					
					final IRemoteProcess p = processBuilder.start();
					if(projectStatus!=null){
						if(kind == CLEAN_BUILD){
							projectStatus.setBuildInCompletedForCleanBuild();
						}else{
							projectStatus.setBuildRunning();
						}
					}
										
					// create a thread to periodically check the progress monitor for cancellation and potentially
					// terminate the process if required
					
					Thread monitorThread = new Thread(new Runnable() {

						public void run() {
							while(!monitor.isCanceled() && !p.isCompleted()) {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									RDTLog.logError(e);
								}
								if(monitor.isCanceled() && !p.isCompleted()) {
									p.destroy();
									if(projectStatus!=null){
										projectStatus.setRuntimeBuildStatus(IndexBuildSequenceController.STATUS_INCOMPLETE);
									}
								}
							}
							
						}
						
					}, MakeMessages.getString("Remote Make Monitor Thread")); //$NON-NLS-1$
					
					monitorThread.start();
					
					

					if (p != null) {
						
						// Hook up the process output to the console.
						// In theory, stderr is combined so no need to read stderr... we should set it to null.
						// HOWEVER:  the RSE provider doesn't merge the streams... so if we want the stderr output, then
						// we have to (for now), supply both streams.  Not sure if this will cause stderr doubling on the Remote Tools provider.
						RemoteProcessClosure remoteProcessClosure = new RemoteProcessClosure(p, consoleOut, null);
						remoteProcessClosure.runNonBlocking();

	
						// wait for the process to finish
						while (remoteProcessClosure.isRunning()) {
							try {
								Thread.sleep(100);
							} catch (InterruptedException e) {
								// just keep waiting until the process is done
							}
						}
						
						
						// create a Job for the refresh
						List<IProject> projectsToRefresh = new LinkedList<IProject>();
						projectsToRefresh.add(currProject);
						Job refreshJob = new ResourceRefreshJob(projectsToRefresh);
						refreshJob.schedule();

					}
				}
				catch (ProjectNotConfiguredException e){
					//occurs when loading a project from RTC, this is forced to run before the project is configured, this is expected
					//if not due to above reason, then legitimate error
				}

				
				getProject().setSessionProperty(qName, !monitor.isCanceled() && !isClean ? new Integer(streamMon.getWorkDone()) : null);

				if (errMsg != null) {
					StringBuffer buf = new StringBuffer(buildCommand + " "); //$NON-NLS-1$
					for (int i = 0; i < buildArguments.length; i++) {
						buf.append(buildArguments[i]);
						buf.append(' ');
					}

					String errorDesc = MakeMessages.getFormattedString("MakeBuilder.buildError", buf.toString()); //$NON-NLS-1$
					buf = new StringBuffer(errorDesc);
					buf.append(System.getProperty("line.separator", "\n")); //$NON-NLS-1$ //$NON-NLS-2$
					buf.append("(").append(errMsg).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
					cos.write(buf.toString().getBytes());
					cos.flush();
				}

				stdout.flush();
				stderr.flush();
				consoleErr.flush();
				consoleOut.flush();
				cos.flush();
				cos.close();
				consoleOut.close();
				consoleErr.close();
				stdout.close();
				stderr.close();

				monitor.subTask(MakeMessages.getString("MakeBuilder.Creating_Markers")); //$NON-NLS-1$
				
				epm.reportProblems();
				

			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		} finally {
			monitor.done();
		}
		if(kind != CLEAN_BUILD){
			
			
			projectStatus.invokeIndex();
			
		}
		return (isClean);
	}
	
	private void removeAllMarkers(IProject currProject) throws CoreException {
		IWorkspace workspace = currProject.getWorkspace();

		// remove all markers
		IMarker[] markers = currProject.findMarkers(ICModelMarker.C_MODEL_PROBLEM_MARKER, true, IResource.DEPTH_INFINITE);
		if (markers != null) {
			workspace.deleteMarkers(markers);
		}
	}
	
	// Turn the string into an array.
	private String[] makeArray(String string) {
		string.trim();
		char[] array = string.toCharArray();
		ArrayList<String> aList = new ArrayList<String>();
		StringBuffer buffer = new StringBuffer();
		boolean inComment = false;
		for (int i = 0; i < array.length; i++) {
			char c = array[i];
			if (array[i] == '"' || array[i] == '\'') {
				if (i > 0 && array[i - 1] == '\\') {
					inComment = false;
				} else {
					inComment = !inComment;
				}
			}
			if (c == ' ' && !inComment) {
				aList.add(buffer.toString());
				buffer = new StringBuffer();
			} else {
				buffer.append(c);
			}
		}
		if (buffer.length() > 0)
			aList.add(buffer.toString());
		return (String[]) aList.toArray(new String[aList.size()]);
	}
	
	protected String[] getTargets(int kind, IMakeBuilderInfo info) {
		IManagedBuildInfo mbsInfo = ManagedBuildManager.getBuildInfo(getProject());
		IConfiguration config = mbsInfo.getDefaultConfiguration();
		IBuilder builder = config.getBuilder();
		
		String targets = ""; //$NON-NLS-1$
		switch (kind) {
			case IncrementalProjectBuilder.AUTO_BUILD :
				targets = builder.getAutoBuildTarget();
				break;
			case IncrementalProjectBuilder.INCREMENTAL_BUILD : // now treated as the same!
			case IncrementalProjectBuilder.FULL_BUILD :
				targets = builder.getIncrementalBuildTarget();
				break;
			case IncrementalProjectBuilder.CLEAN_BUILD :
				targets = builder.getCleanBuildTarget();
				break;
		}
		return makeArray(targets);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.make.core.MakeBuilder#shouldBuild(int, org.eclipse.cdt.make.core.IMakeBuilderInfo)
	 */
	@Override
	protected boolean shouldBuild(int kind, IMakeBuilderInfo info) {
		IProject project = getProject();
		
		IProjectDescription description = null;
		try {
			description = project.getDescription();
		} catch (CoreException e) {
			Activator.log(e);
		}
		
		ICommand[] commands = description.getBuildSpec();
		ICommand builderCommand = null;
		
		for(ICommand command : commands) {
			if(command.getBuilderName().equals(REMOTE_MAKE_BUILDER_ID)) {
				builderCommand = command;
				break;
			}
		}
		
		return builderCommand.isBuilding(kind);
	
	}
	

}
