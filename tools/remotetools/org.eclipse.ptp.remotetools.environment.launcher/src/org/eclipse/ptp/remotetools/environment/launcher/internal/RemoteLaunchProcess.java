/******************************************************************************
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 *****************************************************************************/
package org.eclipse.ptp.remotetools.environment.launcher.internal;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.IStreamListener;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IStreamMonitor;
import org.eclipse.debug.core.model.IStreamsProxy;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.remotetools.core.IRemoteDirectory;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionManager;
import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.core.IRemoteFileTools;
import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.IRemoteScriptExecution;
import org.eclipse.ptp.remotetools.core.RemoteProcess;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchIntegration;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchObserver;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProcess;
import org.eclipse.ptp.remotetools.environment.launcher.core.ILaunchProgressListener;
import org.eclipse.ptp.remotetools.environment.launcher.core.LinuxPath;
import org.eclipse.ptp.remotetools.environment.launcher.core.NullLaunchIntegration;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionConfiguration;
import org.eclipse.ptp.remotetools.environment.launcher.data.ExecutionResult;
import org.eclipse.ptp.remotetools.environment.launcher.data.ISynchronizationRule;
import org.eclipse.ptp.remotetools.environment.launcher.internal.integration.NullLaunchObserver;
import org.eclipse.ptp.remotetools.environment.launcher.internal.process.TargetProcess;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;
import org.eclipse.ptp.remotetools.utils.linux.commandline.ArgumentParser;
import org.eclipse.ui.console.ConsolePlugin;
import org.eclipse.ui.console.IConsole;
import org.eclipse.ui.console.IConsoleManager;


public class RemoteLaunchProcess implements ILaunchProcess, ILaunchProcessCallback {

	int currentProgress = ILaunchProgressListener.UNDEFINED;

	public ILaunchObserver observer = new NullLaunchObserver();
	public ILaunchIntegration launchIntegration = new NullLaunchIntegration();
	public Set progressListeners = new HashSet();
	
	OutputStream launchProcessOutputStream = null;
	OutputStream launchProcessErrorStream = null;
	
	PrintWriter launchProcessOutputWriter = null;
	PrintWriter launchProcessErrorWriter = null;
	
	ExecutionConfiguration configuration = null;
	ExecutionResult executionResult = null;
	List extraSynchronizationRules = new ArrayList();
	
	IProcess applicationProgress;
	TargetProcess targetProcess;
	ILaunch launch;
	IRemoteExecutionManager manager;

	String[] launchScript;

	public RemoteLaunchProcess(ILaunch launch, ExecutionConfiguration configuration, ILaunchIntegration launchIntegration) {
		super();
		this.launch = launch;
		this.configuration = configuration;
		this.currentProgress = ILaunchProgressListener.WAIT;
		this.executionResult = null;
		if (launchIntegration == null) {
			this.launchIntegration = new NullLaunchIntegration();
		} else {
			this.launchIntegration = launchIntegration;
		}
	}

	public synchronized void markAsCanceled() {
		launchProcessErrorWriter.println(Messages.RemoteLaunchProcess_RequestToCancelLaunch);
		manager.cancel();
	}
	
	public synchronized ILaunchObserver getObserver() {
		return observer;
	}

	public synchronized ILaunchIntegration getLaunchIntegration() {
		return launchIntegration;
	}

	protected synchronized void setCurrentProgress(int newProgress) {
		this.currentProgress = newProgress;
		Iterator iterator = progressListeners.iterator();
		while (iterator.hasNext()) {
			ILaunchProgressListener listener = (ILaunchProgressListener) iterator.next();
			listener.notifyProgress(newProgress);
		}
	}
	
	protected synchronized void notifyInterrupt() {
		Iterator iterator = progressListeners.iterator();
		while (iterator.hasNext()) {
			ILaunchProgressListener listener = (ILaunchProgressListener) iterator.next();
			listener.notifyInterrupt();
		}
	}

	public synchronized int getCurrentProgress() {
		return currentProgress;
	}

	public synchronized ExecutionResult getFinalResult() {
		return executionResult;
	}
	
	public synchronized ExecutionConfiguration getConfiguration() {
		return configuration;
	}

