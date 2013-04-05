/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Greg Watson (IBM Corporation) - initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.rdt.sync.core;

import java.net.URI;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.RemoteServicesUtils;

public class SyncUNCPathConverter extends UNCPathConverter {
	private static Map<IPath, URI> fConnMap = new HashMap<IPath, URI>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.utils.UNCPathConverter#toURI(org.eclipse.core.runtime
	 * .IPath)
	 */
	@Override
	public URI toURI(IPath path) {
		URI uri = fConnMap.get(path);
		if (uri == null) {
			uri = RemoteServicesUtils.toURI(path);
			fConnMap.put(path, uri);
		}
		return uri;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.cdt.utils.UNCPathConverter#toURI(java.lang.String)
	 */
	@Override
	public URI toURI(String path) {
		return toURI(new Path(path));
	}
}
