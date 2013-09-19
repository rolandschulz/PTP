/*******************************************************************************
 * Copyright (c) 2010, 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.managedbuilder.xlc.ui.properties;

import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.ptp.rdt.managedbuilder.xlc.ui.messages.Messages;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.ui.IRemoteUIConstants;
import org.eclipse.remote.ui.IRemoteUIFileManager;
import org.eclipse.remote.ui.IRemoteUIServices;
import org.eclipse.remote.ui.RemoteUIServices;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A field editor for a remote directory path type preference.
 * 
 * @since 3.0
 */
public class RemoteDirectoryFieldEditor extends DirectoryFieldEditor {

	/**
	 * @since 3.0
	 */
	protected IRemoteConnection fRemoteConnection;
	private IFileStore fCurrentRemoteFileStore;
	private String fCurrentRemoteFilename;

	/**
	 * Creates a directory field editor.
	 * 
	 * @param name
	 *            the name of the preference this field editor works on
	 * @param labelText
	 *            the label text of the field editor
	 * @param parent
	 *            the parent of the field editor's control
	 * @param IHost
	 *            the remote file system connection host
	 * @since 4.0
	 */
	public RemoteDirectoryFieldEditor(String name, String labelText, Composite parent, IRemoteConnection connection) {
		super(name, labelText, parent);
		fRemoteConnection = connection;
	}

	/*
	 * (non-Javadoc) Method declared on StringButtonFieldEditor. Browse the
	 * remote directories and returns the selected directory.
	 */
	@Override
	protected String changePressed() {

		String remotePath = browseRemoteLocation(getShell(), getTextControl().getText());
		if (remotePath != null) {
			getTextControl().setText(remotePath);
		}
		doCheckState();
		return remotePath;

	}

	/**
	 * open a dialog to browse remote directories
	 * 
	 * @param shell
	 * @param path
	 * @return
	 */
	public String browseRemoteLocation(Shell shell, String path) {
		IRemoteUIServices remoteUIServices = RemoteUIServices.getRemoteUIServices(fRemoteConnection.getRemoteServices());
		if (remoteUIServices != null) {
			IRemoteUIFileManager fileMgr = remoteUIServices.getUIFileManager();
			if (fileMgr != null) {
				fileMgr.setConnection(fRemoteConnection);
				String correctPath = path;
				String selectedPath = fileMgr
						.browseDirectory(
								shell,
								Messages.getString("RemoteDirectoryFieldEditor_0", fRemoteConnection.getName()), correctPath, IRemoteUIConstants.NONE); //$NON-NLS-1$
				return selectedPath;
			}
		}
		return null;
	}

	// synchronize the remote file store with remote file name
	private void updateCurrentRemoteFile() {
		String dirName = getTextControl().getText();
		// only get remoteFile object when we get a new dir name
		if (fCurrentRemoteFilename == null || !fCurrentRemoteFilename.equals(dirName)) {
			fCurrentRemoteFilename = dirName;
			// reset currentRemoteFileStore first
			fCurrentRemoteFileStore = null;
			if (fRemoteConnection != null) {
				fCurrentRemoteFileStore = fRemoteConnection.getFileManager().getResource(dirName);
			}
		}

	}

	/*
	 * Method declared on StringFieldEditor. Checks whether the remote dir is
	 * validated
	 */
	@Override
	protected boolean doCheckState() {
		updateCurrentRemoteFile();
		if (fCurrentRemoteFileStore != null) {
			if (fCurrentRemoteFileStore.fetchInfo().exists()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}

}
