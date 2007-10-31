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
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;


/**
 * @author clement
 *
 */
public abstract class EventRequest implements IPDIEventRequest {
	protected int status = UNKNOWN;
	protected BitList tasks = null;
	protected String message = "";
	
	public EventRequest(BitList tasks) {
		this.tasks = tasks.copy();
	}
	public BitList getTasks() {
		return tasks;
	}
	public void cancel() {
		try {
			doFinish();
			this.status = IPDIEventRequest.CANCELLED;
		} catch (PDIException e) {
			error(e.getMessage());
		}
	}
	public void done() {
		try {
			doFinish();
			this.status = IPDIEventRequest.DONE;
		} catch (PDIException e) {
			error(e.getMessage());
		}
	}
	public void execute(IPDIDebugger debugger) {
		try {
			doExecute(debugger);
			this.status = IPDIEventRequest.RUNNING;
		} catch (PDIException e) {
			error(e.getMessage());
		}
	}
	public void error(String message) {
		this.status = IPDIEventRequest.ERROR;
		this.message = message;
		try {
			doFinish();
		} catch (PDIException e) {
			this.message += " - " + e.getMessage();
		}
	}
	public String getErrorMessage() {
		return message;
	}
	public int getStatus() {
		return status;
	}
	public void setStatus(int status) {
		this.status = status;
	}
	public boolean completed(BitList cTasks, Object result) {
		tasks.andNot(cTasks);
		return tasks.isEmpty();
	}
	protected abstract void doExecute(IPDIDebugger debugger) throws PDIException;
	protected abstract void doFinish() throws PDIException;
	
	public String toString() {
		return getName() + " in status [" + status + "] for " + BitList.showBitList(getTasks()) + ".";
	}
	
    public int getResponseAction() {
    	return ACTION_NONE;// default action
    }
    
    public boolean sendEvent() {
    	return true;
    }
}
