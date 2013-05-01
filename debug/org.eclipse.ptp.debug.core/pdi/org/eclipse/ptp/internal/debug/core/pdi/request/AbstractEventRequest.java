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

import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public abstract class AbstractEventRequest implements IPDIEventRequest {
	protected int status = UNKNOWN;
	protected TaskSet tasks = null;
	protected String message = ""; //$NON-NLS-1$

	/**
	 * @since 4.0
	 */
	public AbstractEventRequest(TaskSet tasks) {
		this.tasks = tasks.copy();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#cancel()
	 */
	public void cancel() {
		try {
			doFinish();
			this.status = IPDIEventRequest.CANCELLED;
		} catch (PDIException e) {
			error(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#completed(org
	 * .eclipse.ptp.core.util.TaskSet, java.lang.Object)
	 */
	/**
	 * @since 4.0
	 */
	public boolean completed(TaskSet cTasks, Object result) {
		tasks.andNot(cTasks);
		return tasks.isEmpty();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#done()
	 */
	public void done() {
		try {
			doFinish();
			this.status = IPDIEventRequest.DONE;
		} catch (PDIException e) {
			error(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#error(java.lang
	 * .String)
	 */
	public void error(String message) {
		this.status = IPDIEventRequest.ERROR;
		this.message = message;
		try {
			doFinish();
		} catch (PDIException e) {
			this.message += " - " + e.getMessage(); //$NON-NLS-1$
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#execute(org.eclipse
	 * .ptp.debug.core.pdi.IPDIDebugger)
	 */
	public void execute(IPDIDebugger debugger) {
		try {
			doExecute(debugger);
		} catch (PDIException e) {
			error(e.getMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getErrorMessage()
	 */
	public String getErrorMessage() {
		return message;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getResponseAction
	 * ()
	 */
	public int getResponseAction() {
		return ACTION_NONE;// default action
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#getStatus()
	 */
	public int getStatus() {
		return status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.IPDISet#getTasks()
	 */
	/**
	 * @since 4.0
	 */
	public TaskSet getTasks() {
		return tasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest#setStatus(int)
	 */
	public void setStatus(int status) {
		this.status = status;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		String statusStr = Messages.AbstractEventRequest_0;

		switch (status) {
		case ERROR:
			statusStr = Messages.AbstractEventRequest_1;
			break;
		case RUNNING:
			statusStr = Messages.AbstractEventRequest_2;
			break;
		case DONE:
			statusStr = Messages.AbstractEventRequest_3;
			break;
		case CANCELLED:
			statusStr = Messages.AbstractEventRequest_4;
			break;
		}

		return NLS.bind(Messages.AbstractEventRequest_5, new Object[] { getName(), statusStr, getTasks() });
	}

	/**
	 * @param debugger
	 * @throws PDIException
	 */
	protected abstract void doExecute(IPDIDebugger debugger) throws PDIException;

	/**
	 * @throws PDIException
	 */
	protected abstract void doFinish() throws PDIException;
}
