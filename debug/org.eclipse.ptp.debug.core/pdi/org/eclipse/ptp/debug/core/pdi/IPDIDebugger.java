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
package org.eclipse.ptp.debug.core.pdi;

import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.BitList;

/**
 * Represents a number of methods to communication to a debugger
 * @author clement
 *
 */
public interface IPDIDebugger extends IPDIBreakpointManagement, IPDIExecuteManagement, IPDIVariableManagement, IPDISignalManagement, IPDIStackframeManagement, IPDIThreadManagement, IPDIMemoryBlockManagement {
	/**
	 * Returns available debugging port number within given time
	 * @param timeout
	 * @return port number
	 * @throws PDIException on failure
	 */
	int getDebuggerPort(int timeout) throws PDIException;

	/**
	 * Register observer for notify event from sdm
	 * @param observer
	 */
	void register(Observer observer);

	/**
	 * Connects debugger and adds observer to debugger
	 * @param monitor
	 * @return true if connection is established
	 * @throws PDIException on failure
	 */
	boolean isConnected(IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Disconnects to debugger
	 * @param observer disconnect observer to debugger
	 * @throws PDIException on failure
	 */
	void disconnection(Observer observer) throws PDIException;
	
	/**
	 * Starts debugger
	 * @throws PDIException on failure
	 */
	void startDebugger(String app, String path, String dir, String[] args) throws PDIException;
	
	/**
	 * Stops debugger
	 * @throws PDIException on failure
	 */
	void stopDebugger() throws PDIException;
	
	/**
	 * Requests a special command for specify process
	 * @param tasks target process
	 * @param command command
	 * @throws PDIException on failure
	 */
	void commandRequest(BitList tasks, String command) throws PDIException;
	
	/**
	 * Returns an action when error occurred
	 * @param errorCode error code
	 * @return an action when error occurred
	 */
	int getErrorAction(int errorCode);
}
