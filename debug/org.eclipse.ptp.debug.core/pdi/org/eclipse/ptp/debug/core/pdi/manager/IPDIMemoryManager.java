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
package org.eclipse.ptp.debug.core.pdi.manager;

import java.math.BigInteger;
import java.util.List;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemoryBlock;

/**
 * Represent memory manager to manage memory
 * 
 * @author clement
 * 
 */
public interface IPDIMemoryManager extends IPDIManager {
	/**
	 * Returns a memory block specified by given identifier.
	 * 
	 * @param qTasks
	 *            target process
	 * @param address
	 * @param units
	 *            - number of bytes
	 * @param wordSize
	 *            - this parameter has been deprecated and will always be passed
	 *            as the value 1. If the memory has an addressable size (number
	 *            of bytes per address) greather than 1, the PDI client should
	 *            take care not to
	 * @return a memory block with the specified identifier
	 * @throws PDIException
	 *             on failure
	 * @since 4.0
	 */
	public IPDIMemoryBlock createMemoryBlock(TaskSet qTasks, String address, int units, int wordSize) throws PDIException;

	/**
	 * Returns an array of all memory blocks set for this debug session.
	 * 
	 * @param qTasks
	 *            target process
	 * @return an array of all memory blocks set for this debug session
	 * @throws PDIException
	 *             on failure
	 * @since 4.0
	 */
	public IPDIMemoryBlock[] getMemoryBlocks(TaskSet qTasks) throws PDIException;

	/**
	 * Requests to remove all memory blocks from the debug session.
	 * 
	 * @param tasks
	 *            target process
	 * @throws PDIException
	 *             on failure
	 * @since 4.0
	 */
	public void removeAllBlocks(TaskSet tasks) throws PDIException;

	/**
	 * Requests to remove the given array of memory blocks from the debug
	 * session.
	 * 
	 * @param tasks
	 *            target process
	 * @param memoryBlock
	 *            - the array of memory blocks to be removed
	 * @throws PDIException
	 *             on failure
	 * @since 4.0
	 */
	public void removeBlocks(TaskSet tasks, IPDIMemoryBlock[] memoryBlocks) throws PDIException;

	/**
	 * Update memory blocks
	 * 
	 * @param block
	 * @param aList
	 * @return
	 * @throws PDIException
	 */
	public BigInteger[] update(IPDIMemoryBlock block, List<IPDIEvent> aList) throws PDIException;
}
