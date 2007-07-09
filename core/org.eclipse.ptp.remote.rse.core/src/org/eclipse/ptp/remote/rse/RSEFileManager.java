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
package org.eclipse.ptp.remote.rse;

import org.eclipse.jface.window.Window;
import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFileDialog;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.swt.widgets.Shell;

public class RSEFileManager implements IRemoteFileManager {
	public String browseRemoteFile(Shell shell, IRemoteConnection conn, String message, String correctPath) {
		IHost host = ((RSEConnection)conn).getHost();
		SystemRemoteFileDialog dlg = new SystemRemoteFileDialog(shell, message, host);
		dlg.setBlockOnOpen(true);
		if(dlg.open() == Window.OK) {
			Object retObj = dlg.getSelectedObject();
			if(retObj instanceof IRemoteFile) {
				IRemoteFile selectedFile = (IRemoteFile) retObj;
				return selectedFile.getAbsolutePath();
			}
		}
		return null;
	}
}
