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

import org.eclipse.ptp.remotetools.core.IRemoteDirectory;
import org.eclipse.ptp.remotetools.exception.CancelException;
import org.eclipse.ptp.remotetools.exception.RemoteConnectionException;
import org.eclipse.ptp.remotetools.exception.RemoteOperationException;

import com.jcraft.jsch.SftpATTRS;
import com.jcraft.jsch.ChannelSftp.LsEntry;

/**
 * @author Richard Maciel
 *
 */
public class RemoteDirectory extends RemoteItem implements
		IRemoteDirectory {

	RemoteDirectory(FileTools fileTools, String path) {
		super(fileTools, path);
	}
	
	public RemoteDirectory(RemoteItem item) {
		super();
		copyAttributesFrom(item);
	}

	RemoteDirectory(FileTools fileTools, String path, SftpATTRS attrs) {
		super(fileTools, path, attrs);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remotetools.core.IRemoteDirectory#isAccessible()
	 */
	public boolean isAccessible() {
		return super.isExecutable();
	}
	
	public void setAccessible(boolean flag) throws RemoteConnectionException, CancelException {
		super.setExecutable(flag);
	}


}
