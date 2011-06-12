/*******************************************************************************
 * Copyright (c) 2008, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    IBM Corporation - Initial API and implementation
 *******************************************************************************/ 

package org.eclipse.ptp.internal.rdt.core.model;

import java.io.Serializable;
import java.net.URI;

import org.eclipse.cdt.utils.EFSExtensionManager;
import org.eclipse.core.resources.IProject;

/**
 * Describes the context in which a service-based operation will be
 * performed.  Ultimately, a scope is a name associated with a collection of files.
 * There is a one to one mapping between scopes and index databases (.pdom files).
 * Scopes are analogous to projects.  Each scope can be hosted only by one EFS scheme,
 * and scopes are used to populate the CModel's ICProject nodes.  Scopes may optionally
 * be mapped to paths in the local filesystem on the client.
 */
public class Scope implements Serializable {

	/**
	 * Special scope that refers to everything in the workspace.
	 */
	public static final String WORKSPACE_ROOT_SCOPE_NAME = "__WORKSPACE_ROOT_SCOPE__"; //$NON-NLS-1$
	
	public static final Scope WORKSPACE_ROOT_SCOPE = new Scope(WORKSPACE_ROOT_SCOPE_NAME, null, null, null, null);
	
	private static final long serialVersionUID = 1L;
	
	private String fName;
	
	private String fScheme;
	
	private String fMappedPath;

	private String fHost;

	private String fRootPath;
	
	public Scope(String name, String scheme, String host, String rootPath, String mappedPath) {
		fName = name;
		fScheme = scheme;
		fHost = host;
		fRootPath = rootPath;
		fMappedPath = mappedPath;
	}
	
	/**
	 * Create a scope for a project.
	 * @throws NullPointerException if project is null
	 */
	public Scope(IProject project) {
		EFSExtensionManager fsUtilityManager = EFSExtensionManager.getDefault();
		URI locationURI = project.getLocationURI();
		
		fName = project.getName();
		fScheme = locationURI.getScheme();
		fMappedPath = fsUtilityManager.getMappedPath(locationURI);
		fRootPath = fsUtilityManager.getPathFromURI(locationURI);
		
		URI managedURI = fsUtilityManager.getLinkedURI(locationURI); 
		fHost = managedURI == null ? locationURI.getHost() : managedURI.getHost();
	}
	
	/**
	 * Returns the name of this scope.
	 * @return the name of this scope.
	 */
	public String getName() {
		return fName;
	}
	
	/**
	 * Returns the URI scheme which should be used to access this scope, or <code>null</code> if
	 * this scope is the workspace scope.
	 * 
	 * @return
	 */
	public String getScheme() {
		return fScheme;
	}
	
	/**
	 * If the scope is mapped to a local path on the client, returns the local path, or <code>null</code> if the scope
	 * is not mapped.
	 * 
	 * @return
	 */
	public String getMappedPath() {
		return fMappedPath;
	}

	public String getHost() {
		return fHost;
	}

	public String getRootPath() {
		return fRootPath;
	}
}
