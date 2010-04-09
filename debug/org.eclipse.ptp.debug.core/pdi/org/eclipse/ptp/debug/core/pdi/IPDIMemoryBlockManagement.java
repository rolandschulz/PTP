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
package org.eclipse.ptp.debug.core.pdi;

import org.eclipse.ptp.debug.core.TaskSet;

/**
 * The memory manager manages the collection of memory blocks specified for the debug session.
 * @author clement
 *
 */
public interface IPDIMemoryBlockManagement {
	/**
	 * Requests to create data read memory
	 * @param tasks target process
	 * @param offset 
	 * @param address
	 * @param wordFormat HEXADECIMAL || OCTAL || BINARY || DECIMAL || RAW || NATURAL
	 * @param wordSize The size of each memory word in bytes
	 * @param rows
	 * @param cols
	 * @param asChar
	 * @throws PDIException on failure
	 */
	void createDataReadMemory(TaskSet tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols, Character asChar) throws PDIException;
	
	/**
	 * Requests to create data write memory
	 * @param tasks target process
	 * @param offset
	 * @param address
	 * @param wordFormat HEXADECIMAL || OCTAL || BINARY || DECIMAL || RAW || NATURAL
	 * @param wordSize
	 * @param value
	 * @throws PDIException
	 */
	void createDataWriteMemory(TaskSet tasks, long offset, String address, int wordFormat, int wordSize, String value) throws PDIException;
}
