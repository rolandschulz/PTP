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
package org.eclipse.ptp.cell.debug.launch.remote.debugger;

import org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchRemoteDebugConfiguration;
import org.eclipse.ptp.cell.debug.preferences.DebugPreferencesConstants;


/**
 * @author Ricardo M. Matinata
 * @since 1.2
 *
 */
public class SPUGDBServer implements ICellDebugLaunchRemoteDebugConfiguration {

	private String dbgBinaryName = RemoteDBGDefaults.getString("SPUGDBServer.0"); //$NON-NLS-1$
	private String dbgPort = RemoteDBGDefaults.getString("SPUGDBServer.1"); //$NON-NLS-1$
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchRemoteDebugConfiguration#getDbgBinaryName()
	 */
	public String getDbgBinaryName() {
		String gdbserver = DebugPreferencesConstants.getSPU_GDBSERVER_BIN_VALUE();
		return (gdbserver != null && !gdbserver.equals("") ? gdbserver : dbgBinaryName); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.cell.debug.launch.ICellDebugLaunchRemoteDebugConfiguration#getDbgPort()
	 */
	public String getDbgPort() {
		String gdbport = DebugPreferencesConstants.getSPU_GDBSERVER_PORT_VALUE();
		return (gdbport != null && !gdbport.equals("") ? gdbport : dbgPort); //$NON-NLS-1$
	}

}
