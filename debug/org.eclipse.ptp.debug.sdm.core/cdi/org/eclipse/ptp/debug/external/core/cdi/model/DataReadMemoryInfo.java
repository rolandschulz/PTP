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
package org.eclipse.ptp.debug.external.core.cdi.model;


/**
 * @author Clement chu
 * 
 */
public class DataReadMemoryInfo {
	String addr;
	long nextRow;
	long prevRow;
	long nextPage;
	long prevPage;
	long numBytes;
	long totalBytes;
	Memory[] memories;
	
	/** Constructor
	 * @param addr
	 * @param nextRow
	 * @param prevRow
	 * @param nextPage
	 * @param prevPage
	 * @param numBytes
	 * @param totalBytes
	 * @param memories
	 */
	public DataReadMemoryInfo(String addr, long nextRow, long prevRow, long nextPage, long prevPage, long numBytes, long totalBytes, Memory[] memories) {
		this.addr = addr;
		this.nextRow = nextRow;
		this.prevRow = prevRow;
		this.nextPage = nextPage;
		this.prevPage = prevPage;
		this.numBytes = numBytes;
		this.totalBytes = totalBytes;
		this.memories = memories;
	}
	/** Get address
	 * @return
	 */
	public String getAddress() {
		return addr;
	}
	/** Get number of bytes
	 * @return
	 */
	public long getNumberBytes() {
		return numBytes;
	}
	/** Get total bytes
	 * @return
	 */
	public long getTotalBytes() {
		return totalBytes;
	}
	/** Get next row
	 * @return
	 */
	public long getNextRow() {
		return nextRow;
	}
	/** Get previous row
	 * @return
	 */
	public long getPreviousRow() {
		return prevRow;
	}
	/** Get next page
	 * @return
	 */
	public long getNextPage() {
		return nextPage;
	}
	/** Get previous page
	 * @return
	 */
	public long getPreviousPage() {
		return prevPage;
	}
	/** Get memories
	 * @return
	 */
	public Memory[] getMemories() {
		return memories;
	}	
}

