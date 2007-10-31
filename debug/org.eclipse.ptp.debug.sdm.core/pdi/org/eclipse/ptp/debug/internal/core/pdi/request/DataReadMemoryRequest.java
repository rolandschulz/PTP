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
package org.eclipse.ptp.debug.internal.core.pdi.request;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataReadMemoryRequest;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.model.DataReadMemoryInfo;
import org.eclipse.ptp.debug.internal.core.pdi.model.Memory;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugMemory;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugMemoryInfo;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent;
import org.eclipse.ptp.proxy.debug.event.IProxyDebugMemoryInfoEvent;

/**
 * @author clement
 *
 */
public class DataReadMemoryRequest extends InternalEventRequest implements IPDIDataReadMemoryRequest {
	private long offset;
	private String address;
	private int wordFormat;
	private int wordSize;
	private int rows;
	private int cols;
	private Character asChar;
	
	public DataReadMemoryRequest(Session session, BitList tasks, long offset, String address, int wordFormat, int wordSize, int rows, int cols, Character asChar) {
		super(tasks);
		this.offset = offset;
		this.address = address;
		this.wordFormat = wordFormat;
		this.wordSize = wordSize;
		this.rows = rows;
		this.cols = cols;
		this.asChar = asChar;
	}
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.createDataReadMemory(tasks, offset, address, wordFormat, wordSize, rows, cols, asChar);
	}
	protected void storeResult(BitList rTasks, IProxyDebugEvent result) {
		if (result instanceof IProxyDebugMemoryInfoEvent) {
			ProxyDebugMemoryInfo info = ((IProxyDebugMemoryInfoEvent)result).getMemoryInfo();
			ProxyDebugMemory[] proxyMems = info.getMemories();
			Memory[] memories = new Memory[proxyMems.length];
			for (int i=0; i<proxyMems.length; i++) {
				memories[i] = new Memory(proxyMems[i].getAddress(), proxyMems[i].getAscii(), proxyMems[i].getData());
			}
			results.put(rTasks, new DataReadMemoryInfo(info.getAddress(), info.getNextRow(), info.getPrevRow(), info.getNextPage(), info.getPrevPage(), info.getNumBytes(), info.getTotalBytes(), memories));
		}
		else {
			storeUnknownResult(rTasks, result);
		}
	}
	public DataReadMemoryInfo getDataReadMemoryInfo(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof DataReadMemoryInfo) {
			return (DataReadMemoryInfo)obj;
		}
		throw new PDIException(qTasks, "No data read memory info");
	}
	public String getName() {
		return "Create data read memory request";
	}
}
