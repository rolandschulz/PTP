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
import org.eclipse.ptp.debug.core.pdi.request.IPDIStartDebuggerRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public class StartDebuggerRequest extends AbstractEventResultRequest implements IPDIStartDebuggerRequest {
	private final String app;
	private final String path;
	private final String dir;
	private final String[] args;

	public StartDebuggerRequest(TaskSet tasks, String app, String path, String dir, String[] args) {
		super(tasks);
		this.app = app;
		this.path = path;
		this.dir = dir;
		this.args = args;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventRequest#doExecute(org.eclipse.ptp.debug.core.pdi.IPDIDebugger)
	 */
	@Override
	public void doExecute(IPDIDebugger debugger) throws PDIException {
		debugger.startDebugger(app, path, dir, args);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getName()
	 */
	public String getName() {
		return Messages.StartDebuggerRequest_0;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventResultRequest#getResponseAction()
	 */
	@Override
	public int getResponseAction() {
		return ACTION_TERMINATED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventResultRequest#storeResult(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.ptp.proxy.debug.event.IProxyDebugEvent)
	 */
	@Override
	protected void storeResult(TaskSet rTasks, Object result) {
	}
}
