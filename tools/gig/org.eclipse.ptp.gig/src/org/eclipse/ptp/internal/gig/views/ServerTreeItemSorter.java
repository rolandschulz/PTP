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
package org.eclipse.ptp.internal.gig.views;

import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerSorter;

/*
 * This is the Sorter that sorts for the ServerView's Tree.
 */
public class ServerTreeItemSorter extends ViewerSorter {

	@Override
	public int category(Object a) {
		// We generally want the folders first, the files second.
		final ServerTreeItem item = (ServerTreeItem) a;
		if (item.isFolder()) {
			return 1;
		}
		else {
			return 2;
		}
	}

	@Override
	public int compare(Viewer viewer, Object a, Object b) {
		final ServerTreeItem item1 = (ServerTreeItem) a;
		final ServerTreeItem item2 = (ServerTreeItem) b;
		return item1.getName().compareTo(item2.getName());
	}
}
