/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.cell.environment.launcher.pdt.internal;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.ptp.cell.environment.launcher.cellbe.AbstractCellRemoteLaunchDelegate;
import org.eclipse.ptp.cell.environment.launcher.cellbe.CancelCallback;
import org.eclipse.ptp.cell.environment.launcher.cellbe.LaunchProcessWrapper;
import org.eclipse.ptp.cell.environment.launcher.cellbe.ProgressListener;
import org.eclipse.ptp.cell.environment.launcher.pdt.Activator;
import org.eclipse.ptp.cell.environment.launcher.pdt.debug.Debug;
import org.eclipse.ptp.cell.environment.launcher.pdt.internal.integration.PdtProfileIntegration;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchIntegration;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchObserver;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProcess;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;


/**
 * 
 * @author Richard Maciel and Daniel Ferber
 *
 */
public class PdtRemoteLaunchDelegate extends AbstractCellRemoteLaunchDelegate {
	
	public String getPluginID() {
		//return RemoteLauncherPlugin.getUniqueIdentifier();
		return Activator.getDefault().getBundle().getSymbolicName();
	}
	
	@Override
	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		
		// Tracing
		 Debug.POLICY.trace(Debug.DEBUG_LAUNCHER, "Start PDT launcher delegate."); //$NON-NLS-1$
	     Debug.POLICY.trace(Debug.DEBUG_LAUNCHER, "Launch configuration map: {0}", configuration.getAttributes().toString()); //$NON-NLS-1$
		
		/*
		 * Get the target controller chosen for the launch and the optional
		 * launch observer that will parse input.
		 * The progress listener is not implemented yet, but will serve as an agent
		 * that communicates with the debugger plug-in.
		 */
		ITargetControl targetControl = getValidatedTargetControl(configuration);
		ILaunchObserver launchObserver = getOutputObserver(configuration);
		ILaunchIntegration launchIntegration = null;
		
		assert mode.equals(ILaunchManager.PROFILE_MODE) : "This launch must only run on PROFILE_MODE";//$NON-NLS-1$
		
