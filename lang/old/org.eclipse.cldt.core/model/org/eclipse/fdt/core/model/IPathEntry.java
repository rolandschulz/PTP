/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.fdt.core.model;

import org.eclipse.core.runtime.IPath;


public interface IPathEntry {

	/**
	 * Entry kind constant describing a path entry identifying a
	 * library. A library is an archive containing 
	 * consisting of pre-compiled binaries.
	 */
	int FDT_LIBRARY = 1;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * required project.
	 */
	int FDT_PROJECT = 2;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * folder containing source code to be compiled.
	 */
	int FDT_SOURCE = 3;

	/**
	 * Entry kind constant describing a path entry identifying a
	 * include path.
	 */
	int FDT_INCLUDE = 4;
	
	/**
	 * Entry kind constant describing a path entry representing
	 * a container id.
	 *
	 */
	int FDT_CONTAINER = 5;

	/**
	 * Entry kind constant describing a path entry representing
	 * a macro definition.
	 *
	 */
	int FDT_MACRO = 6;

	/**
	 * Entry kind constant describing output location
	 *
	 */
	int FDT_OUTPUT = 7;

	/**
	 * Returns the kind of this path entry.
	 *
	 * @return one of:
	 * <ul>
	 * <li><code>FDT_SOURCE</code> - this entry describes a source root in its project
	 * <li><code>FDT_LIBRARY</code> - this entry describes a library
	 * <li><code>FDT_PROJECT</code> - this entry describes another project
	 * <li><code>FDT_INCLUDE</code> - this entry describes a include path
	 * <li><code>FDT_MACRO</code> - this entry describes a macro definition
	 * <li><code>FDT_CONTAINER</code> - this entry describes a container id
	 * <li><code>FDT_OUTPUT</code> - this entry describes output location
	 */
	int getEntryKind();

	/**
	 * 
	 * @return true if exported.
	 */
	boolean isExported();

	/**
	 * Returns the affected IPath
	 * 
	 * @return IPath
	 */
	IPath getPath();

}
