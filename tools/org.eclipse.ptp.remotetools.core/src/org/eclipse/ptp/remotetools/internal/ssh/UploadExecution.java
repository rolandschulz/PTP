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
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.core.IRemoteUploadExecution;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.Debug;

import com.jcraft.jsch.ChannelExec;

public class UploadExecution extends KillableExecution implements IRemoteUploadExecution {

	String remoteFile;
	InputStream sourceStream;
	ByteArrayOutputStream errorStream;
	OutputStream outputStream;
	
	public UploadExecution(ExecutionManager executionManager, String remoteFile, InputStream source) throws RemoteConnectionException {
		super(executionManager);
		this.sourceStream = source;
		this.remoteFile = remoteFile;
		errorStream = new ByteArrayOutputStream();
	}

	public OutputStream getOutputStreamToProcessRemoteFile() {
		if (sourceStream != null) {
			throw new IllegalStateException();
		}
		return outputStream;
	}

	public void startExecution() throws RemoteConnectionException {
		
		ChannelExec channel = createChannel(false);
		IRemotePathTools pathTool = getExecutionManager().getRemotePathTools();
		setCommandLine("cat >" + pathTool.quote(remoteFile, true)); //$NON-NLS-1$
		
		if (sourceStream != null) {
			channel.setInputStream(sourceStream);
			outputStream = null;
		} else {
			try {
				outputStream = channel.getOutputStream();
			} catch (IOException e) {
				throw new RemoteConnectionException(Messages.UploadExecution_StartExecution_FailedCreateUpload, e);
			}
		}
		channel.setErrStream(errorStream);
		
		super.startExecution();
		Debug.println("Uploading " + remoteFile); //$NON-NLS-1$
		
		// Must wait the channel to open or we can have a racing on process
		// trying to manipulate non-existent files (e.g. set file attributes after
		// the upload)
		while(!channel.isClosed() && !channel.isConnected()) {
			synchronized (this) {
				try {
					this.wait(10);
				} catch (InterruptedException e) {
					// Ignore
				}
			}
		}
	}

	public String getErrorMessage() {
		return errorStream.toString();
	}
}
