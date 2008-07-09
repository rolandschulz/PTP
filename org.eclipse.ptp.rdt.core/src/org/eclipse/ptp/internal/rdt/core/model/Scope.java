/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
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

/**
 * Describes the context in which a service-based operation will be
 * performed.  Ultimately, a scope is a name associated with a collection of files.
 */
public class Scope implements Serializable {

	/**
	 * Special scope that refers to everything in the workspace.
	 */
	public static final String WORKSPACE_ROOT_SCOPE_NAME = "__WORKSPACE_ROOT_SCOPE__"; //$NON-NLS-1$
	
	public static final Scope WORKSPACE_ROOT_SCOPE = new Scope(WORKSPACE_ROOT_SCOPE_NAME);
	
	private static final long serialVersionUID = 1L;
	
	private String fName;
	
	public Scope(String name) {
		fName = name;
	}
	
	/**
	 * Returns the name of this scope.
	 * @return the name of this scope.
	 */
	public String getName() {
		return fName;
	}
}
