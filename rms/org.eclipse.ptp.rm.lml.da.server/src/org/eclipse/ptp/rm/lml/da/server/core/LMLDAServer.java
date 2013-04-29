/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rm.lml.da.server.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.remote.server.core.AbstractRemoteServerRunner;
import org.eclipse.ptp.rm.lml.da.server.messages.Messages;

public class LMLDAServer extends AbstractRemoteServerRunner {
	public static String SERVER_ID = "org.eclipse.ptp.rm.lml.da.server"; //$NON-NLS-1$;

	public LMLDAServer() {
		super(Messages.LMLDAServer_Name);
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
		// Nothing to do
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
		return true;
	}

}