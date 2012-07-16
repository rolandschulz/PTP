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
import org.eclipse.ptp.remote.core.IRemoteServices;

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
				String[] buttonLabels = new String[1];
				buttonLabels[0] = IDialogConstants.OK_LABEL;
				MessageDialog dialog = new MessageDialog(null, "Missing Connection", null, "Connection does not exist: " + //$NON-NLS-1$ //$NON-NLS-2$
						connectionName, MessageDialog.ERROR, buttonLabels, 0);
				dialog.open();
			}
		});
	}
}
