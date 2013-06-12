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

import java.math.BigInteger;

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDataReadMemoryInfo;

/**
 * A contiguous segment of memory in an execution context.
 * A memory block is represented by a starting memory address and a length.
 * 
 * @author clement
 * 
 */
public interface IPDIMemoryBlock extends IPDISessionObject {
	/**
	 * Bit mask used to indicate a byte is read-only
	 */
	public static final byte READ_ONLY = 0x01;

	/**
	 * Bit mask used to indicate a byte is valid
	 */
	public static final byte VALID = 0x02;

	/**
	 * @return
	 */
	public IPDIDataReadMemoryInfo getDataReadMemoryInfo();

	/**
	 * @return
	 */
	public String getExpression();

	/**
	 * Returns this memory byte's attribute as a bit mask.
	 * The method throw IndexOutOfBoundsException if the offset is out of range of the block.
	 * 
	 * @return this memory byte's attribute as a bit mask
	 */
	public byte getFlags(int offset);

	/**
	 * @param m
	 */
	public void setDataReadMemoryInfo(IPDIDataReadMemoryInfo m);

	/**
	 * Returns the values of the bytes currently contained in this this memory block.
	 * Note: the number maybe greater or lower to what was requested.
	 * 
	 * @return the values of the bytes currently contained in this this memory block
	 *         throws PCDIException if this method fails. Reasons include:
	 *         <ul>
	 *         <li>Failure communicating with the debug target. The PDIException's status code contains the underlying exception
	 *         responsible for the failure.</li>
	 *         </ul>
	 */
	public byte[] getBytes() throws PDIException;

	/**
	 * Returns the length of this memory block in bytes
	 * 
	 * @return the length of this memory block in bytes
	 */
	public long getLength();

	/**
	 * Returns the start address of this memory block
	 * 
	 * @return the start address of this memory block
	 */
	public BigInteger getStartAddress();

	/**
	 * Returns the size of each memory word in bytes
	 * 
	 * @return The size of each memory word in bytes
	 */
	public int getWordSize();

	/**
	 * Determines whether the block does not update
	 * 
	 * @return true if the block does not update
	 */
	public boolean isFrozen();

	/**
	 * Refresh the data, this may cause events to be trigger if the data values changed.
	 * 
	 * @throws PDIException
	 *             on failure
	 */
	public void refresh() throws PDIException;

	/**
	 * A memoryBlock set frozen means that the block will not update and check for new data
	 * 
	 * @param frozen
	 *            the block is frozen by default
	 */
	public void setFrozen(boolean frozen);

	/**
	 * Sets the value of the bytes in this memory block at the specified offset within this memory block to the specified bytes.
	 * The offset is zero based.
	 * 
	 * @param offset
	 *            the offset at which to set the new values
	 * @param bytes
	 *            the new values
	 *            throws PDIException if this method fails. Reasons include:
	 *            <ul>
	 *            <li>Failure communicating with the debug target. The PDIException's status code contains the underlying exception
	 *            responsible for the failure.</li>
	 *            <li>This memory block does not support value modification</li>
	 *            <li>The specified offset is greater than or equal to the length of this memory block, or the number of bytes
	 *            specified goes beyond the end of this memory block (index of out of range)</li>
	 *            </ul>
	 */
	public void setValue(long offset, byte[] bytes) throws PDIException;
}
