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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IRequestFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.request.IPDIEventRequestManager;

/**
 * Represents a debug session
 * @author clement
 *
 */
public interface IPDISession extends IPDIExecuteManagement {
	public static final int CONNECTING = 1;  
	public static final int CONNECTED = 2;
	public static final int STARTED = 3;
	public static final int EXITING = 4;
	public static final int EXITED = 5;
	
	/**
	 * Returns request factory
	 * @return request factory
	 */
	IRequestFactory getRequestFactory();
	
	/**
	 * Shutdown this session 
	 * @param force whether force to terminate debugger 
	 */
	void shutdown(boolean force);
	
	/** 
	 * Returns job id associated with this session
	 * @return job id
	 */
	String getJobID();
	
	/**
	 * Causes this session to exit
	 * @throws PDIException on failure
	 */
	void exit() throws PDIException;

	/**
	 * Returns the event request manager for this session
	 * @return IPDIEventRequestManager the event request manager for this session
	 */
	IPDIEventRequestManager getEventRequestManager();
	
	/**
	 * Returns the event manager for this session
	 * @return IPDIEventManager the event request manager for this session
	 */
	IPDIEventManager getEventManager();

	/**
	 * Returns the task manager for this session
	 * @return IPDITaskManager the task manager for this session
	 */
	IPDITaskManager getTaskManager();

	/**
	 * Returns the breakpoint manager for this session
	 * @return IPDIBreakpointManager the breakpoint manager for this session
	 */
	IPDIBreakpointManager getBreakpointManager();

	/**
	 * Returns the register manager for this session
	 * @return IPDIRegisterManager the register manager for this session
	 */
	IPDIRegisterManager getRegisterManager();

	/**
	 * Returns the memory manager for this session
	 * @return IPDIMemoryManager the memory manager for this session
	 */
	IPDIMemoryManager getMemoryManager();

	/**
	 * Returns the target manager for this session
	 * @return IPDITargetManager the target manager for this session
	 */
	IPDITargetManager getTargetManager();

	/**
	 * Returns the variable manager for this session
	 * @return IPDIVariableManager the variable manager for this session
	 */
	IPDIVariableManager getVariableManager();
	
	/**
	 * Returns the thread manager for this session
	 * @return IPDIThreadManager the thread manager for this session
	 */
	IPDIThreadManager getThreadManager();

	/**
	 * Returns the expression manager for this session
	 * @return IPDIExpressionManager the expression manager for this session
	 */
	IPDIExpressionManager getExpressionManager();
	
	/**
	 * Returns the signal manager for this session
	 * @return IPDISignalManager the signal manager for this session
	 */
	IPDISignalManager getSignalManager();
	
	/**
	 * Returns a debugger for this session
	 * @return a debugger for this session
	 */
	IPDIDebugger getDebugger();
	
	/**
	 * Returns debug target on given task id or null if target is not registered
	 * @param tid task id
	 * @return debug target
	 * @throws PDIException on failure
	 */
	IPDITarget findTarget(BitList task) throws PDIException;
	
	/**
	 * Set status for session
	 * @param status status of current session
	 */
	void setStatus(int status);
	
	/**
	 * Returns current status of this session
	 * @return current status of this session
	 */
	int getStatus();
	
	/**
	 * Returns whether this target/thread is currently suspended.
	 * @param tasks target process
	 * @return whether this target/thread is currently suspended
	 */
	boolean isSuspended(BitList tasks);
	
	/**
	 * Returns whether this target/thread is currently terminated.
	 * @param tasks target process
	 * @return whether this target/thread is currently terminated
	 */
	boolean isTerminated(BitList tasks);
	
	/**
	 * Returns all tasks of this session
	 * @return all tasks of this session
	 */
	BitList getTasks();
	
	/**
	 * Returns total tasks in this session
	 * @return total tasks in this session
	 */
	int getTotalTasks();
	
	/**
	 * @param monitor
	 * @param app
	 * @param path
	 * @param dir
	 * @param args
	 * @throws PDIException on failure
	 */
	void connectToDebugger(IProgressMonitor monitor, String app, String path, String dir, String[] args) throws PDIException;
	
	/**
	 * Sets a timeout for request
	 */
	void setRequestTimeout(long timeout);
	
	/**
	 * Checks whether request tasks can do step return
	 * @param tasks
	 * @throws PDIException
	 */
	void validateStepReturn(BitList tasks) throws PDIException;	
}
