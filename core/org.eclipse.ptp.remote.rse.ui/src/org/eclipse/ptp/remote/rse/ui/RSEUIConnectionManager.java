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

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.rse.core.RSEConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.rse.core.IRSESystemType;
import org.eclipse.rse.core.RSECorePlugin;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.swt.widgets.Shell;

public class RSEUIConnectionManager implements IRemoteUIConnectionManager {
	private SystemNewConnectionAction action;
	private RSEConnectionManager manager;

	public RSEUIConnectionManager(IRemoteServices services) {
		this.manager = (RSEConnectionManager)services.getConnectionManager();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#newConnection()
	 */
	public IRemoteConnection newConnection(Shell shell) {
		IRemoteConnection[] oldConns = manager.getConnections();

		if (action == null) {
 			action = new SystemNewConnectionAction(shell, false, false, null);
 	   		IRSESystemType systemType = RSECorePlugin.getTheCoreRegistry().getSystemTypeById(IRSESystemType.SYSTEMTYPE_SSH_ONLY_ID);
			if (systemType != null) {
 	   			action.restrictSystemTypes(new IRSESystemType[] { systemType });
			}
		}
    		
		try 
		{
			action.run();
		} catch (Exception e)
		{
			// Ignore
		}
		
		manager.refreshConnections();
		
		/*
		 * Try to work out which is the new connection. Assumes that connections
		 * can only be created, NOT removed.
		 */
		IRemoteConnection[] newConns = manager.getConnections();
		
		if (newConns.length <= oldConns.length) {
			return null;
		}

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
		for (int i = 0; i < oldConns.length; i++) {
			if (!oldConns[i].equals(newConns[i])) {
				return newConns[i];
			}
		}
		
		return newConns[newConns.length-1];
	}
}
