/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.rdt.core.miners;

import java.io.Serializable;
import java.net.URI;

import org.eclipse.cdt.core.index.IIndexFileLocation;

public class RemoteIndexFileLocation implements IIndexFileLocation, Serializable {
	private static final long serialVersionUID = 1L;
	
	URI fURI;
	String fPath;

	public RemoteIndexFileLocation(String path, URI uri) {
		fURI = uri;
		fPath = null;
	}
	
	public RemoteIndexFileLocation(IIndexFileLocation location) {
		if (location != null) {
			fURI = location.getURI();
			fPath = location.getFullPath();
		}
	}

	public String getFullPath() {
		return fPath;
	}

	public URI getURI() {
		return fURI;
	}

	@Override
	public String toString() {
		if (fURI != null) {
			return fURI.toString();
		}
		if (fPath != null) {
			return fPath.toString();
		}
		return ""; //$NON-NLS-1$
	}
	
	@Override
	public boolean equals(Object o) {
		if (!(o instanceof IIndexFileLocation)) {
			return false;
		}
		IIndexFileLocation other = (IIndexFileLocation) o; 
		String fullPath = other.getFullPath();
		URI uri = other.getURI();
		if (fPath == null && fullPath != null) {
			return false;
		}
		if (fURI == null && fURI != null) {
			return false;
		}
		if (fPath != null && !fPath.equals(fullPath)) {
			return false;
		}
		if (fURI != null && !fURI.equals(uri)) {
			return false;
		}
		return true;
	}
	
	@Override
	public int hashCode() {
		int pathHash = fPath != null ? fPath.hashCode() : 0;
		int uriHash = fURI != null ? fURI.hashCode() : 0;
		return pathHash * 31 + uriHash;
	}
}
