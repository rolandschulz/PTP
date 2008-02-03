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
package org.eclipse.ptp.debug.core.pdi.request;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * @author clement
 *
 */
public abstract class AbstractListArgumentsRequest extends AbstractEventResultRequest implements IPDIListArgumentsRequest {
	private int low = 0;
	private int high = 0;
	
	public AbstractListArgumentsRequest(BitList tasks, int low, int high) {
		super(tasks);
		this.low = low;
		this.high = high;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventRequest#doExecute(org.eclipse.ptp.debug.core.pdi.IPDIDebugger)
	 */
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.listArguments(tasks, low, high);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIListArgumentsRequest#getArguments(org.eclipse.ptp.core.util.BitList)
	 */
	public String[] getArguments(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof String[]) {
			return (String[])obj;
		}
		throw new PDIException(qTasks, "No arguments found");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getName()
	 */
	public String getName() {
		return "List arguments request";
	}
}
