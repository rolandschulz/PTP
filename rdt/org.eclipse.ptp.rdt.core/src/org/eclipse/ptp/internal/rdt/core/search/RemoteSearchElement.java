/*******************************************************************************
 * Copyright (c) 2006, 2011 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    QNX - Initial API and implementation
 *    Markus Schorn (Wind River Systems)
 *    IBM Corporation
 *******************************************************************************/

/* -- ST-Origin --
 * Source folder: org.eclipse.cdt.ui/src
 * Class: org.eclipse.cdt.internal.ui.search.PDOMSearchElement
 * Version: 1.12
 */

package org.eclipse.ptp.internal.rdt.core.search;

import java.io.Serializable;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.Path;

/**
 * Element class used to group matches.
 */
public class RemoteSearchElement implements IAdaptable, Serializable{

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private IIndexFileLocation location;
	
	public void setLocation(IIndexFileLocation location) {
		this.location = location;
	}

	public RemoteSearchElement(IIndexFileLocation loc) {
		this.location= loc;
	}
	
	public int hashCode() {
		return location.hashCode();
	}
	
	public boolean equals(Object obj) {
		if (!(obj instanceof RemoteSearchElement))
			return false;
		if (this == obj)
			return true;
		RemoteSearchElement other = (RemoteSearchElement)obj;
		return location.equals(other.location);
	}

	public IIndexFileLocation getLocation() {
		return location;
	}
	
	
	
	@SuppressWarnings({ "unchecked", "rawtypes" })
	public Object getAdapter(Class adapterType) {
		if (adapterType.isAssignableFrom(IFile.class)) {
			String fullPath= location.getFullPath();
			if (fullPath != null) {
				return ResourcesPlugin.getWorkspace().getRoot().getFile(new Path(fullPath));
			}
		}
		return null;
	}

}
