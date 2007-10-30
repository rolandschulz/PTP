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

import org.eclipse.ptp.remotetools.core.IRemoteDownloadExecution;
import org.eclipse.ptp.remotetools.core.IRemotePathTools;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.internal.common.Debug;

import com.jcraft.jsch.ChannelExec;

public class DownloadExecution extends KillableExecution implements IRemoteDownloadExecution {

	String remoteFile;
	OutputStream sinkStream;
	ByteArrayOutputStream errorStream;
	InputStream inputStream;
	
	public DownloadExecution(ExecutionManager executionManager, String remoteFile, OutputStream sink) throws RemoteConnectionException {
		super(executionManager);
		this.remoteFile = remoteFile;
		this.sinkStream = sink;
		errorStream = new ByteArrayOutputStream();
	}

	public InputStream getInputStreamFromProcessRemoteFile() throws IOException {
		if (sinkStream != null) {
			throw new IllegalStateException();
		}
		return inputStream;
	}

	public void startExecution() throws RemoteConnectionException {
		ChannelExec channel = createChannel(false);
		IRemotePathTools pathTool = getExecutionManager().getRemotePathTools();
		setCommandLine("cat " + pathTool.quote(remoteFile, true)); //$NON-NLS-1$
		
		if (sinkStream != null) {
			channel.setOutputStream(sinkStream);
			inputStream = null;
		} else {
			try {
				inputStream = channel.getInputStream();
			} catch (IOException e) {
				throw new RemoteConnectionException(Messages.DownloadExecution_DownloadExecution_FailedCreateDownload, e);
			}
		}
		channel.setErrStream(errorStream);
		
		super.startExecution();
		Debug.println("Downloading " + remoteFile); //$NON-NLS-1$
	}
	
	public String getErrorMessage() {
		return errorStream.toString();
	}

}
