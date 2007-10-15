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

package org.eclipse.ptp.proxy.debug.client;

public class ProxyDebugMemoryInfo {
	private String addr;
	private long nextRow;
	private long prevRow;
	private long nextPage;
	private long prevPage;
	private long numBytes;
	private long totalBytes;
	private ProxyDebugMemory[] memories;
	
	public ProxyDebugMemoryInfo(String addr, String nextRow, String prevRow, 
			String nextPage, String prevPage, String numBytes, String totalBytes, 
			ProxyDebugMemory[] memories) {
		this.addr = addr;
		this.nextRow = Long.parseLong(nextRow);
		this.prevRow = Long.parseLong(prevRow);
		this.nextPage = Long.parseLong(nextPage);
		this.prevPage = Long.parseLong(prevPage);
		this.numBytes = Long.parseLong(numBytes);
		this.totalBytes = Long.parseLong(totalBytes);
		this.memories = memories;
	}
	
	public String getAddress() {
		return addr;
	}
	
	public long getNextRow() {
		return nextRow;
	}
	
	public long getPrevRow() {
		return prevRow;
	}
	
	public long getNextPage() {
		return nextPage;
	}
	
	public long getPrevPage() {
		return prevPage;
	}
	
	public long getNumBytes() {
		return numBytes;
	}
	
	public long getTotalBytes() {
		return totalBytes;
	}
	
	public ProxyDebugMemory[] getMemories() {
		return memories;
	}
}
