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

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.gig.views.ServerTreeItem;

/*
 * Facilitates the receiving of data into a project via the import.
 */
public class ProjectToRecv {

	private final List<FolderToRecv> folders = new ArrayList<FolderToRecv>();
	private final List<String> files = new ArrayList<String>();

	/*
	 * Adds the item and all folders it is contained in.
	 * If addAll is true and item is a folder, then all items in it will also be added.
	 * Returns it as a folder if it is a folder, null otherwise
	 */
	public FolderToRecv add(ServerTreeItem item, boolean addAll) {
		final ServerTreeItem parentItem = item.getParent();
		if (parentItem.getParent() == null) {
			if (item.isFolder()) {
				FolderToRecv targetFolder = null;
				for (final FolderToRecv folder : folders) {
					if (item.getName().equals(folder.getName())) {
						targetFolder = folder;
						break;
					}
				}
				if (targetFolder != null) {
					if (addAll) {
						targetFolder.addAll(item.getChildren());
					}
					return targetFolder;
				}
				else {
					targetFolder = new FolderToRecv(item.getName());
					folders.add(targetFolder);
					if (addAll) {
						targetFolder.addAll(item.getChildren());
					}
					return targetFolder;
				}
			}
			else {
				files.add(item.getName());
				return null;
			}
		}
		else {
			final FolderToRecv parentFolder = this.add(parentItem, false);
			if (item.isFolder()) {
				final FolderToRecv folder = parentFolder.getFolder(item.getName());
				if (addAll) {
					folder.addAll(item.getChildren());
				}
				return folder;
			}
			else {
				parentFolder.add(item.getName());
				return null;
			}
		}
	}

	/*
	 * see GIGUtilites.sendNamesRecvData
	 */
	public void sendNamesRecvData(IProject project) throws IOException, IncorrectPasswordException, CoreException,
			IllegalCommandException {
		GIGUtilities.sendNamesRecvData(project, folders, files);
	}

}
