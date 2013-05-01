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
package org.eclipse.ptp.internal.gig.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;

/*
 * Facilitates in sending folders and files
 */
public class ProjectToSend {
	private final List<FolderToSend> foldersToSend = new ArrayList<FolderToSend>();
	private final List<IFile> filesToSend = new ArrayList<IFile>();

	/*
	 * Argument is a folder to be added to the project, if already there, merge
	 */
	public void add(FolderToSend folderToSend) {
		for (final FolderToSend curr : foldersToSend) {
			if (curr.getName().equals(folderToSend.getName())) {
				curr.add(folderToSend.foldersToSend, folderToSend.filesToSend);
				return;
			}
		}
		foldersToSend.add(folderToSend);
	}

	/*
	 * Adds the specified file.
	 */
	public void add(IFile file) {
		if (file.getFullPath().segmentCount() > 2) {
			final FolderToSend fts = new FolderToSend(file);
			this.add(fts);
		}
		else {
			this.filesToSend.add(file);
		}
	}

	/*
	 * sends this project. Assumes connection is already established.
	 */
	public void send() throws IOException, CoreException {
		GIGUtilities.sendInt(foldersToSend.size());
		for (final FolderToSend fts : foldersToSend) {
			fts.send();
		}
		GIGUtilities.sendInt(filesToSend.size());
		for (final IFile file : filesToSend) {
			GIGUtilities.sendFile(file);
		}
	}

}
