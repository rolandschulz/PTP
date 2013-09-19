/*******************************************************************************
 * Copyright (c) 2011 Oak Ridge National Laboratory and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    John Eblen - initial implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.sync.ui.preferences;

import java.io.BufferedInputStream;
import java.io.InputStream;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileInfo;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ptp.internal.rdt.sync.core.RDTSyncCorePlugin;
import org.eclipse.ptp.internal.rdt.sync.core.messages.Messages;
import org.eclipse.ptp.rdt.sync.core.SyncConfig;
import org.eclipse.ptp.rdt.sync.core.SyncConfigManager;
import org.eclipse.ptp.rdt.sync.core.exceptions.MissingConnectionException;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteFileManager;

/**
 * Class for accessing the contents (files and directories) of a remote file system. The constructor takes a connection and a
 * root directory. From there, the user can ask for the contents of any subdirectory.
 * 
 * This is useful for GUIs that need to display the contents of a remote file system.
 * 
 * Class also provides a static method for getting remote file content for a given IFile. This is useful for quickly retrieving
 * file content when the file is not available locally. The project, connection, etc. do not have to be provided.
 */
public class RemoteContentProvider implements ITreeContentProvider {
	private final IRemoteConnection connection;
	private final IPath rootDir;
	private final IProject project;
	private final IRemoteFileManager fileManager;

	/**
	 * Create a new content provider at the given location (connection and directory) for the given project.
	 * 
	 * @param conn
	 * @param dir
	 * @param proj
	 */
	public RemoteContentProvider(IRemoteConnection conn, IPath dir, IProject proj) {
		if (conn == null || dir == null) {
			throw new IllegalArgumentException(Messages.RemoteContentProvider_0);
		}
		connection = conn;
		rootDir = dir;
		project = proj;
		fileManager = connection.getFileManager();
	}

	/**
	 * Dispose of content provider.
	 */
	@Override
	public void dispose() {
		// Nothing to do
	}

	/**
	 * Respond to changed input.
	 */
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
		// Nothing to do
	}

	/**
	 * Get the root elements of the remote directory. The root requires special handling, since there is no IFolder for root.
	 * 
	 * @param Ignored
	 *            - but should probably be an IProject
	 * @return the elements
	 */
	@Override
	public Object[] getElements(Object inputElement) {
		IFileStore fileStore = fileManager.getResource(rootDir.toString());
		IFileInfo[] childFiles;
		try {
			childFiles = fileStore.childInfos(EFS.NONE, null);
		} catch (CoreException e) {
			// This can happen if the remote directory does not exist.
			return new Object[0];
		}

		IResource[] childObjects = new IResource[childFiles.length];
		for (int i = 0; i < childFiles.length; i++) {
			if (childFiles[i].isDirectory()) {
				childObjects[i] = project.getFolder(childFiles[i].getName());
			} else {
				childObjects[i] = project.getFile(childFiles[i].getName());
			}
		}

		return childObjects;
	}

	/**
	 * Get the children of the given element
	 * 
	 * @param element
	 * @return the elements
	 */
	@Override
	public Object[] getChildren(Object parentElement) {
		if (!(parentElement instanceof IFolder)) {
			return new Object[0];
		}

		IFileStore fileStore = fileManager.getResource(rootDir.toString()).getFileStore(
				((IFolder) parentElement).getProjectRelativePath());
		IFileInfo[] childFiles;
		try {
			childFiles = fileStore.childInfos(EFS.NONE, null);
		} catch (CoreException e) {
			// This can happen if the directory only exists locally.
			return new Object[0];
		}

		IResource[] childObjects = new IResource[childFiles.length];
		for (int i = 0; i < childFiles.length; i++) {
			IPath childPath = ((IFolder) parentElement).getProjectRelativePath().addTrailingSeparator()
					.append(childFiles[i].getName());
			if (childFiles[i].isDirectory()) {
				childObjects[i] = project.getFolder(childPath);
			} else {
				childObjects[i] = project.getFile(childPath);
			}
		}

		return childObjects;
	}

	/**
	 * Get the parent of the given element
	 * 
	 * @param the
	 *            element
	 * @return the parent element
	 */
	@Override
	public Object getParent(Object element) {
		if (!(element instanceof IResource)) {
			return null;
		}

		return ((IResource) element).getParent();
	}

	/**
	 * See if element has children
	 * 
	 * @param the
	 *            element
	 * @return whether the element has any children.
	 */
	@Override
	public boolean hasChildren(Object element) {
		Object[] obj = getChildren(element);
		return obj == null ? false : obj.length > 0;
	}

	/**
	 * Provide a way to test if the remote connection is still open.
	 * 
	 * @return whether connection is still open
	 */
	public boolean isOpen() {
		return connection.isOpen();
	}

	/**
	 * Get the contents of the corresponding remote file for the given IFile. (IFile should belong to a synchronized project.)
	 * 
	 * @param file
	 * @return a stream
	 * @throws CoreException
	 *             on problems accessing the remote file.
	 * @throws MissingConnectionException
	 */
	public static BufferedInputStream getFileContents(IFile file) throws CoreException, MissingConnectionException {
		BufferedInputStream retStream = null;
		IProject project = file.getProject();
		SyncConfig config = SyncConfigManager.getActive(project);
		if (config != null) {
			IRemoteFileManager fileManager = config.getRemoteConnection().getFileManager();
			IPath remotePath = new Path(config.getLocation(project)).addTrailingSeparator().append(file.getProjectRelativePath());
			IFileStore fileStore = fileManager.getResource(remotePath.toString()); // Assumes "/" separator on remote
			InputStream fileInput = fileStore.openInputStream(EFS.NONE, null);
			if (fileInput != null) {
				retStream = new BufferedInputStream(fileInput);
			}
		} else {
			throw new CoreException(new Status(IStatus.ERROR, RDTSyncCorePlugin.PLUGIN_ID, Messages.RemoteContentProvider_1));
		}

		return retStream;
	}
}
