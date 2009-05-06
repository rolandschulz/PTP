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

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.index.IIndexFileLocation;
import org.eclipse.cdt.core.index.IIndexLocationConverter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;

public class SimpleLocationConverter implements IIndexLocationConverter {

	String fScheme;
	String fHost;

	public SimpleLocationConverter(String scheme, String host) {
		fScheme = scheme;
		fHost = host;
	}
	
	public IIndexFileLocation fromInternalFormat(String raw) {
		try {
			URI internalURI = new URI(raw);
			IPath path = new Path(internalURI.getPath());

			URI uri = new URI(fScheme, fHost, path.toString(), null, null);
			return new RemoteIndexFileLocation(null, uri);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		} 
	}

	public String toInternalFormat(IIndexFileLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

}
