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
package org.eclipse.ptp.internal.remote.remotetools.ui;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsServices;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteConnectionManager;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.AbstractRemoteUIConnectionManager;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.environment.generichost.core.ConfigFactory;
import org.eclipse.ptp.remotetools.environment.wizard.EnvironmentWizard;
import org.eclipse.swt.widgets.Shell;

public class RemoteToolsUIConnectionManager extends AbstractRemoteUIConnectionManager {
	private final TargetTypeElement remoteHost;
	private final IRemoteConnectionManager connMgr;

	public RemoteToolsUIConnectionManager(IRemoteServices services) {
		connMgr = services.getConnectionManager();
		remoteHost = RemoteToolsServices.getTargetTypeElement();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.core.IRemoteUIConnectionManager#newConnection()
	 */
	public IRemoteConnection newConnection(Shell shell) {
		return newConnection(shell, null, null);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager#newConnection(org
	 * .eclipse.swt.widgets.Shell,
	 * org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager
	 * .IRemoteConnectionAttributeHint[], java.lang.String[])
	 */
	public IRemoteConnection newConnection(Shell shell, String[] attrHints, String[] attrHintValues) {
		if (remoteHost != null) {
			IRemoteConnection[] oldConns = connMgr.getConnections();
			EnvironmentWizard wizard;

			if (attrHints != null) {
				Map<String, String> attrs = new HashMap<String, String>();
				for (int i = 0; i < attrHints.length; i++) {
					if (attrHints[i].equals(CONNECTION_ADDRESS_HINT)) {
						attrs.put(ConfigFactory.ATTR_CONNECTION_ADDRESS, attrHintValues[i]);
					} else if (attrHints[i].equals(CONNECTION_PORT_HINT)) {
						attrs.put(ConfigFactory.ATTR_CONNECTION_PORT, attrHintValues[i]);
					} else if (attrHints[i].equals(CONNECTION_TIMEOUT_HINT)) {
						attrs.put(ConfigFactory.ATTR_CONNECTION_TIMEOUT, attrHintValues[i]);
					} else if (attrHints[i].equals(LOGIN_USERNAME_HINT)) {
						attrs.put(ConfigFactory.ATTR_LOGIN_USERNAME, attrHintValues[i]);
					}
				}
				TargetElement target = new TargetElement(remoteHost, remoteHost.getName(), attrs, remoteHost.getName());
				remoteHost.addElement(target);
				wizard = new EnvironmentWizard(target);
			} else {
				wizard = new EnvironmentWizard(remoteHost);
			}

			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.create();
			dialog.setBlockOnOpen(true);

			if (dialog.open() == WizardDialog.OK) {
				/*
				 * Locate the new connection and return it. Assumes connections
				 * can only be created by the wizard, NOT removed.
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
				return newConns[newConns.length - 1];
			}
		}
		return null;
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
		// Not implemented yet
	}
}
