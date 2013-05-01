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
package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.pdi.event.IPDIDataReadMemoryInfo;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMemory;

/**
 * @author Clement chu
 * 
 */
public class DataReadMemoryInfo implements IPDIDataReadMemoryInfo {
	String addr;
	long nextRow;
	long prevRow;
	long nextPage;
	long prevPage;
	long numBytes;
	long totalBytes;
	IPDIMemory[] memories;

	public DataReadMemoryInfo(String addr, long nextRow, long prevRow,
			long nextPage, long prevPage, long numBytes, long totalBytes,
			IPDIMemory[] memories) {
		this.addr = addr;
		this.nextRow = nextRow;
		this.prevRow = prevRow;
		this.nextPage = nextPage;
		this.prevPage = prevPage;
		this.numBytes = numBytes;
		this.totalBytes = totalBytes;
		this.memories = memories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIDataReadMemoryInfo#getAddress()
	 */
	public String getAddress() {
		return addr;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIDataReadMemoryInfo#getMemories()
	 */
	public IPDIMemory[] getMemories() {
		return memories;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIDataReadMemoryInfo#getNextPage()
	 */
	public long getNextPage() {
		return nextPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIDataReadMemoryInfo#getNextRow()
	 */
	public long getNextRow() {
		return nextRow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIDataReadMemoryInfo#getNumberBytes()
	 */
	public long getNumberBytes() {
		return numBytes;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIDataReadMemoryInfo#getPreviousPage()
	 */
	public long getPreviousPage() {
		return prevPage;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIDataReadMemoryInfo#getPreviousRow()
	 */
	public long getPreviousRow() {
		return prevRow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.IPDIDataReadMemoryInfo#getTotalBytes()
	 */
	public long getTotalBytes() {
		return totalBytes;
	}
}
