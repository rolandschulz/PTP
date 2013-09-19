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
package org.eclipse.ptp.internal.remote.rse.ui;

import java.util.Set;

import org.eclipse.ptp.internal.remote.rse.core.RSEConnectionManager;
import org.eclipse.remote.core.IRemoteConnectionWorkingCopy;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.ui.AbstractRemoteUIConnectionManager;
import org.eclipse.remote.ui.IRemoteUIConnectionWizard;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.ui.actions.SystemNewConnectionAction;
import org.eclipse.swt.widgets.Shell;

public class RSEUIConnectionManager extends AbstractRemoteUIConnectionManager {
	private SystemNewConnectionAction fNewConnAction;
	private final RSEConnectionManager fConnManager;

	public RSEUIConnectionManager(IRemoteServices services) {
		this.fConnManager = (RSEConnectionManager) services.getConnectionManager();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.ui.IRemoteUIConnectionManager#getConnectionWizard(org.eclipse.swt.widgets.Shell)
	 */
	public IRemoteUIConnectionWizard getConnectionWizard(final Shell shell) {
		return new IRemoteUIConnectionWizard() {
			public IRemoteConnectionWorkingCopy open() {
				if (fNewConnAction == null) {
					fNewConnAction = new SystemNewConnectionAction(shell, false, false, null);
				}

				try {
					fNewConnAction.run();
				} catch (Exception e) {
					// Ignore
				}

				Object value = fNewConnAction.getValue();
				if (value != null && value instanceof IHost) {
					return fConnManager.createConnection((IHost) value).getWorkingCopy();
				}
				return null;
			}

			public void setInvalidConnectionNames(Set<String> names) {
				// Not supported
			}

			public void setConnectionName(String name) {
				// Not supported
			}

			public void setConnection(IRemoteConnectionWorkingCopy connection) {
				// Not supported
			}
		};
	}
}
