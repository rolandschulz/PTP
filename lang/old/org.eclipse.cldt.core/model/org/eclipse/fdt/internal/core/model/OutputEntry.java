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

import org.eclipse.core.runtime.IPath;
import org.eclipse.fdt.core.model.IOutputEntry;

/**
 * OutputEntry
 */
public class OutputEntry extends APathEntry implements IOutputEntry {
	/**
	 * @param kind
	 * @param path
	 * @param exclusionPatterns
	 * @param isExported
	 */
	public OutputEntry(IPath path, IPath[] exclusionPatterns, boolean isExported) {
		super(FDT_OUTPUT, null, null, path, exclusionPatterns, isExported);
	}

	public boolean equals(Object obj) {
		if (obj instanceof IOutputEntry) {
			IOutputEntry otherEntry = (IOutputEntry) obj;
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
