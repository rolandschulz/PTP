/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import java.net.URI;

import org.eclipse.core.resources.IProject;
import org.eclipse.remote.core.IRemoteConnection;
import org.eclipse.remote.core.IRemoteResource;
import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.RemoteServices;

/**
 * This utility finds the remote URI and
 * RemoteConnection objects for a project.
 * These operations should be generic enough
 * to work for remote and synchronized projects.
 * 
 * @author Steven R. Brandt
 *
 */
public class Util {

	public static URI getLocationURI(IProject prj) {
		IRemoteResource resource = (IRemoteResource) prj.getAdapter(IRemoteResource.class);
		if (resource != null) {
			URI locationURI = resource.getActiveLocationURI();
			return locationURI;
		}
		return null;
	}

	public static IRemoteConnection getRemoteConnection(IProject prj) {
		IRemoteResource resource = (IRemoteResource) prj.getAdapter(IRemoteResource.class);
		if (resource != null) {
			URI locationURI = resource.getActiveLocationURI();
			if (locationURI != null) {
				IRemoteServices services = RemoteServices.getRemoteServices(locationURI);
				if (services != null) {
					IRemoteConnection conn = services.getConnectionManager().getConnection(locationURI);
					if (conn != null) {
						return conn;
					}
				}
			}
		}
		return null;
	}
}
