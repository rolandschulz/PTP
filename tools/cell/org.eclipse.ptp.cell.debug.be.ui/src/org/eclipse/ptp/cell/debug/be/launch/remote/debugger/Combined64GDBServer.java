/**
 * Copyright (c) 2006 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - Initial Implementation
 *
 */
package org.eclipse.ptp.cell.debug.be.launch.remote.debugger;

import org.eclipse.ptp.cell.debug.be.preferences.BEDebugPreferencesConstants;
import org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchRemoteDebugConfiguration;


/**
 * @author Ricardo M. Matinata
 * @since 1.3
 *
 */
public class Combined64GDBServer implements ICellDebugLaunchRemoteDebugConfiguration {

	private String dbgBinaryName = RemoteDBGBEDefaults.getString("Combined64GDBServer.0"); //$NON-NLS-1$
	private String dbgPort = RemoteDBGBEDefaults.getString("Combined64GDBServer.1"); //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchRemoteDebugConfiguration#getDbgBinaryName()
	 */
	public String getDbgBinaryName() {
		String gdbserver = BEDebugPreferencesConstants.getBE_GDBSERVER_BIN_VALUE();
		return (gdbserver != null && !gdbserver.equals("") ? gdbserver : dbgBinaryName); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchRemoteDebugConfiguration#getDbgPort()
	 */
	public String getDbgPort() {
		String gdbport = BEDebugPreferencesConstants.getBE_GDBSERVER_PORT_VALUE();
		return (gdbport != null && !gdbport.equals("") ? gdbport : dbgPort); //$NON-NLS-1$
	}

}
