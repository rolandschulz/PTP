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
package org.eclipse.ptp.internal.debug.core.pdi.request;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIDataReadMemoryInfo;
import org.eclipse.ptp.debug.core.pdi.request.IPDIDataReadMemoryRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public abstract class AbstractDataReadMemoryRequest extends AbstractEventResultRequest implements IPDIDataReadMemoryRequest {
	private final long offset;
	private final String address;
	private final int wordFormat;
	private final int wordSize;
	private final int rows;
	private final int cols;
	private final Character asChar;

	/**
	 * @since 4.0
	 */
	public AbstractDataReadMemoryRequest(TaskSet tasks, long offset, String address, int wordFormat, int wordSize, int rows,
			int cols, Character asChar) {
		super(tasks);
		this.offset = offset;
		this.address = address;
		this.wordFormat = wordFormat;
		this.wordSize = wordSize;
		this.rows = rows;
		this.cols = cols;
		this.asChar = asChar;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventRequest#
	 * doExecute(org.eclipse.ptp.debug.core.pdi.IPDIDebugger)
	 */
	@Override
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.createDataReadMemory(tasks, offset, address, wordFormat, wordSize, rows, cols, asChar);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIDataReadMemoryRequest#
	 * getDataReadMemoryInfo(org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public IPDIDataReadMemoryInfo getDataReadMemoryInfo(TaskSet qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof IPDIDataReadMemoryInfo) {
			return (IPDIDataReadMemoryInfo) obj;
		}
		throw new PDIException(qTasks, Messages.AbstractDataReadMemoryRequest_0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getName()
	 */
	public String getName() {
		return Messages.AbstractDataReadMemoryRequest_1;
	}
}
