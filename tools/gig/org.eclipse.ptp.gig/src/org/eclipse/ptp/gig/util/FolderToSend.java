/*******************************************************************************
 * Copyright (c) 2012 Brandon Gibson
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    Brandon Gibson - initial API and implementation and/or initial documentation
 *******************************************************************************/
package org.eclipse.ptp.gig.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;

/*
 * Helps with exporting folders
 */
public class FolderToSend {

	private final IFolder folder;
	// subfolders
	protected final List<FolderToSend> foldersToSend = new ArrayList<FolderToSend>();
	protected final List<IFile> filesToSend = new ArrayList<IFile>();

	/*
	 * creates the FolderToSend object that contains this file, and adds this file to it
	 */
	public FolderToSend(IFile file) {
		final IPath path = file.getFullPath();
		final int segments = path.segmentCount();
		this.folder = file.getProject().getFolder(path.segment(1));
		if (segments > 3) {
			foldersToSend.add(new FolderToSend(folder, file, 2));
		}
		else {
			this.filesToSend.add(file);
		}
	}

	/*
	 * Boxes up the folder
	 */
	public FolderToSend(IFolder folder2) throws CoreException {
		final IPath path = folder2.getFullPath();
		final int segments = path.segmentCount();
		this.folder = folder2.getProject().getFolder(path.segment(1));
		if (segments > 2) {
			foldersToSend.add(new FolderToSend(folder, folder2, 2));
		}
		else {
			this.addAll();
		}
	}

	/*
	 * Creates this FolderToSend
	 */
	private FolderToSend(IFolder folder, boolean addAll) throws CoreException {
		this.folder = folder;
		if (addAll) {
			this.addAll();
		}
	}

	/*
	 * Recursive creation in order to have a proper file structure
	 */
	private FolderToSend(IFolder parentFolder, IFile file, int i) {
		final IPath path = file.getFullPath();
		final int segments = path.segmentCount();
		this.folder = parentFolder.getFolder(path.segment(i));
		if (segments > i + 2) {
			foldersToSend.add(new FolderToSend(folder, file, i + 1));
		}
		else {
			this.filesToSend.add(file);
		}
	}

	/*
	 * Recursive creation--folder version
	 */
	private FolderToSend(IFolder parentFolder, IFolder folder2, int i) throws CoreException {
		final IPath path = folder2.getFullPath();
		final int segments = path.segmentCount();
		this.folder = parentFolder.getFolder(path.segment(i));
		if (segments > i + 1) {
			foldersToSend.add(new FolderToSend(folder, folder2, i + 1));
		}
		else {
			this.addAll();
		}
	}

	/*
	 * Adds the folder to send recursively if needed
	 */
	private void add(FolderToSend fts) {
		for (final FolderToSend fts2 : foldersToSend) {
			if (fts.getName().equals(fts2.getName())) {
				fts2.add(fts.foldersToSend, fts.filesToSend);
				return;
			}
		}
		foldersToSend.add(fts);
	}

	/*
	 * Adds the file, but checks for duplicates
	 */
	private void add(IFile file) {
		for (final IFile curr : filesToSend) {
			if (curr.getName().equals(file.getName())) {
				return;
			}
		}
		filesToSend.add(file);
	}

	/*
	 * Adds the folders and files recursively
	 */
	public void add(List<FolderToSend> foldersToSend2, List<IFile> filesToSend2) {
		for (final FolderToSend fts : foldersToSend2) {
			this.add(fts);
		}
		for (final IFile file : filesToSend2) {
			this.add(file);
		}
	}

	/*
	 * Adds all of this folder's children to the selected children to send
	 */
	private void addAll() throws CoreException {
		final IResource[] children = folder.members();
		for (final IResource resource : children) {
			if (resource instanceof IFile) {
				filesToSend.add((IFile) resource);
			}
			else if (resource instanceof IFolder) {
				final IFolder childFolder = (IFolder) resource;
				final FolderToSend folderToSend = new FolderToSend(childFolder, true);
				foldersToSend.add(folderToSend);
			}
		}
	}

	public String getName() {
		return folder.getName();
	}

	/*
	 * Sends the data for this folder and children over the network
	 */
	public void send() throws IOException, CoreException {
		GIGUtilities.sendString(folder.getName());
		GIGUtilities.sendInt(foldersToSend.size());
		for (final FolderToSend fts : foldersToSend) {
			fts.send();
		}
		GIGUtilities.sendInt(filesToSend.size());
		for (final IFile file : filesToSend) {
			GIGUtilities.sendFile(file);
		}
	}

	@Override
	public String toString() {
		return folder.toString();
	}
}
