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
package org.eclipse.cldt.internal.core.model;

import org.eclipse.cldt.core.model.IPathEntry;
import org.eclipse.cldt.core.model.ISourceEntry;
import org.eclipse.core.runtime.IPath;

public class SourceEntry extends APathEntry implements ISourceEntry {

	/**
	 * 
	 * @param path
	 * @param exclusionPatterns
	 */
	public SourceEntry(IPath sourcePath, IPath[] exclusionPatterns) {
		super(IPathEntry.CDT_SOURCE, null, null, sourcePath, exclusionPatterns, false);
	}

	public boolean equals (Object obj) {
		if (obj instanceof ISourceEntry) {
			ISourceEntry otherEntry = (ISourceEntry)obj;
			if (!super.equals(otherEntry)) {
				return false;
			}
			if (path == null) {
				if (otherEntry.getPath() != null) {
					return false;
				}
			} else {
				if (!path.toString().equals(otherEntry.getPath().toString())) {
					return false;
				}
			}
			return true;
		}
		return super.equals(obj);
	}

}
