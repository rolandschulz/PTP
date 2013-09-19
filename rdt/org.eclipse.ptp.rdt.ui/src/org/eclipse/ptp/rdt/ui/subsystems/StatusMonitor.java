/*******************************************************************************
 * Copyright (c) 2002, 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Initial Contributors:
 * The following IBM employees contributed to the Remote System Explorer
 * component that contains this file: David McKnight, Kushal Munir, 
 * Michael Berger, David Dykstal, Phil Coulthard, Don Yantzi, Eric Simpson, 
 * Emily Bruner, Mazen Faraj, Adrian Storisteanu, Li Ding, and Kent Hawley.
 * 
 * Contributors:
 * Martin Oberhuber (Wind River) - [168870] refactor org.eclipse.rse.core package of the UI plugin
 * David McKnight   (IBM)        - [225902] [dstore] use C_NOTIFICATION command to wake up the server
 * David McKnight   (IBM)        - [229947] [dstore] interruption to Thread.sleep()  should not stop waitForUpdate()
 * David McKnight   (IBM)        - [231126] [dstore] status monitor needs to reset WaitThreshold on nudge
 * David McKnight   (IBM)        - [278341] [dstore] Disconnect on idle causes the client hang
 *******************************************************************************/

package org.eclipse.ptp.rdt.ui.subsystems;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.dstore.core.model.DE;
import org.eclipse.dstore.core.model.DataElement;
import org.eclipse.dstore.core.model.DataStore;
import org.eclipse.dstore.core.model.DataStoreSchema;
import org.eclipse.dstore.extra.DomainEvent;
import org.eclipse.dstore.extra.IDomainListener;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteConnectionChangeEvent;
import org.eclipse.remote.core.IRemoteConnectionChangeListener;
import org.eclipse.swt.widgets.Display;

/*
 * This utility class can be used to monitor the status of one more more status DataElements.
 * Only one instanceof of this class is required per DataStore for use in monitoring statuses.
 * This is intended to be used in place of StatusChangeListeners
 * 
 *  * <p>
 * The following is one example of the use of the StatusMonitor. The code:
 * <blockquote><pre>
 *    		DataElement status = dataStore.command(dsCmd, args, deObj);
 *
 *			StatusMonitor smon = StatusMonitorFactory.getInstance().getStatusMonitorFor(getSystem(), ds);
 *			smon.waitForUpdate(status, monitor);
 * </pre></blockquote>
 */
/**
 * @since 6.0
 */
public class StatusMonitor implements IDomainListener, IRemoteConnectionChangeListener {

	private static Map<IRemoteConnection, StatusMonitor> fMonitors = new HashMap<IRemoteConnection, StatusMonitor>();
	
	/**
	 * @since 6.0
	 */
	public static StatusMonitor getStatusMonitorFor(IRemoteConnection connection, DataStore store) {
		StatusMonitor monitor = fMonitors.get(connection);
		if (monitor == null) {
			monitor = new StatusMonitor(connection, store);
			fMonitors.put(connection, monitor);
		}
		return monitor;
	}
	
	protected IRemoteConnection fRemoteConnection;

	protected boolean fNetworkDown = false;

	protected List<DataElement> fWorkingStatuses = new ArrayList<DataElement>();
	protected List<DataElement> fCancelledStatuses = new ArrayList<DataElement>();
	protected List<DataElement> fDoneStatuses = new ArrayList<DataElement>();

	protected DataStore fDataStore;