	protected void prepareWorkingDir() throws CoreException, CancelException {
		/*
		 * Only create the remote working directory if some step will require it.
		 */
		boolean doCreate = false;
		if (configuration.getDoSynchronizeAfter() || configuration.getDoSynchronizeBefore()
				|| configuration.getDoCleanup() || launchIntegration.getDoLaunchApplication()) {
			doCreate = true;
		}
		if (! doCreate) {
			launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_PrepareWorkingDir_NotRequired);
			return;
		}
		
		/*
		 * Assure that the remote working directory exists. Create if necessary.
		 */
		IPath remotePath = configuration.getRemoteDirectoryPath();
		String remoteDirectoryAsPath = LinuxPath.toString(remotePath);
		launchProcessOutputWriter.println(NLS.bind(Messages.RemoteLaunchProcess_PrepareWorkingDir_Title, remoteDirectoryAsPath));
		try {
			IRemoteFileTools fileTools = manager.getRemoteFileTools();
			fileTools.createDirectory(remoteDirectoryAsPath);
		} catch (RemoteOperationException e) {
			launchProcessErrorWriter.println(NLS.bind(Messages.RemoteLaunchProcess_PrepareWorkingDir_FailedCreate, e.getMessage()));
			launchProcessErrorWriter.println(Messages.RemoteLaunchProcess_PrepareWorkingDir_FailedCreateHint);
			abortWithError(Messages.RemoteLaunchProcess_PrepareWorkingDir_Failed, e);
		} catch (RemoteConnectionException e) {
			abortWithError(Messages.RemoteLaunchProcess_All_FailedConnection, e);
		}
		
		/*
		 * Assure permissions so that the user can enter and write the directory.
		 */
		try {
			IRemoteFileTools fileTools = manager.getRemoteFileTools();
			IRemoteDirectory remoteExecutable = fileTools.getDirectory(remoteDirectoryAsPath);
			remoteExecutable.setAccessible(true);
			remoteExecutable.setReadable(true);
			remoteExecutable.setWriteable(true);
			remoteExecutable.commitAttributes();
		} catch (RemoteOperationException e) {
			launchProcessErrorWriter.println(NLS.bind(Messages.RemoteLaunchProcess_PrepareWorkingDir_FailedPermissions, e.getMessage()));
			abortWithError(Messages.RemoteLaunchProcess_PrepareWorkingDir_Failed, e);
		} catch (RemoteConnectionException e) {
			abortWithError(Messages.RemoteLaunchProcess_All_FailedConnection, e);
		}
	}

	protected void uploadWorkingDirectory() throws CoreException, CancelException {
		/*
		 * Only run upload if this feature was enabled.
		 */
		if (! configuration.getDoSynchronizeBefore()) {
			launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_UploadWorkingDirectory_TitleUploadDisabled);
			return;
		}

		launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_UploadWorkingDirectory_Title);
		
		/*
		 * Only run upload if rules are available.
		 */
		if (configuration.countUploadRules() <= 0) {
			launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_UploadWorkingDirectory_NoRules);
			return;
		}
		
