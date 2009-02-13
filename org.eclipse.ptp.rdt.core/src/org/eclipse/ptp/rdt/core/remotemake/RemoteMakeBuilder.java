/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.ErrorParserManager;
import org.eclipse.cdt.core.envvar.IEnvironmentVariable;
import org.eclipse.cdt.core.model.ICModelMarker;
import org.eclipse.cdt.core.resources.IConsole;
import org.eclipse.cdt.internal.core.ConsoleOutputSniffer;
import org.eclipse.cdt.make.core.IMakeBuilderInfo;
import org.eclipse.cdt.make.core.MakeBuilder;
import org.eclipse.cdt.make.core.MakeCorePlugin;
import org.eclipse.cdt.make.internal.core.MakeMessages;
import org.eclipse.cdt.make.internal.core.StreamMonitor;
import org.eclipse.cdt.make.internal.core.scannerconfig.ScannerInfoConsoleParserFactory;
import org.eclipse.cdt.managedbuilder.core.IBuilder;
import org.eclipse.cdt.managedbuilder.core.IConfiguration;
import org.eclipse.cdt.managedbuilder.core.IManagedBuildInfo;
import org.eclipse.cdt.managedbuilder.core.ManagedBuildManager;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceRuleFactory;
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
import org.eclipse.ptp.internal.rdt.core.remotemake.RemoteProcessClosure;
import org.eclipse.ptp.rdt.core.serviceproviders.IRemoteExecutionServiceProvider;
import org.eclipse.ptp.rdt.core.services.IRDTServiceConstants;
import org.eclipse.ptp.rdt.services.core.IService;
import org.eclipse.ptp.rdt.services.core.IServiceConfiguration;
import org.eclipse.ptp.rdt.services.core.IServiceProvider;
import org.eclipse.ptp.rdt.services.core.ServiceModelManager;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServices;

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
			IResourceRuleFactory ruleFactory= ResourcesPlugin.getWorkspace().getRuleFactory();
			final ISchedulingRule rule = ruleFactory.modifyRule(getProject());
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

				
				
				IPath workingDirectory = mbsInfo.getDefaultConfiguration().getBuildData().getBuilderCWD();
				
				String[] targets = getTargets(kind, info);
				if (targets.length != 0 && targets[targets.length - 1].equals(info.getCleanBuildTarget()))
					isClean = true;

				String errMsg = null;

				// For the environment variables
				HashMap<String, String> envMap = new HashMap<String, String>();
				
				// Add variables from build info
				IEnvironmentVariable[] envVars = ManagedBuildManager.getEnvironmentVariableProvider().getVariables(mbsInfo.getDefaultConfiguration(), true);
				
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
				ErrorParserManager epm = new ErrorParserManager(getProject(), workingDirectory, this, info.getErrorParsers());
				epm.setOutputStream(streamMon);
				final OutputStream stdout = epm.getOutputStream();
				final OutputStream stderr = epm.getOutputStream();
				
				ConsoleOutputSniffer sniffer = ScannerInfoConsoleParserFactory.getMakeBuilderOutputSniffer(
						stdout, stderr, getProject(), workingDirectory, null, this, null);
				OutputStream consoleOut = (sniffer == null ? stdout : sniffer.getOutputStream());
				OutputStream consoleErr = (sniffer == null ? stderr : sniffer.getErrorStream());
				
				// Determine the service model for this configuration, and use the provider of the build
				// service to execute the build command.
				ServiceModelManager smm = ServiceModelManager.getInstance();
				IServiceConfiguration serviceConfig = smm.getActiveConfiguration(getProject());
				IService buildService = smm.getService(IRDTServiceConstants.SERVICE_BUILD);
				IServiceProvider provider = serviceConfig.getServiceProvider(buildService);
				IRemoteExecutionServiceProvider executionProvider = null;
				if(provider instanceof IRemoteExecutionServiceProvider) {
					executionProvider = (IRemoteExecutionServiceProvider) provider;
				}
				
				if (executionProvider != null) {
					
					IRemoteServices remoteServices = executionProvider.getRemoteServices();
					
					IRemoteConnection connection = executionProvider.getConnection();
					
					if(!connection.isOpen())
						connection.open(monitor);
					
					List<String> command = new LinkedList<String>();
					
					command.add(buildCommand);
					
					for(int k = 0; k <  buildArguments.length; k++)
						command.add(buildArguments[k]);
								
					IRemoteProcessBuilder processBuilder = remoteServices.getProcessBuilder(connection, command);
					
					// set the environment for the builder
					Map<String, String> remoteEnvMap = processBuilder.environment();
					
					if (!info.appendEnvironment()) {
						// if we're replacing the environment then clear the map
						remoteEnvMap.clear();
					}
					remoteEnvMap.putAll(envMap);
					
					// set the directory in which to run the command
					IRemoteFileManager fileManager = remoteServices.getFileManager(connection);
					IFileStore workingDirFileStore = fileManager.getResource(workingDirectory, monitor);
					processBuilder.directory(workingDirFileStore);
					
					// Before launching give visual cues via the monitor
					monitor.subTask(MakeMessages.getString("MakeBuilder.Invoking_Command") + command); //$NON-NLS-1$
					
					// combine stdout and stderr
					// TODO FIXME:  this doesn't currently work for the RSE provider
					processBuilder.redirectErrorStream(true);
					

					
					final IRemoteProcess p = processBuilder.start();
					
					// create a thread to periodically check the progress monitor for cancellation and potentially
					// terminate the process if required
					
					Thread monitorThread = new Thread(new Runnable() {

						public void run() {
							while(!monitor.isCanceled() && !p.isCompleted()) {
								try {
									Thread.sleep(500);
								} catch (InterruptedException e) {
									// TODO Auto-generated catch block
									e.printStackTrace();
								}
								if(monitor.isCanceled() && !p.isCompleted()) {
									p.destroy();
								}
							}
							
						}
						
					}, MakeMessages.getString("Remote Make Monitor Thread")); //$NON-NLS-1$
					
					monitorThread.start();

					if (p != null) {
						try {
							// Close the input of the Process explicitly.
							// We will never write to it.
							p.getOutputStream().close();

							// Hook up the process output to the console.
							// In theory, stderr is combined so no need to read stderr... we should set it to null.
							// HOWEVER:  the RSE provider doesn't merge the streams... so if we want the stderr output, then
							// we have to (for now), supply both streams.  Not sure if this will cause stderr doubling on the Remote Tools provider.
							RemoteProcessClosure remoteProcessClosure = new RemoteProcessClosure(p, consoleOut, consoleErr);
							remoteProcessClosure.runNonBlocking();
						
						} catch (IOException e) {
							// don't really care if this fails
						}

	
						// wait for the process to finish
						while (!p.isCompleted()) {
							try {
								p.waitFor();
							} catch (InterruptedException e) {
								// just keep waiting until the process is done
							}
						}
											
						try {
							// Do not allow the cancel of the refresh, since the
							// builder is external
							// to Eclipse, files may have been created/modified
							// and we will be out-of-sync.
							// The caveat is that for huge projects, it may take a while
							currProject.refreshLocal(IResource.DEPTH_INFINITE, null);
						} catch (CoreException e) {
							// this should never happen because we should never be building from a
							// state where ressource changes are disallowed
						}
					} 

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

				stdout.close();
				stderr.close();

				monitor.subTask(MakeMessages.getString("MakeBuilder.Creating_Markers")); //$NON-NLS-1$
				consoleOut.close();
				consoleErr.close();
				epm.reportProblems();
				cos.close();
			}
		} catch (Exception e) {
			CCorePlugin.log(e);
		} finally {
			monitor.done();
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
}