	/**
	 * Construct a StatusChangeListener
	 * 
	 * @param system
	 *            the system associated with this monitor
	 * @param dataStore
	 *            the dataStore associated with this monitor
	 * @since 6.0
	 */
	public StatusMonitor(IRemoteConnection connection, DataStore dataStore) {
		fRemoteConnection = connection;
		fDataStore = dataStore;
		reInit();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.remote.core.IRemoteConnectionChangeListener#connectionChanged(org.eclipse.remote.core.IRemoteConnectionChangeEvent)
	 */
	/**
	 * @since 6.0
	 */
	public void connectionChanged(IRemoteConnectionChangeEvent e) {
		if (e.getType() == IRemoteConnectionChangeEvent.CONNECTION_CLOSED ||
				e.getType() == IRemoteConnectionChangeEvent.CONNECTION_ABORTED) {
			fNetworkDown = true;
		}
	}

	public void dispose() {
		fRemoteConnection.removeConnectionChangeListener(this);
		fWorkingStatuses.clear();
		fDoneStatuses.clear();
		fCancelledStatuses.clear();
		fDataStore.getDomainNotifier().removeDomainListener(this);
	}

	/**
	 * @see IDomainListener#domainChanged(DomainEvent)
	 */
	public void domainChanged(DomainEvent event) {
		if (fWorkingStatuses.size() == 0) {
			return;
		}

		DataElement parent = (DataElement) event.getParent();
		if (fWorkingStatuses.contains(parent)) {
			boolean isStatusDone = determineStatusDone(parent);
			if (isStatusDone) {
				setDone(parent);
			}
		}
	}

	public DataStore getDataStore() {
		return fDataStore;
	}

	/**
	 * Test if the StatusChangeListener returned because the network connection
	 * to the remote system was broken.
	 */
	public boolean isNetworkDown() {
		return fNetworkDown;
	}

	/**
	 * @see IDomainListener#listeningTo(DomainEvent)
	 */
	public boolean listeningTo(DomainEvent event) {
		if (fWorkingStatuses.size() == 0) {
			return true;
		}

		DataElement parent = (DataElement) event.getParent();
		if (fWorkingStatuses.contains(parent)) {
			return determineStatusDone(parent);
		}

		return false;
	}

	public void reInit() {
		fNetworkDown = false;
		fRemoteConnection.addConnectionChangeListener(this);
		fWorkingStatuses.clear();
		fDoneStatuses.clear();
		fCancelledStatuses.clear();
		fDataStore.getDomainNotifier().addDomainListener(this);
	}

	public synchronized void setCancelled(DataElement status) {
		fWorkingStatuses.remove(status);
		fCancelledStatuses.add(status);
	}

	/**
	 * setDone(boolean)
	 */
	public synchronized void setDone(DataElement status) {
		fWorkingStatuses.remove(status);
		fDoneStatuses.add(status);
	}

	public synchronized void setWorking(DataElement status) {
		fWorkingStatuses.add(status);
	}

	public DataElement waitForUpdate(DataElement status) throws InterruptedException {
		return waitForUpdate(status, null, 0);
	}

	public DataElement waitForUpdate(DataElement status, int wait) throws InterruptedException {
		return waitForUpdate(status, null, wait);
	}

	public DataElement waitForUpdate(DataElement status, IProgressMonitor monitor) throws InterruptedException {
		return waitForUpdate(status, monitor, 0);
	}

	public synchronized DataElement waitForUpdate(DataElement status, IProgressMonitor monitor, int wait)
			throws InterruptedException {
		if (fNetworkDown && status.getDataStore().isConnected()) {
			reInit();
		}
		if (determineStatusDone(status)) {
			setDone(status);
			return status;
		}

		setWorking(status);

		Display display = Display.getCurrent();

		// Prevent infinite looping by introducing a threshold for wait

		int WaitThreshold = 50;
		if (wait > 0)
			WaitThreshold = wait * 10; // 1 second means 10 sleep(100ms)
		else if (wait == -1) // force a diagnostic
			WaitThreshold = -1;

		int initialWaitThreshold = WaitThreshold;
		int nudges = 0; // nudges used for waking up server with slow
						// connections
		// nudge up to 12 times before giving up

		if (display != null) {
			// Current thread is UI thread
			while (fWorkingStatuses.contains(status)) {
				// while (display.readAndDispatch()) {
				// Process everything on event queue
				// }

				if ((monitor != null) && (monitor.isCanceled())) {
					setCancelled(status);
					throw new InterruptedException();
				}

				boolean statusDone = determineStatusDone(status);

				if (statusDone) {
					setDone(status);
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// Continue waiting in case of spurious interrupt.
						// We check the progress monitor to listen for Eclipse
						// Shutdown.
						continue;
					}
					if (WaitThreshold > 0) // update timer count if
						// threshold not reached
						--WaitThreshold; // decrement the timer count

					if (WaitThreshold == 0) {
						wakeupServer(status);

						// no diagnostic factory but there is a timeout
						if (nudges >= 12)
							return status; // returning the undone status object

						nudges++;
						WaitThreshold = initialWaitThreshold;
					} else if (fNetworkDown || !fDataStore.isConnected()) {
						dispose();
						throw new InterruptedException();
					}
				}
			}

		} else {
			// Current thread is not UI thread
			while (fWorkingStatuses.contains(status)) {

				if ((monitor != null) && (monitor.isCanceled())) {
					setCancelled(status);
					throw new InterruptedException();
				}

				boolean statusDone = determineStatusDone(status);

				if (statusDone) {
					setDone(status);
				} else {
					try {
						Thread.sleep(100);
					} catch (InterruptedException e) {
						// Continue waiting in case of spurious interrupt.
						// We check the progress monitor to listen for Eclipse
						// Shutdown.
						continue;
					}

					if (WaitThreshold > 0) // update timer count if
						// threshold not reached
						--WaitThreshold; // decrement the timer count

					if (WaitThreshold == 0) {
						wakeupServer(status);

						// no diagnostic factory but there is a timeout
						if (nudges >= 12)
							return status; // returning the undone status object

						nudges++;
						WaitThreshold = initialWaitThreshold;
					} else if (fNetworkDown) {
						dispose();
						throw new InterruptedException();
					}
				}
			}
		}

		return status;
	}

	public boolean wasCancelled(DataElement status) {
		if (fCancelledStatuses.contains(status)) {
			return true;
		}
		return false;
	}

	private void wakeupServer(DataElement status) {
		if (status != null) {
			// token command to wake up update handler
			DataElement cmdDescriptor = fDataStore.findCommandDescriptor(DataStoreSchema.C_NOTIFICATION);
			DataElement subject = status.getParent().get(0);
			if (cmdDescriptor != null) {
				fDataStore.command(cmdDescriptor, subject);
			}
		}
	}

	/**
	 * Determines whether the status is done.
	 * 
	 * @return <code>true</code> if status done, <code>false</code> otherwise.
	 */
	protected boolean determineStatusDone(DataElement status) {
		return status.getAttribute(DE.A_VALUE).equals("done") || status.getAttribute(DE.A_NAME).equals("done"); //$NON-NLS-1$ //$NON-NLS-2$
	}
}
