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
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remotetools.environment.EnvironmentPlugin;
import org.eclipse.ptp.remotetools.environment.control.ITargetControl;
import org.eclipse.ptp.remotetools.environment.core.ITargetElement;
import org.eclipse.ptp.remotetools.environment.core.TargetEnvironmentManager;
import org.eclipse.ptp.remotetools.environment.core.TargetTypeElement;
import org.eclipse.ptp.remotetools.environment.wizard.EnvironmentWizard;
import org.eclipse.swt.widgets.Shell;


public class RemoteToolsConnectionManager implements IRemoteConnectionManager {
	private TargetTypeElement genericHost = null;
	private Map<String, IRemoteConnection> connections = new HashMap<String, IRemoteConnection>();
	
	public RemoteToolsConnectionManager() {
		TargetEnvironmentManager targetMgr = EnvironmentPlugin.getDefault().getTargetsManager();
		for (Object obj : targetMgr.getTypeElements()) {
			TargetTypeElement element = (TargetTypeElement)obj;
			if (element.getName().equals("Generic Host")) {
				genericHost = element;
				break;
			}
		}
		refreshConnections();
	}
	
	private void refreshConnections() {
		for (Object obj : genericHost.getElements()) {
			ITargetElement element = (ITargetElement)obj;
			IRemoteConnection conn = connections.get(element.getName());
			if (conn == null) {
				ITargetControl control;
				try {
					control = element.getControl();
					connections.put(element.getName(), new RemoteToolsConnection(element.getName(), "", "", control));
				} catch (CoreException e) {
				}
			}
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnection(java.lang.String)
	 */
	public IRemoteConnection getConnection(String name) {
		return connections.get(name);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#getConnections()
	 */
	public IRemoteConnection[] getConnections() {
		return connections.values().toArray(new IRemoteConnection[connections.size()]);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#newConnection()
	 */
	public void newConnection(Shell shell) {
		if (genericHost != null) {
			EnvironmentWizard wizard = new EnvironmentWizard(genericHost);
			WizardDialog dialog = new WizardDialog(shell, wizard);
			dialog.create();
			dialog.setBlockOnOpen(true);
			if (dialog.open() == WizardDialog.OK) {
				refreshConnections();
			}
		}
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#supportsNewConnections()
	 */
	public boolean supportsNewConnections() {
		return genericHost != null;
	}
}
