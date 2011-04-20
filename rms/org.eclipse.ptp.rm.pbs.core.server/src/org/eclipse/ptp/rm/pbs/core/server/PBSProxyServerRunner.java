/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.core.server;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.core.server.AbstractRemoteServerRunner;

public class PBSProxyServerRunner extends AbstractRemoteServerRunner {
	public static final String SERVER_ID = "org.eclipse.ptp.rm.pbs.PBSProxyServer"; //$NON-NLS-1$

	private static final String SUCCESS_STRING = "PBSProxyRuntimeServer started"; //$NON-NLS-1$

	public PBSProxyServerRunner() {
		super("PBS Proxy Server"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#doFinishServer
	 * ()
	 */
	@Override
	protected void doServerFinished(IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.done();
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#doRestartServer
	 * ()
	 */
	@Override
	protected boolean doServerStarting(IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.done();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#doStartServer
	 * ()
	 */
	@Override
	protected boolean doServerStarted(IProgressMonitor monitor) {
		if (monitor != null) {
			monitor.done();
		}
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#
	 * doVerifyServerRunningFromStderr(java.lang.String)
	 */
	@Override
	protected boolean doVerifyServerRunningFromStderr(String output) {
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.launch.core.AbstractRemoteServerRunner#
	 * doVerifyServerRunningFromStdout(java.lang.String)
	 */
	@Override
	protected boolean doVerifyServerRunningFromStdout(String output) {
		if (output.startsWith(SUCCESS_STRING)) {
			return true;
		}
		return false;
	}
}
