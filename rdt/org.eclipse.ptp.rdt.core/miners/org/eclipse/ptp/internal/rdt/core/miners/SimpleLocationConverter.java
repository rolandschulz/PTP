/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
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
import org.eclipse.dstore.core.model.DataStore;

public class SimpleLocationConverter implements IIndexLocationConverter {

	String fScheme;
	String fHost;
	DataStore fDataStore;

	public SimpleLocationConverter(String scheme, String host, DataStore datastore) {
				
		fScheme = scheme;
		fHost = host;
		fDataStore = datastore;
	}
	
	public IIndexFileLocation fromInternalFormat(String raw) {
		try {
			
			URI uri = createURIForScheme(fScheme, fHost, raw);
			return new RemoteIndexFileLocation(null, uri);
		} catch (URISyntaxException e) {
			throw new IllegalArgumentException(e);
		} 
	}

	private URI createURIForScheme(String scheme, String host, String path) throws URISyntaxException {
		
		if(scheme == null || scheme.equals("")) { //$NON-NLS-1$
			scheme = ScopeManager.getInstance().getSchemeForFile(path);
		}
		
		// create the URI
		URI newURI = URICreatorManager.getDefault(fDataStore).createURI(scheme, host, path);
		
		return newURI;
	}

	public String toInternalFormat(IIndexFileLocation location) {
		// TODO Auto-generated method stub
		return null;
	}

}
