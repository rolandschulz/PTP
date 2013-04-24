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

import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.ptp.gig.GIGPlugin;
import org.eclipse.swt.graphics.Image;

/*
 * The Label Provider for the ServerView's tree.
 */
public class ServerLabelProvider extends LabelProvider {

	@Override
	public Image getImage(Object object) {
		final ServerTreeItem item = (ServerTreeItem) object;
		if (item.isFolder()) {
			return GIGPlugin.getImageDescriptor("icons/fldr_obj.gif").createImage(); //$NON-NLS-1$
		}
		return GIGPlugin.getImageDescriptor("icons/file_obj.gif").createImage(); //$NON-NLS-1$
	}

	@Override
	public String getText(Object element) {
		final ServerTreeItem item = (ServerTreeItem) element;
		return item.getName();
	}
}