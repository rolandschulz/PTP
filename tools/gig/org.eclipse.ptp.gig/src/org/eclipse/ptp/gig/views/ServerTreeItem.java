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
package org.eclipse.ptp.gig.views;

import java.util.ArrayList;
import java.util.List;

/*
 * An item from the ServerView's tree. Either a file or a folder with a (possibly empty) list of children.
 */
public class ServerTreeItem {

	private final List<ServerTreeItem> children = new ArrayList<ServerTreeItem>();
	private final ServerTreeItem parent;
	private final String name;
	private final boolean isFolder;

	/*
	 * This is for items contained directly by the project.
	 */
	public ServerTreeItem(String name) {
		this.name = name;
		this.parent = null;
		this.isFolder = true;
	}

	/*
	 * This is for items contained by some other part of the tree.
	 */
	public ServerTreeItem(String name, ServerTreeItem parent, boolean isFolder) {
		this.name = name;
		this.parent = parent;
		parent.add(this);
		this.isFolder = isFolder;
	}

	/*
	 * adds the serverTreeItem to the children
	 */
	private void add(ServerTreeItem serverTreeItem) {
		children.add(serverTreeItem);
	}

	@Override
	public boolean equals(Object o) {
		if (o instanceof ServerTreeItem) {
			final ServerTreeItem item = (ServerTreeItem) o;
			if (this.parent == null) {
				if (item.parent == null) {
					return true;
				}
				else {
					return false;
				}
			}
			else {
				if (item.parent == null) {
					return false;
				}
				else {
					return this.name.equals(item.name) && this.parent.equals(item.parent);
				}
			}
		}
		return false;
	}

	/*
	 * Returns the children.
	 */
	public Object[] getChildren() {
		return children.toArray();
	}

	/*
	 * returns the relative path of this
	 */
	public String getFullName() {
		if (parent.parent == null) {
			return name;
		}
		// we want the linux file separator, since this is the one that the server will use
		return parent.getFullName() + '/' + name;
	}

	public String getName() {
		return name;
	}

	public ServerTreeItem getParent() {
		return parent;
	}

	public boolean hasChildren() {
		return children.size() > 0;
	}

	public boolean isFolder() {
		return isFolder;
	}

	@Override
	public String toString() {
		return name;
	}

}