		// Extract information about the xml file
		boolean copyXmlFile =  configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_COPY_XML_FILE, IPdtLaunchAttributes.DEFAULT_COPY_XML_FILE);
		String remoteXmldirPath = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_REMOTE_XML_DIR, IPdtLaunchAttributes.DEFAULT_REMOTE_XML_DIR);
		String localXmlfilePath = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_LOCAL_XML_FILE, IPdtLaunchAttributes.DEFAULT_LOCAL_XML_FILE); //TODO: set the complete filename of the xml file
		String remoteXmlFile = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_REMOTE_XML_FILE, IPdtLaunchAttributes.DEFAULT_REMOTE_XML_FILE);
		String traceLibPath = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_TRACE_LIB_PATH, IPdtLaunchAttributes.DEFAULT_TRACE_LIB_PATH);
		String pdtModuleFilePath = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_PDT_MODULE_PATH, IPdtLaunchAttributes.DEFAULT_PDT_MODULE_PATH);
		String remoteTraceDirPath = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_REMOTE_TRACE_DIR, IPdtLaunchAttributes.DEFAULT_REMOTE_TRACE_DIR);
		String traceFilePrefix = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_TRACE_FILE_PREFIX, IPdtLaunchAttributes.DEFAULT_TRACE_FILE_PREFIX);
		
		// Translate macros from some fields
		//LaunchVariableManager.getDefault().resolveValue(location, EMPTY_STRING, EMPTY_STRING, ILaunchVariableContextInfo.CONTEXT_LAUNCH, configuration);
		
		
		// Copy data (already translated) to the bean. 
		PdtLaunchBean launchBean = new PdtLaunchBean();
		launchBean.setCopyXmlFile(copyXmlFile);
		launchBean.setRemoteXmlDirPath(remoteXmldirPath);
		launchBean.setLocalXmlFilePath(localXmlfilePath);
		launchBean.setRemoteXmlFile(remoteXmlFile);
		launchBean.setRemoteTraceDirPath(remoteTraceDirPath);
		launchBean.setTraceFilePrefix(traceFilePrefix);
		
		launchIntegration = new PdtProfileIntegration(configuration, mode, launch, monitor, launchBean);
		
		// Create the execution config from the configuration
		ExecutionConfiguration executionConfig = createExecutionConfig(configuration);
		
		// Check if it is necessary to copy the XML file to the remote machine
		
		
		/*if(copyXmlFile) {
			// Add rule to upload the xml file.
			UploadRule urule = new UploadRule();
			urule.setOverwritePolicy(org.eclipse.ptp.remotetools.environment.launcher.data.OverwritePolicies.ALWAYS);
			urule.setPreserveTimeStamp(true);
			
			urule.setLocalFiles(new String [] {localXmlfilePath});
			
			// Set the rule
			urule.setRemoteDirectory(remoteXmldirPath);
			executionConfig.addSynchronizationRule(urule);
			executionConfig.setDoSynchronizeBefore(true);
		}*/
		
		
		// Build the path of the remote file
		//getTargetControl(configuration).startJob(new )
		/*String outputPrefix = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_TRACE_FILE_PREFIX, IPdtLaunchAttributes.DEFAULT_TRACE_FILE_PREFIX);
		String outputDirPath = configuration.getAttribute(
				IPdtLaunchAttributes.ATTR_TRACE_PATH, IPdtLaunchAttributes.DEFAULT_TRACE_PATH);*/
		//String fullTracefilePath = new Path(outputDirPath).append(outputPrefix + ""); // Fetch remote file path
		//String projectPath = getCProject(configuration).getPath().toOSString(); // Fetch local dir path
		
		//super.
		
		// Add rule to download the generated profile file
		/*DownloadRule drule = new DownloadRule();
		drule.setRemoteFiles(new String [] {fullTracefilePath});
		drule.setLocalDirectory(projectPath); 
		executionConfig.addSynchronizationRule(drule);*/
		
		// Add environment's variables associated with the xml execution
		executionConfig.addEnvironmentVariable("LD_LIBRARY_PATH=$LD_LIBRARY_PATH:" +  traceLibPath);//$NON-NLS-1$
				//configuration.getAttribute(IPdtLaunchAttributes.ATTR_TRACE_LIB_PATH, IPdtLaunchAttributes.DEFAULT_TRACE_LIB_PATH));
		executionConfig.addEnvironmentVariable("PDT_KERNEL_MODULE=" +  pdtModuleFilePath); //$NON-NLS-1$
				//configuration.getAttribute(IPdtLaunchAttributes.ATTR_PDT_MODULE_PATH, IPdtLaunchAttributes.DEFAULT_PDT_MODULE_PATH));
		
		// Build the remote path
		String envPdtConfigFile;
		if(copyXmlFile) {
			String xmlFilename = new Path(localXmlfilePath).lastSegment();
			envPdtConfigFile = remoteXmldirPath + IPath.SEPARATOR + xmlFilename;
			//executionConfig.addEnvironmentVariable("PDT_CONFIG_FILE=" + remoteXmldirPath + IPath.SEPARATOR + xmlFilename);
		} else {
			envPdtConfigFile = remoteXmlFile;
			//executionConfig.addEnvironmentVariable("PDT_CONFIG_FILE=" + remoteXmlFile);
		}
		executionConfig.addEnvironmentVariable("PDT_CONFIG_FILE=" + envPdtConfigFile); //$NON-NLS-1$
		Debug.POLICY.trace(Debug.DEBUG_LAUNCHER_VARIABLES, "PDT_CONFIG_FILE=" + envPdtConfigFile); //$NON-NLS-1$
		
		
		executionConfig.addEnvironmentVariable("PDT_TRACE_OUTPUT=" +  remoteTraceDirPath); //$NON-NLS-1$
				//configuration.getAttribute(IPdtLaunchAttributes.ATTR_REMOTE_TRACE_DIR, IPdtLaunchAttributes.DEFAULT_REMOTE_TRACE_DIR));
		executionConfig.addEnvironmentVariable("PDT_OUTPUT_PREFIX=" +  traceFilePrefix);//$NON-NLS-1$
				//configuration.getAttribute(IPdtLaunchAttributes.ATTR_TRACE_FILE_PREFIX, IPdtLaunchAttributes.DEFAULT_TRACE_FILE_PREFIX));
		
		/*
		 * Create a remote execution job, set the contribution objects
		 * and add this job to the target controller.
		 */
		ILaunchProcess launchProcess = RemoteLauncherPlugin.createRemoteLaunchProcess(launch, executionConfig, launchIntegration);
		launchProcess.setLaunchObserver(launchObserver);
		LaunchProcessWrapper wrapper = new LaunchProcessWrapper(launchProcess);

		/*
		 * Create a progress queue with callback to control the progress monitor.
		 * This queue is a progress listener for the execution job.
		 */
		ProgressListener progressListener = new ProgressListener(executionConfig, monitor, new CancelCallback(launchProcess));
		try {
			progressListener.start();
			launchProcess.addProgressListener(progressListener);
		
			/*
			 * Finally, run the launcher job.
			 */
			targetControl.startJob(wrapper);
		
			/*
			 * Block the launcher, showing the progress monitor, 
			 * until the application start remote execution.
			 */
			progressListener.waitForLaunch();
		} catch (CoreException e) {
			Debug.POLICY.error(Debug.DEBUG_LAUNCHER, e);
			Debug.POLICY.logError(e, Messages.getString("PdtRemoteLaunchDelegate.Pdt_Launch_Error")); //$NON-NLS-1$
			throw e;
		} finally {
			Debug.POLICY.trace(Debug.DEBUG_LAUNCHER, "Finished launcher delegate."); //$NON-NLS-1$
			
			launchProcess.removeProgressListener(progressListener);
			progressListener.interrupt();
			monitor.done();
		}
	}
}
