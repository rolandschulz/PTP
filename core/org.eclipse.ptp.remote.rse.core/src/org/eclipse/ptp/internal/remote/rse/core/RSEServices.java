/*******************************************************************************
 * Copyright (c) 2007, 2014 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *    Markus Schorn - Fix for Bug 449362
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.rse.core;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.SubMonitor;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.internal.remote.rse.core.messages.Messages;
import org.eclipse.remote.core.AbstractRemoteServices;
import org.eclipse.remote.core.IRemoteConnectionManager;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IRemoteServicesDescriptor;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.core.model.ISystemRegistry;

public class RSEServices extends AbstractRemoteServices {
	private ISystemRegistry fRegistry = null;
	private IRemoteConnectionManager fConnMgr = null;
	private boolean fInitialized;
	private boolean fTriggeredInitJob;

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
			// The call to 'isInitComplete(...)' does not trigger the 
			// initialization of RSE. Let's call 'waitForInitCompletion', which
			// will do that.
			if (!fTriggeredInitJob) {
				fTriggeredInitJob = true;
				new Job(Messages.RSEServices_Initializing_RSE_services) {
					protected IStatus run(IProgressMonitor monitor) {
						try {
							return RSECorePlugin.waitForInitCompletion();
						} catch (InterruptedException e) {
							return Status.CANCEL_STATUS;
						}
					}
				}.schedule();
			}
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
	 * @see org.eclipse.remote.core.IRemoteServices#getCapabilities()
	 */
	public int getCapabilities() {
		return IRemoteServices.CAPABILITY_ADD_CONNECTIONS | IRemoteServices.CAPABILITY_REMOVE_CONNECTIONS;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.remote.IRemoteServicesDelegate#getConnectionManager()
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
	 * @see org.eclipse.remote.core.IRemoteServices#initialize()
	 */
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServices#initialize(org.eclipse.core.runtime.IProgressMonitor)
	 */
	public boolean initialize(IProgressMonitor monitor) {
		if (!fInitialized) {
			RSEAdapterCorePlugin.log(new Exception("RSE Initialized!")); //$NON-NLS-1$
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
