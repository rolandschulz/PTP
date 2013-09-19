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

import java.util.Set;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsConnectionManager;
import org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsConnectionWorkingCopy;
import org.eclipse.ptp.internal.remote.remotetools.core.RemoteToolsServices;
import org.eclipse.ptp.remotetools.environment.control.ITargetConfig;
import org.eclipse.ptp.remotetools.environment.core.TargetElement;
import org.eclipse.ptp.remotetools.environment.wizard.EnvironmentWizard;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.exception.RemoteConnectionException;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.swt.widgets.Shell;

public class RemoteToolsUIConnectionManager extends AbstractRemoteUIConnectionManager {
	private final RemoteToolsConnectionManager connMgr;

	public RemoteToolsUIConnectionManager(RemoteToolsServices services) {
		connMgr = (RemoteToolsConnectionManager) services.getConnectionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionManager#getConnectionWizard(org.eclipse.swt.widgets.Shell)
	 */
	public IRemoteUIConnectionWizard getConnectionWizard(final Shell shell) {
		final TargetElement element = connMgr.newTargetElement("new");
		final ITargetConfig config;
		try {
			config = element.getControl().getConfig();
		} catch (CoreException e) {
			return null;
		}

		return new IRemoteUIConnectionWizard() {
			private String fName;
			private RemoteToolsConnectionWorkingCopy fConnection;

			public IRemoteConnectionWorkingCopy open() {
				final RemoteToolsConnectionWorkingCopy conn;
				if (fConnection == null) {
					try {
						conn = (RemoteToolsConnectionWorkingCopy) connMgr.newConnection(fName);
						for (String key : config.getAttributes().getAttributesAsMap().keySet()) {
							conn.setAttribute(key, config.getAttributes().getString(key));
						}
					} catch (RemoteConnectionException e) {
						RemoteToolsAdapterUIPlugin.log(e);
						return null;
					}
				} else {
					conn = fConnection;
				}

				EnvironmentWizard wizard = new EnvironmentWizard(conn.getTargetElement());

				WizardDialog dialog = new WizardDialog(shell, wizard);
				dialog.create();
				dialog.setBlockOnOpen(true);

				if (dialog.open() == WizardDialog.OK) {
					return conn;
				}
				return null;
			}

			public void setInvalidConnectionNames(Set<String> names) {
				// TODO Auto-generated method stub

			}

			public void setConnectionName(String name) {
				fName = name;
			}

			public void setConnection(IRemoteConnectionWorkingCopy connection) {
				if (connection instanceof RemoteToolsConnectionWorkingCopy) {
					fConnection = (RemoteToolsConnectionWorkingCopy) connection;
				}
			}
		};
	}
}
