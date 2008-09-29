/******************************************************************************
 * Copyright (c) 2007 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *

*****************************************************************************/
package org.eclipse.ptp.cell.environment.launcher.cellbe;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchManager;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.ITargetLaunchAttributes;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.Messages;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.integration.DebugIntegration;
import org.eclipse.ptp.cell.environment.launcher.cellbe.internal.integration.RunIntegration;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.control.ITargetStatus;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchIntegration;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchObserver;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProcess;
import org.eclipse.ptp.remotetools.environment.launcher.core.NullLaunchIntegration;
import org.eclipse.ptp.remotetools.environment.launcher.core.RemoteLaunchDelegate;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;


/**
 * Use it if you need to create a new launch delegate for Cell B.E. machine 
 * and pretend to customize only a few operations.
 * 
 * @author Daniel Ferber and Richard Maciel
 *
 */
public abstract class AbstractCellRemoteLaunchDelegate extends RemoteLaunchDelegate {

	/**
	 * Return the {@link ITargetControl} selected in the launch configuration.
	 * 
	 * @param configuration The launch configuration
	 * @return The {@link ITargetControl} or <code>null</code> no valid one is selected.
	 * 
	 * @throws CoreException On failure the get the attribute.
	 */
	public ITargetControl getTargetControl(ILaunchConfiguration configuration) throws CoreException {
		// ok
		String id = configuration.getAttribute(ITargetLaunchAttributes.ATTR_TARGET_ID, (String)null);
		ITargetControl control = null;
		if ((id == null) || (id.length() == 0)) {
			return null;
		}

		control = EnvironmentPlugin.getDefault().getTargetsManager().selectControl(id);	
		if (control == null) {
			return null;
		}
		
		return control;
	}
	
	/**
	 * Return the {@link ITargetControl} selected in the launch configuration.
	 * 
	 * @param configuration
	 *            The launch configuration
	 * @return The {@link ITargetControl}.
	 * 
	 * @throws CoreException
	 *             <ul>
	 *             <li>On failure the get the attribute.
	 *             <li>If not target is selected.
	 *             <li>If the selected target does not exist.
	 *             <li>If the target is not PAUSED or RESUMED.
	 *             </ul>
	 */
	public ITargetControl getValidatedTargetControl(ILaunchConfiguration configuration) throws CoreException {
		// ok
		String id = configuration.getAttribute(ITargetLaunchAttributes.ATTR_TARGET_ID, (String)null);
		ITargetControl control = null;
		if ((id == null) || (id.length() == 0)) {
			abort(Messages.CellLaunchDelegate_TargetControl_ErrorNoTargetSelected, null, 0);
		}

		control = EnvironmentPlugin.getDefault().getTargetsManager().selectControl(id);	
		if (control == null) {
			abort(NLS.bind(Messages.CellLaunchDelegate_TargetControl_ErrorInexistentTarget, id), null, 0);
		}
		
		switch (control.query()) {
		case ITargetStatus.PAUSED:
		case ITargetStatus.RESUMED:
			break;
		case ITargetStatus.STARTED:
			abort(NLS.bind(Messages.CellLaunchDelegate_TargetControl_ErrorTargetNotReady, id), null, 0);
			break;
		case ITargetStatus.STOPPED:
			abort(NLS.bind(Messages.CellLaunchDelegate_TargetControl_ErrorTargetNotStarted, id), null, 0);
			break;
		default:
			abort(NLS.bind(Messages.CellLaunchDelegate_TargetControl_ErrorInvalidState, id), null, 0);
		}
		
		return control;
	}
	
	/**
	 * Return the name of the {@link ITargetControl} selected in the launch configuration.
	 * 
	 * @param configuration The launch configuration
	 * @return The name.
	 * 
	 * @throws CoreException On failure the get the attribute.
	 */
	public String getTargetName(ILaunchConfiguration configuration) throws CoreException {
		// ok
		return configuration.getAttribute(ITargetLaunchAttributes.ATTR_TARGET_ID, (String)null);
	}

	public void launch(ILaunchConfiguration configuration, String mode,
			ILaunch launch, IProgressMonitor monitor) throws CoreException {
		/*
		 * Get the target controller chosen for the launch and the optional
		 * launch observer that will parse input.
		 * The progress listener is not implemented yet, but will serve as an agent
		 * that communicates with the debugger plug-in.
		 */
		ITargetControl targetControl = getValidatedTargetControl(configuration);
		ILaunchObserver launchObserver = getOutputObserver(configuration);
		ILaunchIntegration launchIntegration = null;
		if (mode.equals(ILaunchManager.DEBUG_MODE)) {
			launchIntegration = new DebugIntegration(configuration, mode, launch, new NullProgressMonitor());
		} else if (mode.equals(ILaunchManager.RUN_MODE)) {
			launchIntegration = new RunIntegration();			
		} else {
			launchIntegration = new NullLaunchIntegration();
		}
		
		ExecutionConfiguration executionConfig = createExecutionConfig(configuration);
		
		/*
		 * Create a remote execution job, set the contribution objects
		 * and add this job to the target controller.
		 */
		ILaunchProcess launchProcess = RemoteLauncherPlugin.createRemoteLaunchProcess(launch, executionConfig, launchIntegration);
		launchProcess.setLaunchObserver(launchObserver);
		LaunchProcessWrapper wrapper = new LaunchProcessWrapper(launchProcess);

		/*
		 * Create a progress queue, with callback, to control de progress monitor.
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
		} finally {
			launchProcess.removeProgressListener(progressListener);
			progressListener.interrupt();
			monitor.done();
		}
	}

	/**
	 * Create a configuration object from launch configuration.
	 * Each getter does verification of the values.
	 * 
	 * @param configuration
	 * @return
	 * @throws CoreException
	 */
	public ExecutionConfiguration createExecutionConfig(
			ILaunchConfiguration configuration) throws CoreException {
		ExecutionConfiguration executionConfig = new ExecutionConfiguration();
		executionConfig.setProject(getCProject(configuration));
		executionConfig.setExecutable(verifyProgramPath(configuration).toFile());
		executionConfig.setBeforeCommand(getBeforeCommand(configuration));
		executionConfig.setAfterCommand(getAfterCommand(configuration));
		executionConfig.addArguments(getProgramArgumentsArray(configuration));
		executionConfig.setRemoteDirectory(getValidatedRemoteDirectory(configuration));
		executionConfig.setWorkingDirectory(getWorkingDirectory(configuration));
		executionConfig.setDoSynchronizeAfter(getSynchronizeAfter(configuration));
		executionConfig.setDoSynchronizeBefore(getSynchronizeBefore(configuration));
		executionConfig.setDoCleanup(getSynchronizeCleanup(configuration));
		executionConfig.addEnvironmentVariables(getEnvironment(configuration));
		executionConfig.setDoForwardX11(getUseForwardedX11(configuration));
		executionConfig.setLabel(NLS.bind(Messages.CellLaunchDelegate_LaunchLabel, new Object [] { getProgramName(configuration), getTargetName(configuration)}));
//		executionConfig.setAfterFiles(getAfterFiles(configuration));
//		executionConfig.setBeforeFiles(getBeforeFiles(configuration));
		executionConfig.setDoAllocateTerminal(getAllocateTerminal(configuration));
		executionConfig.addSynchronizationRules(getSynchronizeRules(configuration));
		executionConfig.validate();
		return executionConfig;
	}

}
