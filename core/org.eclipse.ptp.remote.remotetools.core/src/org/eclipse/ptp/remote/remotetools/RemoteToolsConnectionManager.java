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
package org.eclipse.ptp.remote.remotetools;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.remotetools.environment.core.PTPTargetControl;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.environment.wizard.EnvironmentWizard;
import org.eclipse.swt.widgets.Shell;


public class RemoteToolsConnectionManager implements IRemoteConnectionManager {
	private TargetTypeElement remoteHost = null;
	private Map<String, IRemoteConnection> connections = new HashMap<String, IRemoteConnection>();
	
	public RemoteToolsConnectionManager() {
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		for (Object obj : targetMgr.getTypeElements()) {
			TargetTypeElement element = (TargetTypeElement)obj;
			if (element.getName().equals("Remote Host")) {
				remoteHost = element;
				break;
			}
		}
		refreshConnections();
	}
	
	/**
	 * Refresh the list of connections that we know about. Deals with connection that are added or deleted
	 * by another entity.
	 */
	private void refreshConnections() {
		Map<String, IRemoteConnection> newConns = new HashMap<String, IRemoteConnection>();
		for (Object obj : remoteHost.getElements()) {
			ITargetElement element = (ITargetElement)obj;
			IRemoteConnection conn = connections.get(element.getName());
			if (conn == null) {
				ITargetControl control;
				try {
					control = element.getControl();
					/*
					 * FIXME: need to work out how to get this information correctly!!!!
					 */
					String address = (String) element.getAttributes().get("ptp.connection-address");
					String user = (String) element.getAttributes().get("ptp.login-username");
					newConns.put(element.getName(), new RemoteToolsConnection(element.getName(), address, user, (PTPTargetControl)control));
				} catch (CoreException e) {
				}
			} else {
				newConns.put(element.getName(), conn);
			}
		}
		connections = newConns;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnection(java.lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		refreshConnections();
		return connections.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		refreshConnections();
		return connections.values().toArray(new IRemoteConnection[connections.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#newConnection()
	 */
	public void newConnection(Shell shell) {
		if (remoteHost != null) {
			EnvironmentWizard wizard = new EnvironmentWizard(remoteHost);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.create();
			dialog.setBlockOnOpen(true);
			if (dialog.open() == WizardDialog.OK) {
				refreshConnections();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteConnectionManager#supportsNewConnections()
	 */
	public boolean supportsNewConnections() {
		return remoteHost != null;
	}
}
