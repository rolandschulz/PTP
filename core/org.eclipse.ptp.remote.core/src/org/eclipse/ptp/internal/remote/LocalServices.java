/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote;

import java.util.List;

import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.IRemoteServicesDelegate;


public class LocalServices implements IRemoteServicesDelegate {
	private IRemoteConnectionManager connMgr;
	private IRemoteFileManager fileMgr;
	
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String>command) {
		return new LocalProcessBuilder(conn, command);
	}
	
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		return new LocalProcessBuilder(conn, command);
	}
	
	public IRemoteConnectionManager getConnectionManager() {
		return connMgr;
	}
	
	public IRemoteFileManager getFileManager() {
		return fileMgr;
	}
	
	public boolean initialize() {
		connMgr = new LocalConnectionManager();
		fileMgr = new LocalFileManager();
		return true;
	}
}
