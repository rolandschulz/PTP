/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.managedbuilder.xlc.ui.properties;

import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.jface.preference.DirectoryFieldEditor;
import org.eclipse.rse.core.filters.ISystemFilterReference;
import org.eclipse.rse.core.model.IHost;
import org.eclipse.rse.core.subsystems.ISubSystem;
import org.eclipse.rse.core.subsystems.ISubSystemConfiguration;
import org.eclipse.rse.files.ui.dialogs.SystemRemoteFolderDialog;
import org.eclipse.rse.services.clientserver.messages.SystemMessageException;
import org.eclipse.rse.subsystems.files.core.model.RemoteFileUtility;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFile;
import org.eclipse.rse.subsystems.files.core.subsystems.IRemoteFileSubSystem;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;

/**
 * A field editor for a remote directory path type preference.
 */
public class RemoteDirectoryFieldEditor extends DirectoryFieldEditor {

	protected IHost connectionHost;
	private IRemoteFile currentRemoteFile;
	private String currentRemoteFilename;

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
	 */
	public RemoteDirectoryFieldEditor(String name, String labelText, Composite parent, IHost connectionHost) {
		super(name, labelText, parent);
		this.connectionHost = connectionHost;
	}

	/*
	 * (non-Javadoc) Method declared on StringButtonFieldEditor. Browse the
	 * remote directories and returns the selected directory.
	 */
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
		SystemRemoteFolderDialog dlg = new SystemRemoteFolderDialog(shell);
		// disable creating new connection button
		dlg.setShowNewConnectionPrompt(false);

		if (connectionHost != null) {
			// set the host as the only host(disable connection switching)
			dlg.setDefaultSystemConnection(connectionHost, true);
			updateCurrentRemoteFile();
			if (currentRemoteFile != null) {
				dlg.setPreSelection(currentRemoteFile);
			}

		}

		dlg.open();

		Object output = dlg.getOutputObject();

		if (output instanceof ISystemFilterReference) {
			ISystemFilterReference ref = (ISystemFilterReference) output;
			ISubSystem targetSubSystem = ref.getSubSystem();
			ISubSystemConfiguration factory = targetSubSystem.getSubSystemConfiguration();
			if (factory.supportsDropInFilters()) {
				output = targetSubSystem.getTargetForFilter(ref);
			}
		}
		String outputLocation = null;
		if (output instanceof IRemoteFile) {
			IRemoteFile rmtFile = (IRemoteFile) output;
			outputLocation = rmtFile.getAbsolutePath();
		}
		return outputLocation;
	}

	// synchronize the remote file object with remote file name
	private void updateCurrentRemoteFile() {
		String dirName = getTextControl().getText();
		// only get remoteFile object when we get a new dir name
		if (currentRemoteFilename == null || !currentRemoteFilename.equals(dirName)) {
			currentRemoteFilename = dirName;
			// reset currentRemoteFile first
			currentRemoteFile = null;
			if (connectionHost != null) {
				IRemoteFileSubSystem remoteFileSubSystem = RemoteFileUtility.getFileSubSystem(connectionHost);
				if (remoteFileSubSystem != null) {
					try {

						currentRemoteFile = remoteFileSubSystem.getRemoteFileObject(currentRemoteFilename,
								new NullProgressMonitor());
					} catch (SystemMessageException e) {

					}
				}
			}
		}

	}

	/*
	 * Method declared on StringFieldEditor. Checks whether the remote dir is
	 * validated
	 */
	protected boolean doCheckState() {

		updateCurrentRemoteFile();
		if (currentRemoteFile != null) {
			if (currentRemoteFile.exists()) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}

	}

}
