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

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

/*
 * The content provider for the ServerView's tree
 */
public class ServerContentProvider implements ITreeContentProvider {

	@Override
	public void dispose() {
	}

	@Override
	public Object[] getChildren(Object parentElement) {
		if (parentElement instanceof ServerTreeItem) {
			final ServerTreeItem item = (ServerTreeItem) parentElement;
			return item.getChildren();
		}
		return null;
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (inputElement instanceof ServerTreeItem) {
			final ServerTreeItem item = (ServerTreeItem) inputElement;
			return item.getChildren();
		}
		return null;
	}

	@Override
	public Object getParent(Object element) {
		if (element instanceof ServerTreeItem) {
			final ServerTreeItem item = (ServerTreeItem) element;
			return item.getParent();
		}
		return null;
	}

	@Override
	public boolean hasChildren(Object element) {
		if (element instanceof ServerTreeItem) {
			final ServerTreeItem item = (ServerTreeItem) element;
			return item.hasChildren();
		}
		return false;
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

}
