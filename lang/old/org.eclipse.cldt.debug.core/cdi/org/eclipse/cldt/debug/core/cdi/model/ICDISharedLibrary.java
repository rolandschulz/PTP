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
package org.eclipse.cldt.debug.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.cldt.debug.core.cdi.CDIException;

/**
 * 
 * Represents a shared library which has been loaded into 
 * the debug target.
 * 
 * @since Jul 8, 2002
 */
public interface ICDISharedLibrary extends ICDIObject {
	/**
	 * Returns the name of shared library file.
	 * 
	 * @return the name of shared library file
	 */
	String getFileName();
	
	/**
	 * Returns the start address of this library.
	 * 
	 * @return the start address of this library
	 */
	BigInteger getStartAddress();

	/**
	 * Returns the end address of this library.
	 * 
	 * @return the end address of this library
	 */
	BigInteger getEndAddress();

	/**
	 * Returns whether the symbols of this library are read.
	 *
	 * @return whether the symbols of this library are read
	 */
	boolean areSymbolsLoaded();
	
	/**
	 * Loads the library symbols.
	 * 
	 * @throws CDIException if this method fails.  Reasons include:
	 */
	void loadSymbols() throws CDIException;
}
