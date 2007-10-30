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
package org.eclipse.ptp.remotetools.environment.launcher.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.cdt.core.model.ICProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remotetools.environment.launcher.RemoteLauncherPlugin;
import org.eclipse.ptp.remotetools.environment.launcher.core.IRemoteLaunchErrors;

/**
 * Stores configuration how the remote launch it to be executed.
 * @author Daniel Felix Ferber
 */
public class ExecutionConfiguration {

	ICProject cProject = null; // required
	File executable = null; // required
	boolean doConnectStreams = true;
	boolean doForwardX11 = false;
	boolean doAllocateTerminal = true;
	
	IPath remoteDirectory = null; // required
	File workingDirectory = null; // required
	List arguments = new ArrayList();
	
	List environmentVariables = new ArrayList();
	String beforeCommand = null; // defaults to nothing
	String afterCommand = null; // defaults to nothing
	boolean synchronizeAfter = false;
	boolean synchronizeBefore = false;
	boolean doCleanup = false;
	
	List synchronizationRules = new ArrayList();
	
	String label;
	
	public String getLabel() {
		return label;
	}
	
	public void setLabel(String label) {
		this.label = label;
	}

	public boolean getDoCleanup() {
		return doCleanup;
	}

	public void setDoCleanup(boolean doCleanup) {
		this.doCleanup = doCleanup;
	}

	public String[] getArgumentsArray() {
		String [] array = new String[arguments.size()];
		arguments.toArray(array);
		return array;
	}
	
	public void addArgument(String argument) {
		arguments.add(argument);
	}

	public void addArguments(String[] argumentsArray) {
		for (int i = 0; i < argumentsArray.length; i++) {
			String argument = argumentsArray[i];
			addArgument(argument);
		}
	}

	public boolean getDoConnectStreams() {
		return doConnectStreams;
	}

	public void setDoConnectStream(boolean doConnect) {
		this.doConnectStreams = doConnect;
	}
	
	public boolean getDoForwardX11() {
		return doForwardX11;
	}

	public void setDoForwardX11(boolean doForward) {
		this.doForwardX11 = doForward;
	}

	public ICProject getProject() {
		return cProject;
	}

	public void setProject(ICProject project) {
		cProject = project;
	}

	public String[] getEnvironmentVariablesArray() {
		String [] array = new String[environmentVariables.size()];
		environmentVariables.toArray(array);
		return array;
	}

	public void addEnvironmentVariable(String variable) {
		environmentVariables.add(variable);
	}

	public void addEnvironmentVariables(String[] environmentArray) {
		for (int i = 0; i < environmentArray.length; i++) {
			String string = environmentArray[i];
			addEnvironmentVariable(string);
		}
	}	

	public File getExecutableFile() {
		return executable;
	}

	public void setExecutable(File executable) {
		this.executable = executable;
	}

	public IPath getRemoteDirectoryPath() {
		return remoteDirectory;
	}

	static int counter = 0;
	
	public void setRemoteDirectory(IPath remoteDirectory) {
		//this.remoteDirectory = remoteDirectory.append(Integer.toString(counter++));
		this.remoteDirectory = remoteDirectory;
	}

	public File getWorkingDirectoryFile() {
		return workingDirectory;
	}
	
	public void setWorkingDirectory(File workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	public IPath getRemoteExecutablePath() {
		IPath localExecutable = new Path(executable.getPath());
		IPath remoteExecutable = remoteDirectory.append(localExecutable.lastSegment());
		return remoteExecutable;
	}
	
	public String getRemoteExecutableName() {
		return getRemoteExecutablePath().lastSegment();
	}

	public void setBeforeCommand(String beforeCommand) {
		this.beforeCommand = beforeCommand;
	}

	public void setAfterCommand(String afterCommand) {
		this.afterCommand = afterCommand;		
	}

	public void setDoSynchronizeAfter(boolean synchronizeAfter) {
		this.synchronizeAfter = synchronizeAfter;
	}
	
	public void setDoSynchronizeBefore(boolean synchronizeBefore) {
		this.synchronizeBefore = synchronizeBefore;
	}

	public String getAfterCommand() {
		return afterCommand;
	}

	public String getBeforeCommand() {
		return beforeCommand;
	}

	public boolean getDoSynchronizeAfter() {
		return synchronizeAfter;
	}

	public boolean getDoSynchronizeBefore() {
		return synchronizeBefore;
	}

	public ISynchronizationRule[] getSynchronizationRulesArray() {
		ISynchronizationRule[] array = (ISynchronizationRule[]) synchronizationRules.toArray(new ISynchronizationRule[synchronizationRules.size()]);
		return array;
	}

	public void addSynchronizationRule(ISynchronizationRule rule) {
		synchronizationRules.add(rule);
	}

	public void addSynchronizationRules(ISynchronizationRule []rules) {
		for (int i = 0; i < rules.length; i++) {
			ISynchronizationRule rule = rules[i];
			addSynchronizationRule(rule);
		}
	}	
	
	public int countSynchronizationRules() {
		return synchronizationRules.size();
	}
	
	public int countUploadRules() {
		int result = 0;
		for (Iterator iter = synchronizationRules.iterator(); iter.hasNext();) {
			ISynchronizationRule element = (ISynchronizationRule) iter.next();
			if (element.isUploadRule()) {
				result++;
			}
		}
		return result;
	}
	
	public int countDownloadRules() {
		int result = 0;
		for (Iterator iter = synchronizationRules.iterator(); iter.hasNext();) {
			ISynchronizationRule element = (ISynchronizationRule) iter.next();
			if (element.isDownloadRule()) {
				result++;
			}
		}
		return result;
	}

	public void setDoAllocateTerminal(boolean allocateTerminal) {
		this.doAllocateTerminal = allocateTerminal;
	}
	
	public boolean getDoAllocateTerminal() {
		return doAllocateTerminal;
	}
	
	/**
	 * Validate the configuration and raise a {@link CoreException} if some attribute is missing
	 * or invalid.
	 */
	public void validate() throws CoreException {
		if (cProject == null) {
			RemoteLauncherPlugin.throwCoreException(Messages.ExecutionConfiguration_Error_MissingProject, IRemoteLaunchErrors.INVALID_EXECUTION_CONFIGURATION);
		}
		if (executable == null) {
			RemoteLauncherPlugin.throwCoreException(Messages.ExecutionConfiguration_Error_MissingExecutable, IRemoteLaunchErrors.INVALID_EXECUTION_CONFIGURATION);
		}
		if (remoteDirectory == null) {
			RemoteLauncherPlugin.throwCoreException(Messages.ExecutionConfiguration_Error_MissingRemoteWorkingDirectory, IRemoteLaunchErrors.INVALID_EXECUTION_CONFIGURATION);
		}
		if (workingDirectory == null) {
			RemoteLauncherPlugin.throwCoreException(Messages.ExecutionConfiguration_Error_MissingLocalRemoteDirectory, IRemoteLaunchErrors.INVALID_EXECUTION_CONFIGURATION);
		}
		
	}
}
