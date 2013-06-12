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

import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Maintains the list of directories to search for source files.
 * 
 * @author clement
 * 
 */
public interface IPDISourceManagement {
	/**
	 * Set the source search paths for the debug session.
	 * 
	 * @param String
	 *            array of search paths
	 * @throws PDIException
	 *             on failure
	 */
	public void setSourcePaths(String[] srcPaths) throws PDIException;

	/**
	 * Return the array of source paths
	 * 
	 * @return String array of search paths.
	 * @throws PDIException
	 *             on failure
	 */
	public String[] getSourcePaths() throws PDIException;

	/**
	 * @param startAddress
	 *            is the begining address
	 * @param endAddress
	 *            is the end address
	 * @throws PDIException
	 *             on failure.
	 */
	public IPDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws PDIException;

	/**
	 * @param filename
	 *            is the name of the file to disassemble
	 * @param linenum
	 *            is the line number to disassemble around
	 * @throws PDIException
	 *             on failure.
	 */
	public IPDIInstruction[] getInstructions(String filename, int linenum) throws PDIException;

	/**
	 * @param filename
	 *            is the name of the file to disassemble
	 * @param linenum
	 *            is the line number to disassemble around
	 * @param lines
	 *            is the number of disassembly to produced
	 * @throws PDIException
	 *             on failure.
	 */
	public IPDIInstruction[] getInstructions(String filename, int linenum, int lines) throws PDIException;

	/**
	 * @param startAddress
	 *            is the begining address
	 * @param endAddress
	 *            is the end address
	 * @throws PDIException
	 *             on failure.
	 */
	public IPDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws PDIException;

	/**
	 * @param filename
	 *            is the name of the file to disassemble
	 * @param linenum
	 *            is the line number to disassemble around
	 * @param lines
	 *            is the number of disassembly to produced
	 * @throws PDIException
	 *             on failure.
	 */
	public IPDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws PDIException;

	/**
	 * @param filename
	 *            is the name of the file to disassemble
	 * @param linenum
	 *            is the line number to disassemble around
	 * @param lines
	 *            is the number of disassembly to produced
	 * @throws PDIException
	 *             on failure.
	 */
	public IPDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws PDIException;
}
