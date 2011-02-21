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
package org.eclipse.ptp.remote.rse.ui;

import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.operation.IRunnableContext;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.exception.RemoteConnectionException;
import org.eclipse.ptp.remote.rse.core.RSEConnectionManager;
import org.eclipse.ptp.remote.rse.ui.messages.Messages;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.swt.widgets.Shell;

public class RSEUIConnectionManager implements IRemoteUIConnectionManager {
	private SystemNewConnectionAction action;
	private final RSEConnectionManager manager;

	public RSEUIConnectionManager(IRemoteServices services) {
		this.manager = (RSEConnectionManager) services.getConnectionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#newConnection()
	 */
	public IRemoteConnection newConnection(Shell shell) {
		IRemoteConnection[] oldConns = manager.getConnections();

		if (action == null)
			action = new SystemNewConnectionAction(shell, false, false, null);

		try {
			action.run();
		} catch (Exception e) {
			// Ignore
		}

		manager.refreshConnections();

		/*
		 * Try to work out which is the new connection. Assumes that connections
		 * can only be created, NOT removed.
		 */
		IRemoteConnection[] newConns = manager.getConnections();

		if (newConns.length <= oldConns.length)
			return null;

		Arrays.sort(oldConns, new Comparator<IRemoteConnection>() {
			public int compare(IRemoteConnection c1, IRemoteConnection c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		Arrays.sort(newConns, new Comparator<IRemoteConnection>() {
			public int compare(IRemoteConnection c1, IRemoteConnection c2) {
				return c1.getName().compareToIgnoreCase(c2.getName());
			}
		});
		for (int i = 0; i < oldConns.length; i++)
			if (!oldConns[i].equals(newConns[i]))
				return newConns[i];

		return newConns[newConns.length - 1];
	}

	public IRemoteConnection newConnection(Shell shell, Map<String, String> defaultAttr) {
		return newConnection(shell);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager#
	 * openConnectionWithProgress(org.eclipse.swt.widgets.Shell,
	 * org.eclipse.jface.operation.IRunnableContext,
	 * org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void openConnectionWithProgress(final Shell shell, IRunnableContext context, final IRemoteConnection connection) {
		if (!connection.isOpen()) {
			IRunnableWithProgress op = new IRunnableWithProgress() {
				public void run(IProgressMonitor monitor) throws InvocationTargetException, InterruptedException {
					try {
						connection.open(monitor);
					} catch (RemoteConnectionException e) {
						ErrorDialog.openError(shell, Messages.RSEUIConnectionManager_0, Messages.RSEUIConnectionManager_1,
								new Status(IStatus.ERROR, RSEAdapterUIPlugin.PLUGIN_ID, e.getMessage()));
					}
				}
			};
			try {
				if (context != null)
					context.run(true, true, op);
				else
					new ProgressMonitorDialog(shell).run(true, true, op);
			} catch (InvocationTargetException e) {
				ErrorDialog.openError(shell, Messages.RSEUIConnectionManager_0, Messages.RSEUIConnectionManager_1, new Status(
						IStatus.ERROR, RSEAdapterUIPlugin.PLUGIN_ID, e.getMessage()));
			} catch (InterruptedException e) {
				ErrorDialog.openError(shell, Messages.RSEUIConnectionManager_0, Messages.RSEUIConnectionManager_1, new Status(
						IStatus.ERROR, RSEAdapterUIPlugin.PLUGIN_ID, e.getMessage()));
			}
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager#updateConnection
	 * (org.eclipse.swt.widgets.Shell,
	 * org.eclipse.ptp.remote.core.IRemoteConnection)
	 */
	public void updateConnection(Shell shell, IRemoteConnection connection) {
		// TODO Auto-generated method stub

	}
}
