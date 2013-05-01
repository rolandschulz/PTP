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

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.internal.gig.views.ServerTreeItem;

/*
 * This helps with importing folders
 */
public class FolderToRecv {

	private final String name;
	// subfolders
	private final List<FolderToRecv> folders = new ArrayList<FolderToRecv>();
	// files
	private final List<String> files = new ArrayList<String>();

	public FolderToRecv(String name) {
		this.name = name;
	}

	/*
	 * Adds the item and all children to this.
	 */
	private void add(ServerTreeItem item) {
		if (item.isFolder()) {
			final FolderToRecv folder = this.getFolder(item.getName());
			folder.addAll(item.getChildren());
		}
		else {
			this.add(item.getName());
		}
	}

	/*
	 * Adds the string to the set of files. The unique property of the set is enforced by this function
	 */
	public void add(String name) {
		for (final String s : files) {
			if (s.equals(name)) {
				return;
			}
		}
		files.add(name);
	}

	public void addAll(Object[] objects) {
		for (final Object o : objects) {
			final ServerTreeItem item = (ServerTreeItem) o;
			add(item);
		}
	}

	/*
	 * Checks if this folder already exists in the list folders.
	 * If it does, returns the folder.
	 * If not, it creates the folder, adds it to folders, then returns it.
	 */
	public FolderToRecv getFolder(String name2) {
		for (final FolderToRecv folder : folders) {
			if (folder.getName().equals(name2)) {
				return folder;
			}
		}
		final FolderToRecv folder = new FolderToRecv(name2);
		folders.add(folder);
		return folder;
	}

	public String getName() {
		return name;
	}

	/*
	 * Sends this folder and all its contents, and receives the corresponding info.
	 */
	public void sendNamesRecvData(IContainer container) throws IOException, CoreException {
		GIGUtilities.sendNamesRecvData(container, name, folders, files);
	}

	@Override
	public String toString() {
		return this.name;
	}
}
