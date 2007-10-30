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
package org.eclipse.ptp.remotetools.internal.ssh;

import java.io.ByteArrayOutputStream;

import org.eclipse.ptp.remotetools.core.IRemoteExecutionTools;
import org.eclipse.ptp.remotetools.core.IRemoteScript;
import org.eclipse.ptp.remotetools.core.IRemoteScriptExecution;
import org.eclipse.ptp.remotetools.core.RemoteProcess;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteExecutionException;
import org.eclipse.ptp.remotetools.internal.common.RemoteScript;


/**
 * A set of facility methods for common execution patterns.
 * 
 * @author Richard Maciel
 */
public class ExecutionTools implements IRemoteExecutionTools {
	ExecutionManager manager;

	protected ExecutionTools(ExecutionManager manager) {
		this.manager = manager;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionTools#createScript()
	 */
	public IRemoteScript createScript() {
		return new RemoteScript();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionTools#executeScript(org.eclipse.ptp.remotetools.core.IRemoteScript)
	 */
	public IRemoteScriptExecution executeScript(IRemoteScript remoteScript) throws RemoteConnectionException {
		if (!(remoteScript instanceof RemoteScript)) {
			throw new IllegalArgumentException();
		}

		
		ScriptExecution actualExecution = new ScriptExecution(manager, (RemoteScript) remoteScript);
		actualExecution.startExecution();


		return actualExecution;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionTools#executeCommand(java.lang.String)
	 */
	public int executeWithExitValue(String command) throws RemoteExecutionException, RemoteConnectionException, CancelException {
		IRemoteScript script = createScript();
		script.setScript(command);
	
		IRemoteScriptExecution execution = executeScript(script);
		execution.waitForEndOfExecution();
		int returncode = execution.getReturnCode();
		execution.close();

		return returncode;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteExecutionTools#executeCommandWithOutput(java.lang.String)
	 */
	public String executeWithOutput(String command) throws RemoteExecutionException, RemoteConnectionException, CancelException {
		IRemoteScript script = createScript();
		script.setScript(command);
		
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		script.setProcessOutputStream(baos);
		
		IRemoteScriptExecution execution = executeScript(script);
		execution.waitForEndOfExecution();
		execution.close();

		return baos.toString();
	}

	public void executeBashCommand(String command) throws RemoteExecutionException, CancelException, RemoteConnectionException {
		IRemoteScript script = createScript();
		script.setScript(command);
	
		ByteArrayOutputStream baos1 = new ByteArrayOutputStream();
		script.setProcessOutputStream(baos1);
		ByteArrayOutputStream baos2 = new ByteArrayOutputStream();
		script.setProcessErrorStream(baos2);
		
		IRemoteScriptExecution execution = executeScript(script);
		execution.waitForEndOfExecution();
		
		int returncode = execution.getReturnCode();
		execution.close();
		
		if (returncode != 0) {
			throw new RemoteExecutionException(Messages.ExecutionTools_ExecuteBashCommand_FailedRunBashCommand, returncode, baos1.toString(), baos2.toString());
		}
	}

	public RemoteProcess executeProcess(IRemoteScript remoteScript) throws RemoteExecutionException, CancelException, RemoteConnectionException {
		remoteScript.setFetchProcessErrorStream(true);
		remoteScript.setFetchProcessInputStream(true);
		remoteScript.setFetchProcessOutputStream(true);
		IRemoteScriptExecution execution = executeScript(remoteScript);
		SSHRemoteProcess process = new SSHRemoteProcess(execution);
		return process;
	}


}
