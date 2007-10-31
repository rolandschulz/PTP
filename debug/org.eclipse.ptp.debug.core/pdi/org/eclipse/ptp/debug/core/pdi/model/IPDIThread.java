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
package org.eclipse.ptp.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * A thread in a debug target.
 * A thread contains stack frames.  Stack frames are only available when the thread is suspended, and are returned in top-down order.
 * @author clement
 *
 */
public interface IPDIThread extends IPDISessionObject {
	/**
	 * Returns the stack frames contained in this thread. An empty collection is returned if this thread contains
	 * no stack frames, or is not currently suspended. Stack frames are returned in top down order.
	 * @return  a collection of stack frames
	 * @throws PDIException on failure
	 */
	IPDIStackFrame[] getStackFrames() throws PDIException;

	/**
	 * Returns the stack frames contained in this thread between the specified
     * <tt>fromIndex</tt>, inclusive, and <tt>toIndex</tt>, exclusive.
	 * An empty collection is returned if this thread contains
	 * no stack frames, or is not currently suspended. Stack frames are returned in top down order.
	 * @return  a collection of stack frames
	 * @throws PDIException on failure
	 * @throws IndexOutOfBoundsException for an illegal endpoint index value
     *     (fromIndex &lt; 0 || toIndex &gt; size || fromIndex &gt; toIndex).
 
	 */
	IPDIStackFrame[] getStackFrames(int fromIndex, int len) throws PDIException;

	/**
	 * Returns the depth of the stack frames.
	 * @return  depth of stack frames
	 * @throws PDIException on failure
	 */
	int getStackFrameCount() throws PDIException;

	/**
	 * Return thread local storage variables descriptor.
	 * @return IPDIThreadStorageDescriptor
	 * @throws PDIException on failure
	 */
	IPDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws PDIException;

	/**
	 * Create a variable from the descriptor for evaluation.  A CreatedEvent will be trigger and
	 * ChangedEvent will also be trigger when the variable is assign a new value.
	 * DestroyedEvent is fired when the variable is out of scope and automatically removed from the manager list.
	 * @param varDesc IPDThreadStorageDesc
	 * @return IPDIThreadStorage
	 * @throws PDIException on failure
	 */
	IPDIThreadStorage createThreadStorage(IPDIThreadStorageDescriptor varDesc) throws PDIException;

	/**
	 * Determines whether both threads are the same.
	 * @param thead
	 * @return true if the threads are the same.
	 */
	boolean equals(IPDIThread thead);
	
	/**
	 * Returns pdi target in this thread
	 * @return pdi target in this thread
	 */
	IPDITarget getTarget();
}
