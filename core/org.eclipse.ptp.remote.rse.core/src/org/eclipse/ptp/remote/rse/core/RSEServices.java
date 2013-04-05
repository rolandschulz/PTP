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
package org.eclipse.ptp.remote.rse.core;

import java.io.IOException;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.ptp.remote.core.AbstractRemoteServices;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteFileManager;
import org.eclipse.ptp.remote.core.IRemoteProcess;
import org.eclipse.ptp.remote.core.IRemoteProcessBuilder;
import org.eclipse.ptp.remote.core.IRemoteServicesDescriptor;
import org.eclipse.ptp.remote.rse.core.messages.Messages;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;

public class RSEServices extends AbstractRemoteServices {
	private ISystemRegistry fRegistry = null;
	private IRemoteConnectionManager fConnMgr = null;
	private boolean fInitialized;

	public RSEServices(IRemoteServicesDescriptor descriptor) {
		super(descriptor);
	}

	private void checkInitialize() {
		fRegistry = RSECorePlugin.getTheSystemRegistry();
		if (fRegistry == null) {
			return;
		}

		// The old code that tried to wait for RSE to initialize
		// was wrong. If the init job hadn't run yet, it wouldn't block.
		// However, we can't block here anyway, because this can get called
		// from the main thread on startup, before RSE is initialized.
		// This would mean we deadlock ourselves.

		// So if RSE isn't initialized, report out initialization failed,
		// and the next time someone tries to use the service,
		// initialization
		// will be attempted again.

		if (!RSECorePlugin.isInitComplete(RSECorePlugin.INIT_ALL)) {
			return;
		}

		if (!RSECorePlugin.getThePersistenceManager().isRestoreComplete()) {
			return;
		}

		fConnMgr = new RSEConnectionManager(fRegistry, this);
		fInitialized = true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#getCommandShell(org.eclipse.ptp.remote.core.IRemoteConnection, int)
	 */
	public IRemoteProcess getCommandShell(IRemoteConnection conn, int flags) throws IOException {
		throw new IOException("Not currently implemented"); //$NON-NLS-1$
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.IRemoteServicesDelegate#getConnectionManager()
	 */
	public IRemoteConnectionManager getConnectionManager() {
		if (!fInitialized) {
			return null;
		}
		if (fConnMgr == null) {
			fConnMgr = new RSEConnectionManager(fRegistry, this);
		}
		return fConnMgr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.IRemoteServicesDelegate#getFileManager(org.eclipse
	 * .ptp.remote.IRemoteConnection)
	 */
	public IRemoteFileManager getFileManager(IRemoteConnection conn) {
		if (!fInitialized) {
			return null;
		}

		if (!(conn instanceof RSEConnection)) {
			return null;
		}
		return new RSEFileManager((RSEConnection) conn);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.IRemoteServicesDelegate#getProcessBuilder(org.
	 * eclipse.ptp.remote.IRemoteConnection, java.util.List)
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, List<String> command) {
		if (!fInitialized) {
			return null;
		}

		return new RSEProcessBuilder(conn, getFileManager(conn), command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.IRemoteServicesDelegate#getProcessBuilder(org.
	 * eclipse.ptp.remote.IRemoteConnection, java.lang.String[])
	 */
	public IRemoteProcessBuilder getProcessBuilder(IRemoteConnection conn, String... command) {
		if (!fInitialized) {
			return null;
		}

		return new RSEProcessBuilder(conn, getFileManager(conn), command);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#initialize()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.core.IRemoteServices#initialize(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean initialize(IProgressMonitor monitor) {
		if (!fInitialized) {
			SubMonitor progress = SubMonitor.convert(monitor, 10);
			progress.setTaskName(Messages.RSEServices_Initializing_RSE_services);
			while (!fInitialized && !progress.isCanceled()) {
				progress.setWorkRemaining(9);
				checkInitialize();
				if (!fInitialized) {
					try {
						synchronized (this) {
							wait(500);
						}
					} catch (InterruptedException e) {
						// Ignore
					}
				}
				progress.worked(1);
			}
		}
		return fInitialized;
	}
}
