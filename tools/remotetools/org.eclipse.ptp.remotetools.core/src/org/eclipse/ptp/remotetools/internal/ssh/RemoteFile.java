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


import org.eclipse.ptp.remotetools.core.IRemoteFile;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * @author Richard Maciel
 *
 */
public class RemoteFile extends RemoteItem implements IRemoteFile {

	RemoteFile(FileTools fileTools, String path, SftpATTRS attrs) {
		super(fileTools, path, attrs);
	}

	RemoteFile(FileTools fileTools, String path) {
		super(fileTools, path);
	}
	
	public RemoteFile(RemoteItem item) {
		super();
		copyAttributesFrom(item);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteFile#getSize()
	 */
	public long getSize() {
		return size;
	}

	public boolean isExecutable() {
		return super.isExecutable();
	}
	
	public void setExecutable(boolean flag) {
		super.setExecutable(flag);
	}
}
