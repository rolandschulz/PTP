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

package org.eclipse.fdt.debug.core.cdi.model;

import org.eclipse.fdt.debug.core.cdi.CDIException;

/**
 * Manages the sharedLibraries in the target.
 */
public interface ICDISharedLibraryManagement {

	/**
	 * Returns the array of shared libraries for this target.
	 * 
	 * @return ICDISharedLibrary[] array
	 * @throws CDIException on failure.
	 */
	ICDISharedLibrary[] getSharedLibraries() throws CDIException;

}
