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
package org.eclipse.ptp.debug.core.pdi.manager;

import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequest;

/**
 * Manages a number of EventRequests for a target debugger.
 * 
 * @author clement
 * 
 */
public interface IPDIEventRequestManager extends IPDIManager {
	/**
	 * Flush any pending requests.
	 * 
	 * @since 5.0
	 */
	public void flushEventRequests();

	/**
	 * Adds an event request
	 * 
	 * @param request
	 *            event request is being added
	 * @throws PDIException
	 *             on failure
	 */
	public void addEventRequest(IPDIEventRequest request) throws PDIException;

	/**
	 * Determines whether this handler can execute on the elements specified in
	 * the given request by reporting enabled state to the request.
	 * 
	 * @param request
	 *            specifies elements to operate on and collects enabled state
	 * @return whether this handler can execute this request
	 */
	public boolean canExecute(IPDIEventRequest request);

	/**
	 * Deletes all event requests
	 * 
	 * @throws PDIException
	 *             on failure
	 */
	public void deleteAllEventRequests() throws PDIException;

	/**
	 * Deletes an event request
	 * 
	 * @param request
	 *            event request is being removed
	 * @throws PDIException
	 *             on failure
	 */
	public void deleteEventRequest(IPDIEventRequest request) throws PDIException;

	/**
	 * Executes this command on the elements specified in the given request
	 * reporting status to the given request and returns whether this handler
	 * should remain enabled while the command is executing.
	 * 
	 * @param request
	 *            specifies elements to operate on and collects execution status
	 * @throws PDIException
	 *             on failure
	 */
	public void execute(IPDIEventRequest request) throws PDIException;

	/**
	 * Returns an array of stored requests
	 * 
	 * @return an array of stored requests
	 */
	public IPDIEventRequest[] getRequests();
}
