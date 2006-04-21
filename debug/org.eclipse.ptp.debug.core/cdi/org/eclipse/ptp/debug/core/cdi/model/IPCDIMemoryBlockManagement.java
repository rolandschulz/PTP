/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.debug.core.cdi.model;

import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public interface IPCDIMemoryBlockManagement {
	/** Returns a memory block specified by given identifier.
	 * @param address 
	 * @param units - number of units
	 * @param wordSize - The size of each memory word in bytes
	 * @return a memory block with the specified identifier
	 * @throws PCDIException on failure. Reasons include:
	 */
	IPCDIMemoryBlock createMemoryBlock(String address, int units, int wordSize) throws PCDIException;

	/** Removes the given array of memory blocks from the debug session.
	 * @param memoryBlock - the array of memory blocks to be removed
	 * @throws PCDIException on failure. Reasons include:
	 */
	void removeBlocks(IPCDIMemoryBlock[] memoryBlocks) throws PCDIException;

	/** Removes all memory blocks from the debug session.
	 * @throws PCDIException on failure. Reasons include:
	 */
	void removeAllBlocks() throws PCDIException;

	/** Returns an array of all memory blocks set for this debug session.
	 * @return an array of all memory blocks set for this debug session
	 * @throws PCDIException on failure. Reasons include:
	 */
	IPCDIMemoryBlock[] getMemoryBlocks() throws PCDIException;
}
