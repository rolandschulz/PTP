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
package org.eclipse.ptp.remote.remotetools.ui;

import java.util.Arrays;
import java.util.Comparator;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.environment.wizard.EnvironmentWizard;
import org.eclipse.swt.widgets.Shell;


public class RemoteToolsUIConnectionManager implements IRemoteUIConnectionManager {
	private TargetTypeElement remoteHost = null;
	private IRemoteConnectionManager connMgr = null;
	
	public RemoteToolsUIConnectionManager(IRemoteServices services) {
		this.connMgr = services.getConnectionManager();
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		for (Object obj : targetMgr.getTypeElements()) {
			TargetTypeElement element = (TargetTypeElement)obj;
			if (element.getName().equals("Remote Host")) {
				remoteHost = element;
				break;
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.core.IRemoteUIConnectionManager#newConnection()
	 */
	public IRemoteConnection newConnection(Shell shell) {
		if (remoteHost != null) {
			IRemoteConnection[] oldConns = connMgr.getConnections();
			
			EnvironmentWizard wizard = new EnvironmentWizard(remoteHost);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.create();
			dialog.setBlockOnOpen(true);
			
			if (dialog.open() == WizardDialog.OK) {
				/*
				 * Locate the new connection and return it. Assumes connections can
				 * only be created by the wizard, NOT removed.
				 */
				IRemoteConnection[] newConns = connMgr.getConnections();
				
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
		return null;
	}
}
