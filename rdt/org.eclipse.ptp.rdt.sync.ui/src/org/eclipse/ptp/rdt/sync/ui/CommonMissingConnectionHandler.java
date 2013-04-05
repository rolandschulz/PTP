/*******************************************************************************
 * Copyright (c) 2012 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ptp.rdt.sync.core.IMissingConnectionHandler;
import org.eclipse.ptp.rdt.sync.ui.messages.Messages;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.RemoteUIServices;

public class CommonMissingConnectionHandler implements IMissingConnectionHandler {
	private static long lastMissingConnectiontDialogTimeStamp = 0;
	private static final long timeBetweenDialogs = 5000; // 5 seconds

	@Override
	public void handle(final IRemoteServices remoteServices, final String connectionName) {
		RDTSyncUIPlugin.getStandardDisplay().syncExec(new Runnable() {
			@Override
			public void run() {
				// Avoid flooding the display with missing connection dialogs
				if (System.currentTimeMillis() - lastMissingConnectiontDialogTimeStamp <= timeBetweenDialogs) {
					return;
				}
				lastMissingConnectiontDialogTimeStamp = System.currentTimeMillis();
				String[] buttonLabels = new String[2];
				buttonLabels[0] = IDialogConstants.OK_LABEL;
				buttonLabels[1] = Messages.CommonMissingConnectionHandler_6;
				String newline = System.getProperty("line.separator"); //$NON-NLS-1$
				MessageDialog dialog = new MessageDialog(null, Messages.CommonMissingConnectionHandler_0, null,
						Messages.CommonMissingConnectionHandler_1 + connectionName + Messages.CommonMissingConnectionHandler_2
								+ newline + newline + Messages.CommonMissingConnectionHandler_3 + newline
								+ Messages.CommonMissingConnectionHandler_4 + newline + Messages.CommonMissingConnectionHandler_5,
						MessageDialog.ERROR, buttonLabels, 0);
				int buttonPressed = dialog.open();
				if (buttonPressed == 1) {
					IRemoteUIConnectionManager connectionManager = RemoteUIServices.getRemoteUIServices(remoteServices)
							.getUIConnectionManager();
					if (connectionManager != null) {
						connectionManager.newConnection(dialog.getShell());
					}
				}
			}
		});
	}
}