/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cldt.debug.core.cdi.model;

import java.math.BigInteger;

import org.eclipse.cldt.debug.core.cdi.CDIException;

/**
 * The memory manager manages the collection of memory blocks 
 * specified for the debug session.
 * 
 * ICDIMemoryBlockManagement
 * 
 */
public interface ICDIMemoryBlockManagement {

	/**
	 * Returns a memory block specified by given identifier.
	 * 
	 * @param address 
	 * @param length - how much for address
	 * @return a memory block with the specified identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock createMemoryBlock(String address, int length)
		throws CDIException;

	/**
	 * Returns a memory block specified by given identifier.
	 * 
	 * @param address 
	 * @param length - how much for address
	 * @return a memory block with the specified identifier
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock createMemoryBlock(BigInteger address, int length)
		throws CDIException;

	/**
	 * Removes the given array of memory blocks from the debug session.
	 * 
	 * @param memoryBlock - the array of memory blocks to be removed
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeBlocks(ICDIMemoryBlock[] memoryBlocks) throws CDIException;

	/**
	 * Removes all memory blocks from the debug session.
	 * 
	 * @exception CDIException on failure. Reasons include:
	 */
	void removeAllBlocks() throws CDIException;

	/**
	 * Returns an array of all memory blocks set for this debug session.
	 *
	 * @return an array of all memory blocks set for this debug session
	 * @throws CDIException on failure. Reasons include:
	 */
	ICDIMemoryBlock[] getMemoryBlocks() throws CDIException;

}
