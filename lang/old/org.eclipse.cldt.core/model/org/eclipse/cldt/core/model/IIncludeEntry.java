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
package org.eclipse.cldt.core.model;

import org.eclipse.core.runtime.IPath;

public interface IIncludeEntry extends IPathEntry {

	/**
	 * Returns the include path 
	 * @return IPath
	 */
	IPath getIncludePath();

	/**
	 * Return the base path of the includePath
	 * @return IPath
	 */
	IPath getBasePath();

	/**
	 * Return the includePath with the base path.
	 * 
	 * @return
	 */
	IPath getFullIncludePath();

	/**
	 * Return the reference path
	 * 
	 * @return
	 */
	IPath getBaseReference();

	/**
	 * Whether or not it a system include path
	 * @return boolean
	 */
	boolean isSystemInclude();

	/**
	 * If isRecursive() is true, specify an exclude file patterns.
	 * @return IPath
	 */
	IPath[] getExclusionPatterns();

	/**
	 * Returns a char based representation of the exclusions patterns full path.
	 */
	public char[][] fullExclusionPatternChars();

}
