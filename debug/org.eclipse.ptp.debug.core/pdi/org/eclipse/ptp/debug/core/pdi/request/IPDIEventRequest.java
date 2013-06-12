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

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDIDebugger;
import org.eclipse.ptp.debug.core.pdi.IPDISet;

/**
 * Represents a request for notification of an event.
 * 
 * @author clement
 * 
 */
public interface IPDIEventRequest extends IPDISet {
	// status
	public final static int ERROR = 0x1;
	public final static int RUNNING = 0x2;
	public final static int DONE = 0x3;
	public final static int CANCELLED = 0x4;
	public final static int UNKNOWN = 0x5;

	public final static int ACTION_NONE = 0x1; // by default, nothing
	public final static int ACTION_TERMINATED = 0x2; // terminate all remaining
														// tasks

	/**
	 * Sets current status of this request
	 * 
	 * @param status
	 */
	public void setStatus(int status);

	/**
	 * Returns the current status of this request
	 * 
	 * @return the current status of this request
	 */
	public int getStatus();

	/**
	 * Indicates this request is complete. Clients must call this method whether
	 * the request succeeds, fails, or is cancelled to indicate that processing
	 * is complete. Only clients fulfilling a request should call this method.
	 * Clients making a request are not intended to call this method.
	 */
	public void done();

	/**
	 * Cancels this request. A request may be cancelled by the originator of
	 * request or a client fulfilling a request. Optionally a cancelled status
	 * may be set on this request with more details. A client fulfilling a
	 * request must still call <code>done()</code> to indicate the request is
	 * complete.
	 */
	public void cancel();

	/**
	 * Indicates this request is executing.
	 * 
	 * @param debugger
	 */
	public void execute(IPDIDebugger debugger);

	/**
	 * Indicates this request has an error.
	 * 
	 * @param message
	 *            error message
	 */
	public void error(String message);

	/**
	 * Returns name of this request
	 * 
	 * @return name of this request
	 */
	public String getName();

	/**
	 * Returns error message of this request
	 * 
	 * @return error message of this request
	 */
	public String getErrorMessage();

	/**
	 * Returns whether tasks are completed and set result
	 * 
	 * @param qTasks
	 * @param result
	 * @return true if the tasks are completed
	 * @since 4.0
	 */
	public boolean completed(TaskSet qTasks, Object result);

	/**
	 * Returns action type for action after completing this request
	 * 
	 * @return action type
	 */
	public int getResponseAction();
}
