/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.internal.core.model;


import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.model.CoreModelUtil;
import org.eclipse.fdt.core.model.ICElement;
import org.eclipse.fdt.core.model.ISourceEntry;
import org.eclipse.fdt.core.model.ISourceRoot;

/**
 * SourceRoot
 */
public class SourceRoot extends CContainer implements ISourceRoot {

	ISourceEntry sourceEntry;

	/**
	 * @param parent
	 * @param res
	 */
	public SourceRoot(ICElement parent, IResource res, ISourceEntry entry) {
		super(parent, res);
		sourceEntry = entry;
		IPath path = getPath();
		IPath cpath = getParent().getPath();
		if (path.segmentCount() > cpath.segmentCount()) {
			IPath p = path.removeFirstSegments(cpath.segmentCount());
			setElementName(p.toString());
		}
	}

	public ISourceEntry getSourceEntry() {
		return sourceEntry;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.ISourceRoot#isOnclasspath(org.eclipse.fdt.core.model.ICElement)
	 */
	public boolean isOnSourceEntry(ICElement element) {
		IPath path = element.getPath();
		return this.isOnSourceEntry(path);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.core.model.ISourceRoot#isOnSourceEntry(org.eclipse.core.resources.IResource)
	 */
	public boolean isOnSourceEntry(IResource res) {
		IPath path = res.getFullPath();
		return isOnSourceEntry(path);
	}

	public boolean isOnSourceEntry(IPath path) {
		if (sourceEntry.getPath().isPrefixOf(path) 
				&& !CoreModelUtil.isExcluded(path, sourceEntry.fullExclusionPatternChars())) {
			return true;
		}
		return false;
	}

}
