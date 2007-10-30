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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.remotetools.core.IRemoteScriptExecution;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.Debug;
import org.eclipse.ptp.remotetools.internal.common.RemoteScript;

import com.jcraft.jsch.ChannelExec;

/**
 * @author richardm
 * 
 */
public class ScriptExecution extends KillableExecution implements IRemoteScriptExecution {

	protected RemoteScript remoteScript;
	
	protected InputStream fromProcessOutput;
	protected InputStream fromProcessError;
	protected OutputStream toProcessInput;


	protected ScriptExecution(ExecutionManager executionManager, RemoteScript remoteScript)
			throws RemoteConnectionException {
		super(executionManager);
		this.remoteScript = remoteScript;
	}

	/*
	 * (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.internal.ssh.KillableExecution#startExecution()
	 */
	public void startExecution() throws RemoteConnectionException {
		String commandline = getRemoteScript().getScriptString();
		ChannelExec channel = createChannel(getRemoteScript().getAllocateTerminal());
		setCommandLine(commandline);

		try {
			if (remoteScript.getInputStream() != null) {
				channel.setInputStream(remoteScript.getInputStream());
			} else if (remoteScript.getFetchProcessInputStream()) {
				toProcessInput = channel.getOutputStream();
			}
		
			if (remoteScript.getOutputStream() != null) {
				channel.setOutputStream(remoteScript.getOutputStream());
			} else if (remoteScript.getFetchProcessOutputStream()) {
				fromProcessOutput = channel.getInputStream();
			}
			
			if (remoteScript.getErrorStream() != null) {
				channel.setErrStream(remoteScript.getErrorStream());
			} else if (remoteScript.getFetchProcessErrorStream()) {
				fromProcessError = channel.getErrStream();
			}
		} catch (IOException e) {
			throw new RemoteConnectionException(Messages.ScriptExecution_StartExecution_FailedInitStreams, e);
		}
		channel.setXForwarding(remoteScript.willForwardX11());

		super.startExecution();
		Debug.println("Executing " + commandline); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.IRemoteCommandExecution#getInputStreamFromProcessOutputStream()
	 */
	public InputStream getInputStreamFromProcessOutputStream() throws IOException {
		if (fromProcessOutput == null) {
			throw new IllegalStateException();
		}
		return fromProcessOutput;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.IRemoteCommandExecution#getInputStreamFromProcessErrorStream()
	 */
	public InputStream getInputStreamFromProcessErrorStream() throws IOException {
		if (fromProcessError == null) {
			throw new IllegalStateException();
		}
		return fromProcessError;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remotetools.IRemoteCommandExecution#getOutputStreamFromProcessInputStream()
	 */
	public OutputStream getOutputStreamToProcessInputStream() throws IOException {
		if (toProcessInput == null) {
			throw new IllegalStateException();
		}
		return toProcessInput;
	}

	public RemoteScript getRemoteScript() {
		return (RemoteScript) remoteScript;
	}
}
