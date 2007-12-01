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

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteConnectionManager;
import org.eclipse.ptp.remote.remotetools.ui.ConfigurationDialog;
import org.eclipse.ptp.remotetools.core.AuthToken;
import org.eclipse.ptp.remotetools.core.PasswdAuthToken;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;


public class RemoteToolsConnectionManager implements IRemoteConnectionManager {
	private class AuthDialog extends Dialog {
		private Text hostnameText;
		private Text usernameText;
		private Text passwordText;
		
		public AuthDialog(Shell parent) {
			super(parent);
		}
		
		protected Control createDialogArea(Composite parent) {
			Composite comp = (Composite) super.createDialogArea(parent);
			
			GridLayout layout = (GridLayout) comp.getLayout();
			layout.numColumns = 2;
			
			Label label = new Label(comp, SWT.RIGHT);
			label.setText("Hostname: ");
			hostnameText = new Text(comp, SWT.SINGLE);
			GridData data = new GridData(GridData.FILL_HORIZONTAL);
			hostnameText.setLayoutData(data);
			
			label = new Label(comp, SWT.RIGHT);
			label.setText("Username: ");
			usernameText = new Text(comp, SWT.SINGLE);
			data = new GridData(GridData.FILL_HORIZONTAL);
			usernameText.setLayoutData(data);
			
			label = new Label(comp, SWT.RIGHT);
			label.setText("Password: ");
			passwordText = new Text(comp, SWT.SINGLE | SWT.PASSWORD);
			data = new GridData(GridData.FILL_HORIZONTAL);
			passwordText.setLayoutData(data);
			
			return comp;
		}
		  
		public AuthToken getAuth() {
			return new PasswdAuthToken(usernameText.getText(), passwordText.getText());
		}
		
		public String getHost() {
			return hostnameText.getText();
		}
	}
	
	private Map<String, IRemoteConnection> connections = new HashMap<String, IRemoteConnection>();
	
	public RemoteToolsConnectionManager() {
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
		ConfigurationDialog dialog = new ConfigurationDialog(shell);
		dialog.open();
		
		//org.eclipse.ptp.remotetools.core.IRemoteConnection conn = RemotetoolsPlugin.createSSHConnection(dialog.getAuth(), dialog.getHost());
		
		//connections.put(dialog.getHost(), new RemoteToolsConnection(conn, dialog.getHost(), dialog.getAuth().getUsername()));
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteConnectionManager#supportsNewConnections()
	 */
	public boolean supportsNewConnections() {
		return true;
	}
}
