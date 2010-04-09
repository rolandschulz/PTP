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

import java.util.List;
import java.util.Observer;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.ptp.debug.core.TaskSet;

/**
 * Represents a number of methods to communication to a debugger
 * @author clement
 *
 */
public interface IPDIDebugger extends 
			IPDIBreakpointManagement, 
			IPDIExecuteManagement, 
			IPDIVariableManagement, 
			IPDISignalManagement, 
			IPDIStackframeManagement, 
			IPDIThreadManagement, 
			IPDIMemoryBlockManagement {
	/**
	 * Requests a special command for specify process
	 * @param tasks target process
	 * @param command command
	 * @throws PDIException on failure
	 */
	public void commandRequest(TaskSet tasks, String command) throws PDIException;

	/**
	 * Disconnects observer from debugger
	 * @param observer disconnect observer from debugger
	 * @throws PDIException on failure
	 */
	public void disconnect(Observer observer) throws PDIException;
	
	/**
	 * Returns an action when error occurred
	 * 
	 * @param errorCode error code
	 * @return an action when error occurred
	 */
	public int getErrorAction(int errorCode);
	
	/**
	 * Connects debugger and adds observer to debugger
	 * 
	 * @param monitor
	 * @return true if connection is established
	 * @throws PDIException on failure
	 */
	public boolean isConnected(IProgressMonitor monitor) throws PDIException;
	
	/**
	 * Register observer for notify event from sdm
	 * 
	 * @param observer
	 */
	public void register(Observer observer);
	
	/**
	 * Starts debugger
	 * 
	 * @throws PDIException on failure
	 */
	public void startDebugger(String app, String path, String dir, String[] args) throws PDIException;
	
	/**
	 * Stops debugger
	 * 
	 * @throws PDIException on failure
	 */
	public void stopDebugger() throws PDIException;
	
	/**
	 * Initialize the debugger. If args is an empty list, attempt to initialize the debugger
	 * and add any necessary arguments to the list. If args is not empty, then attempt to
	 * initialize the debugger using the supplied arguments.
	 * 
	 * @param configuration debugger launch configuration
	 * @param args debugger arguments
	 * @param monitor progress monitor
	 * @throws PDIException
	 */
	public void initialize(ILaunchConfiguration configuration, List<String> args, IProgressMonitor monitor) throws PDIException;
}
