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
import java.net.URISyntaxException;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.cdt.utils.UNCPathConverter;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.NullProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.PTPRemoteCorePlugin;

public class SyncUNCPathConverter extends UNCPathConverter {
	private static Map<String, IRemoteConnection> fConnMap = new HashMap<String, IRemoteConnection>();

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.cdt.utils.UNCPathConverter#toURI(org.eclipse.core.runtime
	 * .IPath)
	 */
	@Override
	public URI toURI(IPath path) {
		/*
		 * Map the UNC server component to a connection known by one of the
		 * remote service implementations. We do this by searching through each
		 * service for a connection with the same name as the server. We keep a
		 * cache of mappings so that we only have to do this once for each
		 * server name.
		 */
		String server = path.segment(0);
		IRemoteConnection conn = fConnMap.get(server);
		if (conn == null) {
			IRemoteServices[] services = PTPRemoteCorePlugin.getDefault().getAllRemoteServices(new NullProgressMonitor());
			for (IRemoteServices service : services) {
				conn = service.getConnectionManager().getConnection(server);
				if (conn != null) {
					fConnMap.put(server, conn);
					break;
				}
			}
		}
		if (conn != null) {
			String scheme = conn.getRemoteServices().getScheme();
			String filePath = path.removeFirstSegments(1).makeAbsolute().toString();
			try {
				return new URI(scheme, server, filePath, null);
			} catch (URISyntaxException e) {
			}
		}
		return null;
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
