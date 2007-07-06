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
package org.eclipse.ptp.internal.remote;

import java.io.File;

import org.eclipse.ptp.remote.IRemoteConnection;
import org.eclipse.ptp.remote.IRemoteFileManager;
import org.eclipse.ptp.remote.PTPRemotePlugin;
import org.eclipse.swt.widgets.FileDialog;


public class LocalFileManager implements IRemoteFileManager {
	public String browseRemoteFile(IRemoteConnection conn, String message, String correctPath) {
		FileDialog dialog = new FileDialog(PTPRemotePlugin.getShell());
		dialog.setText(message);
		if (correctPath != null) {
			File path = new File(correctPath);
			if (path.exists())
				dialog.setFilterPath(path.isFile() ? correctPath : path.getParent());
		}
	
		return dialog.open();
	}
}