//		List rules_tmp = new ArrayList(Arrays.asList(configuration.getSynchronizationRulesArray()));
//		rules_tmp.addAll(extraSynchronizationRules);
//		ISynchronizationRule [] rules = (ISynchronizationRule[]) rules_tmp.toArray(new ISynchronizationRule[rules_tmp.size()]);
		ISynchronizationRule [] rules = configuration.getSynchronizationRulesArray();
		RuleActionFactory factory = new RuleActionFactory(this);
		for (int i = 0; i < rules.length; i++) {
			ISynchronizationRule rule = rules[i];
			if (! rule.isUploadRule()) {
				continue;
			}
			if (! rule.isActive()) {
				String message = NLS.bind(Messages.RemoteLaunchProcess_UploadWorkingDirectory_IgnoreInactive, Integer.toString(i));
				launchProcessOutputWriter.println(message);
				continue;
			}
			try {
				rule.validate();
			} catch (CoreException e) {
				String message = NLS.bind(Messages.RemoteLaunchProcess_UploadWorkingDirectory_IgnoreInvalid, new Object [] { Integer.toString(i), e.getMessage()});
				launchProcessErrorWriter.println(message);
				continue;
			}
			IRuleAction action = factory.getAction(rule);
			try {
				action.run();
			} catch (CoreException e) {
				String message = NLS.bind(Messages.RemoteLaunchProcess_UploadWorkingDirectory_FailedRule, new Object [] { Integer.toString(i), e.getMessage()});
				launchProcessOutputWriter.println(message);
			} catch (RemoteConnectionException e) {
				abortWithError(Messages.RemoteLaunchProcess_All_FailedConnection, e);
			}
		}
	}

	protected void uploadApplication() throws CoreException, CancelException {
		if (! launchIntegration.getDoLaunchApplication()) { 
			launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_UploadApplication_TitleNoUpload);
			return;
		}
		launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_UploadApplication_Title);
		
		// Shortcut to often used values
		IPath remoteDirectoryPath = configuration.getRemoteDirectoryPath();
		IPath remoteExecutablePath = configuration.getRemoteExecutablePath();
		String remoteDirectory = LinuxPath.toString(remoteDirectoryPath);
		String remoteExecutable = LinuxPath.toString(remoteExecutablePath);
		File executableFile = configuration.getExecutableFile();
		
		try {
			launchProcessOutputWriter.println(NLS.bind(Messages.RemoteLaunchProcess_UploadApplication_UploadMessage, new Object [] {executableFile.getCanonicalPath(), remoteExecutable}));
		} catch (IOException e) {
			// Ignore
		}
		
		/*
		 * Copy executable to working directory
		 */
		try {
			manager.getRemoteCopyTools().uploadFileToDir(executableFile, remoteDirectory);
			launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_UploadApplication_CompletedUpload);
		} catch (RemoteOperationException e) {
			launchProcessErrorWriter.println(NLS.bind(Messages.RemoteLaunchProcess_UploadApplication_FailedUpload, e.getMessage()));
			abortWithError(Messages.RemoteLaunchProcess_Failed, e);
		} catch (RemoteConnectionException e) {
			abortWithError(Messages.RemoteLaunchProcess_All_FailedConnection, e);
		}

		/*
		 * Assure executable permissions.
		 */
		try {
			IRemoteFileTools fileTools = manager.getRemoteFileTools();
			IRemoteFile remoteFile = fileTools.getFile(remoteExecutable);
			remoteFile.setReadable(true);
			remoteFile.setExecutable(true);
			remoteFile.commitAttributes();
		} catch (RemoteOperationException e) {
			launchProcessErrorWriter.println(NLS.bind(Messages.RemoteLaunchProcess_FailedPermissions, e.getMessage()));
			abortWithError(Messages.RemoteLaunchProcess_Failed, e);
		} catch (RemoteConnectionException e) {
			abortWithError(Messages.RemoteLaunchProcess_All_FailedConnection, e);
		}
	}

	protected void downloadWorkingDirectory() throws CoreException, CancelException {
		/*
		 * Only run download if this feature was enabled.
		 */
		if (! configuration.getDoSynchronizeAfter() && extraSynchronizationRules.size() == 0) {
			launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_DownloadWorkingDirectory_TitleDownloadDisabled);
			return;
		}

		launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_DownloadWorkingDirectory_Title);
		
		/*
		 * Only run download if rules are available.
		 */
		if (configuration.countDownloadRules() <= 0 && extraSynchronizationRules.size() == 0) {
			launchProcessOutputWriter.println(Messages.RemoteLaunchProcess_UploadWorkingDirectory_NoRules);
			return;
		}
		
		List rules_tmp = new ArrayList(Arrays.asList(configuration.getSynchronizationRulesArray()));
		rules_tmp.addAll(extraSynchronizationRules);
		ISynchronizationRule [] rules = (ISynchronizationRule[]) rules_tmp.toArray(new ISynchronizationRule[rules_tmp.size()]);
		RuleActionFactory factory = new RuleActionFactory(this);
		for (int i = 0; i < rules.length; i++) {
			ISynchronizationRule rule = rules[i];
			if (! rule.isDownloadRule()) {
				continue;
			}
			if (! rule.isActive()) {
				String message = NLS.bind(Messages.RemoteLaunchProcess_UploadWorkingDirectory_IgnoreInactive, Integer.toString(i));
				launchProcessOutputWriter.println(message);
				continue;
			}
			try {
				rule.validate();
			} catch (CoreException e) {
				String message = NLS.bind(Messages.RemoteLaunchProcess_DownloadWorkingDirectory_IgnoreInvalid, new Object [] { Integer.toString(i), e.getMessage()});
				launchProcessErrorWriter.println(message);
				continue;
			}
			IRuleAction action = factory.getAction(rule);
			try {
				action.run();
			} catch (CoreException e) {
				String message = NLS.bind(Messages.RemoteLaunchProcess_DownloadWorkingDirectory_FailedRule, new Object [] { Integer.toString(i), e.getMessage()});
				launchProcessOutputWriter.println(message);
			} catch (RemoteConnectionException e) {
				abortWithError(Messages.RemoteLaunchProcess_All_FailedConnection, e);
			}
		}
	}

	protected void prepareApplication() throws CoreException, CancelException {
		if (! launchIntegration.getDoLaunchApplication()) return;

		try {
			String command = configuration.getBeforeCommand();
			if (command == null) {
				launchProcessOutputWriter.println("* Launch configuration does not require running a bash script before launch.");
				return;
			}
			command = command.trim();
			if (command.length() == 0) {
				launchProcessOutputWriter.println("* Launch configuration does not require running a bash script before launch.");
				return;
			}
			
			launchProcessOutputWriter.println("* Prepare launch by executing:");
			launchProcessOutputWriter.println("   " + command);
			
			IRemoteExecutionTools ret = manager.getExecutionTools();
			IRemoteScript script = ret.createScript();
			String[] environmentVariables = configuration.getEnvironmentVariablesArray();
			for (int i = 0; i < environmentVariables.length; i++) {
				script.addEnvironment(environmentVariables[i]);
			}
						
			/*
			 * Change the current directory from home to the actual working directory.
			 */
			command = "cd " + configuration.getRemoteDirectoryPath() + "\n" + command + "\n";

			script.setScript(command.split("\n"));
			
			IRemoteScriptExecution execution = ret.executeScript(script);
			execution.waitForEndOfExecution();
			if (execution.getReturnCode() == 0) {
				launchProcessOutputWriter.println("   Script executed successfully.");
			} else {
				launchProcessErrorWriter.println("   Script returned " + Integer.toString(execution.getReturnCode()));
			}
			execution.close();			
		} catch (RemoteExecutionException e) {
			launchProcessOutputWriter.println("   Script failed: " + e.getErrorMessage());
		} catch (RemoteConnectionException e) {
			abortWithError("Connection to host failed.", e);
		}		
	}

	protected void finalizeApplication() throws CoreException, CancelException {
		if (! launchIntegration.getDoLaunchApplication()) return;

		try {
			String command = configuration.getAfterCommand();
			if (command == null) {
				launchProcessOutputWriter.println("* Launch configuration does not require running a bash script after launch.");
				return;			
			}
			command = command.trim();
			if (command.length() == 0) {
				launchProcessOutputWriter.println("* Launch configuration does not require running a bash script after launch.");
				return;
			}
			
			launchProcessOutputWriter.println("* Finalize launch by executing:");
			launchProcessOutputWriter.println("   " + command);
			
			IRemoteExecutionTools ret = manager.getExecutionTools();
			IRemoteScript script = ret.createScript();
			String[] environmentVariables = configuration.getEnvironmentVariablesArray();
			for (int i = 0; i < environmentVariables.length; i++) {
				script.addEnvironment(environmentVariables[i]);
			}
			
			/*
			 * Change the current directory from home to the actual working directory.
			 */
			command = "cd " + configuration.getRemoteDirectoryPath() + "\n" + command + "\n";
			
			script.setScript(command.split("\n"));
			
			IRemoteScriptExecution execution = ret.executeScript(script);
			execution.waitForEndOfExecution();
			if (execution.getReturnCode() == 0) {
				launchProcessOutputWriter.println("   Script executed successfully.");
			} else {
				launchProcessErrorWriter.println("   Script returned " + Integer.toString(execution.getReturnCode()));
			}
			execution.close();			
		} catch (RemoteExecutionException e) {
			launchProcessOutputWriter.println("   Script failed: " + e.getErrorMessage());
		} catch (RemoteConnectionException e) {
			abortWithError("Connection to host failed.", e);
		}		
	}
	
	protected void runApplication() throws CoreException, CancelException {
		if (! launchIntegration.getDoLaunchApplication()) {
			launchProcessOutputWriter.println("* Launch configuration does not require running the executable.");
			return;
		}
		
		IRemoteExecutionTools iret = null;
		IRemoteScript script = null;
		try {
			iret = manager.getExecutionTools();
			script = iret.createScript();
		} catch (RemoteConnectionException e) {
			abortWithError("Connection to host failed.", e);
		}
		
		String[] environmentVariables = configuration.getEnvironmentVariablesArray();
		for (int i = 0; i < environmentVariables.length; i++) {
			script.addEnvironment(environmentVariables[i]);
		}
		script.setForwardX11(configuration.getDoForwardX11());

		ArrayList commandsList = new ArrayList(Arrays.asList(launchScript));
		String remoteWorkingDirectory = LinuxPath.toString(configuration.getRemoteDirectoryPath());
		IRemotePathTools pathTools = manager.getRemotePathTools();
		commandsList.add(0, "cd " + pathTools.quote(remoteWorkingDirectory, true));
		if (configuration.getDoAllocateTerminal()) {
			// If terminal is allocated, then turn off echo, since Eclipse console already
			// echo user input.
			commandsList.add(0, "stty -echo");
		}
		String commands[] = new String[commandsList.size()];
		commands = (String[]) commandsList.toArray(commands);
		script.setScript(commands);
		script.setFetchProcessErrorStream(true);
		script.setFetchProcessInputStream(true);
		script.setFetchProcessOutputStream(true);
		script.setAllocateTerminal(configuration.getDoAllocateTerminal());
		
		RemoteProcess remoteProcess = null;
		executionResult = new ExecutionResult();
		try {
			launchProcessOutputWriter.println("* Started application on target.");
			
			launchIntegration.prepareLaunch();
			remoteProcess = manager.getExecutionTools().executeProcess(script);
			applicationProgress = DebugPlugin.newProcess(launch, remoteProcess, "Remote process");
			IStreamsProxy proxy = applicationProgress.getStreamsProxy();
			if (proxy != null) {
				/*
				 * The proxy exists only when a console was created for the process.
				 */
				IStreamListener listener = new IStreamListener() {
					public void streamAppended(String text, IStreamMonitor monitor) {
						observer.receiveOutput(text);
					}
				};
				applicationProgress.getStreamsProxy().getOutputStreamMonitor().addListener(listener );
			}
			launchIntegration.finalizeLaunch();
			showProcessConsole();
			remoteProcess.waitFor();
			
			IRemoteScriptExecution execution = remoteProcess.getRemoteExecution();
			executionResult = new ExecutionResult();
			executionResult.setExitValue(execution.getReturnCode());
			
			if (execution.wasCanceled()) {
				launchProcessErrorWriter.println("   Execution was canceled");
				executionResult.setStatus(ExecutionResult.CANCELLED);
			} else if (execution.wasOK()) {
				if (execution.getReturnCode() > 0) {
					launchProcessOutputWriter.println("   Finished with exit code: " + Integer.toString(executionResult.getExitValue()));
					executionResult.setStatus(ExecutionResult.SUCCESS_WITH_CODE);
				} else {
					launchProcessOutputWriter.println("   Finished successfully");
					executionResult.setStatus(ExecutionResult.SUCCESS);
				}
			} else if (execution.wasException()) {
				launchProcessErrorWriter.println("   Finished with exception: " + execution.getFinishStatusText(execution.getFinishStatus()));
				executionResult.setStatus(ExecutionResult.EXCEPTION);				
			} else if (execution.wasCommandError()) {
				launchProcessErrorWriter.println("   Could not run application: " + execution.getFinishStatusText(execution.getFinishStatus()));
				executionResult.setStatus(ExecutionResult.COMMAND_ERROR);								
			} else {
				launchProcessErrorWriter.println("   Failed.");
				executionResult.setStatus(ExecutionResult.UNKNOWN);
			}
		} catch (CoreException e) {
			launchProcessErrorWriter.println("   Failed: " + e.getMessage());
			IStatus status = e.getStatus();
			if (status.isMultiStatus()) {
				launchProcessErrorWriter.println("     " + status.getException().getMessage());
			}
			executionResult.setStatus(ExecutionResult.ERROR);
			throw e;
		} catch (RemoteConnectionException e) {
			executionResult.setStatus(ExecutionResult.ERROR);
			abortWithError("Connection to host failed.", e);
		} catch (RemoteExecutionException e) {
			executionResult.setStatus(ExecutionResult.ERROR);
			launchProcessErrorWriter.println("   Failed to execute: " + e.getErrorMessage());
		} catch (InterruptedException e) {
			executionResult.setStatus(ExecutionResult.ERROR);
			// TODO Auto-generated catch block
		} finally {
			remoteProcess.destroy();
		}
	}

	protected void cleanUp() throws CoreException, CancelException {
		if (! configuration.getDoCleanup()) {
			launchProcessOutputWriter.println("* Launch configuration does not require cleaning up remote working directory.");
			return;
		}

		IPath remotePath = configuration.getRemoteDirectoryPath();
		String remoteDirectory = LinuxPath.toString(remotePath);
		try {
			launchProcessOutputWriter.println("* Cleaning up remote working directory: " + remoteDirectory);
			IRemoteFileTools irft = manager.getRemoteFileTools();
			irft.removeFile(remoteDirectory);
			launchProcessOutputWriter.println("   Clean up complete");
		} catch (RemoteOperationException e) {
			launchProcessErrorWriter.println("   Clean up failed: " + e.getMessage());
			abortWithError("Could not clean up target.", e);
		} catch (RemoteConnectionException e) {
			abortWithError("Connection to host failed.", e);
		}
	}

	public static String createCommandLine(String remoteExecutableName, String[] argumentsArray) {
//		String result = remoteExecutableName;
//		for (int i = 0; i < argumentsArray.length; i++) {
//			// TODO: which kind of quoting is needed?
//			String argument = argumentsArray[i].replaceAll(" ", "\\ ");
//			result += " " + argument;
//		}
//		return result;
		ArgumentParser parser = new ArgumentParser(remoteExecutableName, argumentsArray);
		return parser.getCommandLine(true);
	}

	public void run(IRemoteExecutionManager manager) {
		this.manager = manager;
		setCurrentProgress(ILaunchProgressListener.WAIT);
		
		/*
		 * Create a job that represents the entire launch.
		 */
		targetProcess = new TargetProcess(launch, this);
		addProgressListener(targetProcess);
		launchProcessOutputWriter = new PrintWriter(targetProcess.getOutputStream(), true);
		launchProcessErrorWriter = new PrintWriter(targetProcess.getErrorStream(), true);
		targetProcess.start();

		observer.setExecutionManager(manager);
		launchIntegration.setExecutionManager(manager);

		try {
			observer.start();			
			launchIntegration.start();
			
			String remexecpath = LinuxPath.toString(
					configuration.getRemoteExecutablePath());
			launchScript = launchIntegration.createLaunchScript(remexecpath, configuration.getArgumentsArray());
			
			setCurrentProgress(ILaunchProgressListener.PREPARE_WORKING_DIR1);
			prepareWorkingDir();
			launchIntegration.prepareUploadWorkingDir();

			setCurrentProgress(ILaunchProgressListener.UPLOAD_WORKING_DIR);
			uploadWorkingDirectory();

			setCurrentProgress(ILaunchProgressListener.PREPARE_WORKING_DIR2);
			launchIntegration.finalizeWorkingDir();

			setCurrentProgress(ILaunchProgressListener.UPLOAD_APPLICATION);
			uploadApplication();

			setCurrentProgress(ILaunchProgressListener.PREPARE_APPLICATION);
			prepareApplication();
			launchIntegration.prepareApplication();
			observer.prepareApplication();

			setCurrentProgress(ILaunchProgressListener.RUNNING);
			runApplication();
			
			setCurrentProgress(ILaunchProgressListener.FINALIZE_APPLICATION);
			observer.finalizeApplication();
			launchIntegration.finalizeApplication();
			finalizeApplication();
			
			setCurrentProgress(ILaunchProgressListener.DOWNLOAD_WORKING_DIR);
			downloadWorkingDirectory();
			
			setCurrentProgress(ILaunchProgressListener.FINALIZE_WORKING_DIR2);
			launchIntegration.finalizeWorkingDir();
			
			setCurrentProgress(ILaunchProgressListener.CLEANUP);
			cleanUp();

			setCurrentProgress(ILaunchProgressListener.FINALIZE_CLEANUP);
			launchIntegration.finalizeCleanup();
			
			setCurrentProgress(ILaunchProgressListener.FINISHED);
			observer.finish();
			launchIntegration.finish();

			if (executionResult.getStatus() == ExecutionResult.SUCCESS) {
				showProcessConsole();
			} else {
				showLaunchConsole();
			}
			
		} catch (CancelException e) {
			if (executionResult == null) {
				executionResult = new ExecutionResult();
				executionResult.setStatus(ExecutionResult.CANCELLED);
			}
			forcedCleanUp(manager);
//			notifyInterrupt();
		} catch (CoreException e) {
			if (executionResult == null) {
				executionResult = new ExecutionResult();
				executionResult.setStatus(ExecutionResult.ERROR);
			}
			forcedCleanUp(manager);
			launchProcessErrorWriter.println();
			launchProcessErrorWriter.println("*** The launch did not work correctly ***");
			launchProcessErrorWriter.println("    An exception was thrown during the launch: " + e.getMessage());
			launchProcessErrorWriter.println();
			notifyInterrupt();
		} finally {
			setCurrentProgress(ILaunchProgressListener.FINISHED);
			observer.cleanup();
			launchIntegration.cleanup();
		}
	}

	private void forcedCleanUp(IRemoteExecutionManager manager) {
		if (getCurrentProgress() > ILaunchProgressListener.PREPARE_APPLICATION) {
			setCurrentProgress(ILaunchProgressListener.FINALIZE_APPLICATION);
			try {
				launchIntegration.finalizeApplication();
			} catch (CancelException e) {
			} catch (CoreException e) {
			}
		}
		
		setCurrentProgress(ILaunchProgressListener.FINALIZE_WORKING_DIR2);
		try {
			launchIntegration.finalizeCleanup();
		} catch (CoreException e1) {
		} catch (CancelException e1) {
		}
		
		setCurrentProgress(ILaunchProgressListener.CLEANUP);
		try {
			cleanUp();
		} catch (CancelException e) {
		} catch (CoreException e) {
		}

		setCurrentProgress(ILaunchProgressListener.FINALIZE_CLEANUP);
		try {
			launchIntegration.finalizeCleanup();
		} catch (CoreException e) {
		} catch (CancelException e) {
		}
	}

	protected void abortWithError(String message, Exception e) throws CoreException {
		Status status = new Status(IStatus.ERROR, RemoteLauncherPlugin.getUniqueIdentifier(), 0, message, e);
		throw new CoreException(status);
	}
	
	public synchronized void addProgressListener(ILaunchProgressListener progressListener) {
		progressListeners.add(progressListener);
	}

	public synchronized void removeProgressListener(ILaunchProgressListener progressListener) {
		progressListeners.remove(progressListener);
	}

	public void setLaunchObserver(ILaunchObserver launchObserver) {
		if (launchObserver == null) {
			this.observer = new NullLaunchObserver();
		} else {
			this.observer = launchObserver;
		}
	}

	public IRemoteExecutionManager getExecutionManager() {
		return manager;
	}

	public ILaunch getLaunch() {
		return launch;
	}

	public ILaunchConfiguration getLaunchConfiguration() {
		return launch.getLaunchConfiguration();
	}

	public void showProcessConsole() {
		if (applicationProgress == null) {
			return;
		}
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] consoles = consoleManager.getConsoles();
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			if (console instanceof org.eclipse.debug.ui.console.IConsole) {
				org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
				IProcess process = processConsole.getProcess();
				if (process == applicationProgress) {
					consoleManager.showConsoleView(console);
				}
			}
		}
	}
	
	public void showLaunchConsole() {
		if (applicationProgress == null) {
			return;
		}
		IConsoleManager consoleManager = ConsolePlugin.getDefault().getConsoleManager();
		IConsole[] consoles = consoleManager.getConsoles();
		for (int i = 0; i < consoles.length; i++) {
			IConsole console = consoles[i];
			if (console instanceof org.eclipse.debug.ui.console.IConsole) {
				org.eclipse.debug.ui.console.IConsole processConsole = (org.eclipse.debug.ui.console.IConsole) console;
				IProcess process = processConsole.getProcess();
				if (process == targetProcess) {
					consoleManager.showConsoleView(console);
				}
			}
		}
	}

	public PrintWriter getErrorWriter() {
		return launchProcessErrorWriter;
	}

	public PrintWriter getOutputWriter() {
		return launchProcessOutputWriter;
	}

	public void addSynchronizationRule(ISynchronizationRule rule) {
		extraSynchronizationRules.add(rule);
	}
}
