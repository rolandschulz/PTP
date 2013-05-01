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
import org.eclipse.ptp.debug.core.pdi.request.IPDIListArgumentsRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public abstract class AbstractListArgumentsRequest extends AbstractEventResultRequest implements IPDIListArgumentsRequest {
	private int low = 0;
	private int high = 0;

	/**
	 * @since 4.0
	 */
	public AbstractListArgumentsRequest(TaskSet tasks, int low, int high) {
		super(tasks);
		this.low = low;
		this.high = high;
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
		debugger.listArguments(tasks, low, high);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIListArgumentsRequest#getArguments
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public String[] getArguments(TaskSet qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		Object obj = getResult(qTasks);
		if (obj instanceof String[]) {
			return (String[]) obj;
		}
		throw new PDIException(qTasks, Messages.AbstractListArgumentsRequest_0);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getName()
	 */
	public String getName() {
		return Messages.AbstractListArgumentsRequest_1;
	}
}
