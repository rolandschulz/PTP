/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.remotetools.core;

import java.net.URI;
import java.net.URISyntaxException;

import org.eclipse.cdt.core.EFSExtensionProvider;
import org.eclipse.core.runtime.Path;

public class RemoteToolsExtensionProvider extends EFSExtensionProvider {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.core.EFSExtensionProvider#createNewURIFromPath(java.net
	 * .URI, java.lang.String)
	 */
	@Override
	public URI createNewURIFromPath(URI locationOnSameFilesystem, String path) {
		URI uri = locationOnSameFilesystem;

		Path p = new Path(path);
		String pathString = p.toString(); // to convert any backslashes to
											// slashes if we are on Windows
		final int length = pathString.length();
		StringBuffer pathBuf = new StringBuffer(length + 1);

		// force the path to be absolute
		if (length > 0 && (pathString.charAt(0) != '/')) {
			pathBuf.append('/');
		}
		// additional double-slash for UNC paths to distinguish from host
		// separator
		if (pathString.startsWith("//")) //$NON-NLS-1$
			pathBuf.append('/').append('/');
		pathBuf.append(pathString);

		/*
		 * The default EFSExtensionProvider mistakenly assumes that the
		 * authority is server based. This implementation should work for any
		 * authority.
		 */
		try {
			return new URI(uri.getScheme(), uri.getAuthority(), pathBuf.toString(), // replaced!
					uri.getQuery(), uri.getFragment());
		} catch (URISyntaxException e) {
			// Should log something
		}
		return null;
	}
}
