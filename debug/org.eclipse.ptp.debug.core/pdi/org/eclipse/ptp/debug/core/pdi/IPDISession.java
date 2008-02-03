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
import org.eclipse.ptp.debug.core.pdi.event.IPDIEventFactory;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIBreakpointManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIEventManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIEventRequestManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIExpressionManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIMemoryManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISignalManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDISourceManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITargetManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIThreadManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIModelFactory;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.request.IPDIRequestFactory;

/**
 * Represents a debug session
 * @author clement
 *
 */
public interface IPDISession extends IPDISessionObject, IPDIExecuteManagement {
	public static final int CONNECTING = 1;  
	public static final int CONNECTED = 2;
	public static final int STARTED = 3;
	public static final int EXITING = 4;
	public static final int EXITED = 5;
	
	/**
	 * @param tasks
	 */
	public void processRunningEvent(BitList tasks);
	
	/**
	 * @return
	 */
	public IPDISourceManager getSourceManager();
	
	/**
	 * @param tasks
	 * @param thread_id
	 * @param vars
	 */
	public void processSupsendedEvent(BitList tasks, final int thread_id, final String[] vars);

	/**
	 * @param monitor
	 * @param app
	 * @param path
	 * @param dir
	 * @param args
	 * @throws PDIException on failure
	 */
	public void connectToDebugger(IProgressMonitor monitor, String app, String path, String dir, String[] args) throws PDIException;
	
	/**
	 * Causes this session to exit
	 * @throws PDIException on failure
	 */
	public void exit() throws PDIException;
	
	/**
	 * Returns debug target on given task id or null if target is not registered
	 * @param tid task id
	 * @return debug target
	 * @throws PDIException on failure
	 */
	public IPDITarget findTarget(BitList task) throws PDIException;

	/**
	 * Returns the breakpoint manager for this session
	 * @return IPDIBreakpointManager the breakpoint manager for this session
	 */
	public IPDIBreakpointManager getBreakpointManager();

	/**
	 * Returns a debugger for this session
	 * @return a debugger for this session
	 */
	public IPDIDebugger getDebugger();
		
	/**
	 * Get the factory to create events for this session
	 * 
	 * @return
	 */
	public IPDIEventFactory getEventFactory();
	
	/**
	 * Returns the event manager for this session
	 * @return IPDIEventManager the event request manager for this session
	 */
	public IPDIEventManager getEventManager();
	
	/**
	 * Returns the event request manager for this session
	 * @return IPDIEventRequestManager the event request manager for this session
	 */
	public IPDIEventRequestManager getEventRequestManager();
	
	/**
	 * Returns the expression manager for this session
	 * @return IPDIExpressionManager the expression manager for this session
	 */
	public IPDIExpressionManager getExpressionManager();

	/** 
	 * Returns job id associated with this session
	 * @return job id
	 */
	public String getJobID();
	
	/**
	 * Returns the memory manager for this session
	 * @return IPDIMemoryManager the memory manager for this session
	 */
	public IPDIMemoryManager getMemoryManager();

	/**
	 * Get the factory to create model elements for this session
	 * 
	 * @return
	 */
	public IPDIModelFactory getModelFactory();

	/**
	 * Returns the register manager for this session
	 * @return IPDIRegisterManager the register manager for this session
	 */
	public IPDIRegisterManager getRegisterManager();

	/**
	 * Returns request factory
	 * @return request factory
	 */
	public IPDIRequestFactory getRequestFactory();

	/**
	 * Returns the signal manager for this session
	 * @return IPDISignalManager the signal manager for this session
	 */
	public IPDISignalManager getSignalManager();

	/**
	 * Returns current status of this session
	 * @return current status of this session
	 */
	public int getStatus();
	
	/**
	 * Returns the target manager for this session
	 * @return IPDITargetManager the target manager for this session
	 */
	public IPDITargetManager getTargetManager();

	/**
	 * Returns the task manager for this session
	 * @return IPDITaskManager the task manager for this session
	 */
	public IPDITaskManager getTaskManager();
	
	/**
	 * Returns all tasks of this session
	 * @return all tasks of this session
	 */
	public BitList getTasks();
	
	/**
	 * Returns the thread manager for this session
	 * @return IPDIThreadManager the thread manager for this session
	 */
	public IPDIThreadManager getThreadManager();
	
	/**
	 * @return
	 */
	public long getTimeout();
	
	/**
	 * Returns total tasks in this session
	 * @return total tasks in this session
	 */
	public int getTotalTasks();
	
	/**
	 * Returns the variable manager for this session
	 * @return IPDIVariableManager the variable manager for this session
	 */
	public IPDIVariableManager getVariableManager();
	
	/**
	 * Returns whether this target/thread is currently suspended.
	 * @param tasks target process
	 * @return whether this target/thread is currently suspended
	 */
	public boolean isSuspended(BitList tasks);
	
	/**
	 * Returns whether this target/thread is currently terminated.
	 * @param tasks target process
	 * @return whether this target/thread is currently terminated
	 */
	public boolean isTerminated(BitList tasks);
	
	/**
	 * @param runnable
	 */
	public void queueRunnable(Runnable runnable);
	
	/**
	 * Sets a timeout for request
	 */
	public void setRequestTimeout(long timeout);
	
	/**
	 * Set status for session
	 * @param status status of current session
	 */
	public void setStatus(int status);
	
	/**
	 * Shutdown this session 
	 * @param force whether force to terminate debugger 
	 */
	public void shutdown(boolean force);
	
	/**
	 * Checks whether request tasks can do step return
	 * @param tasks
	 * @throws PDIException
	 */
	public void validateStepReturn(BitList tasks) throws PDIException;	
}
