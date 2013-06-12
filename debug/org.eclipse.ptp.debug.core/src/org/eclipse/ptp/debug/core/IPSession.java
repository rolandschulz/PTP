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
package org.eclipse.ptp.debug.core;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.pdi.IPDISession;

/**
 * Interface for managing debugger session
 * 
 */
public interface IPSession extends IAdaptable {
	/**
	 * Connection to the debugger
	 * 
	 * @param monitor
	 * @param app
	 * @param path
	 * @param cwd
	 * @param args
	 * @throws CoreException
	 */
	public void connectToDebugger(IProgressMonitor monitor, String app, String path, String cwd, String[] args)
			throws CoreException;

	/**
	 * Create the debug target
	 * 
	 * @param tasks
	 * @param refresh
	 * @param register
	 * @since 4.0
	 */
	public void createDebugTarget(TaskSet tasks, boolean refresh, boolean register);

	/**
	 * Remove the debug target
	 * 
	 * @param tasks
	 * @param refresh
	 * @param register
	 * @since 4.0
	 */
	public void deleteDebugTarget(TaskSet tasks, boolean refresh, boolean register);

	/**
	 * Remove all debug targets
	 * 
	 * @param register
	 */
	public void deleteDebugTargets(boolean register);

	/**
	 * Called to dispose resources used by the session
	 */
	public void dispose();

	/**
	 * Find the debug target associated with the tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDebugTarget findDebugTarget(TaskSet tasks);

	/**
	 * Fire a debug event.
	 * 
	 * @param change
	 * @param breakpoint
	 * @param info
	 */
	public void fireDebugEvent(int change, int breakpoint, IPDebugInfo info);

	/**
	 * Force processes to terminated state. The flag isError indicates if this
	 * was as the result of an error.
	 * 
	 * @param isError
	 */
	public void forceStoppedDebugger(boolean isError);

	/**
	 * Get the breakpoint manager
	 * 
	 * @return
	 */
	public IPBreakpointManager getBreakpointManager();

	/**
	 * Get the debug info
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public IPDebugInfo getDebugInfo(TaskSet tasks);

	/**
	 * Get the launch instance used to launch this session
	 * 
	 * @return
	 */
	public IPLaunch getLaunch();

	/**
	 * Get the location set manager
	 * 
	 * @return the location set manager
	 * @since 5.0
	 */
	public IPLocationSetManager getLocationSetManager();

	/**
	 * Get the memory manager
	 * 
	 * @return
	 */
	public IPMemoryManager getMemoryManager();

	/**
	 * Get the PDI session
	 * 
	 * @return
	 */
	public IPDISession getPDISession();

	/**
	 * Get the project
	 * 
	 * @return
	 */
	public IProject getProject();

	/**
	 * Get the register manager
	 * 
	 * @return
	 */
	public IPRegisterManager getRegisterManager();

	/**
	 * Get the set manager
	 * 
	 * @return
	 */
	public IPSetManager getSetManager();

	/**
	 * Get the signal manager
	 * 
	 * @return
	 * @since 5.0
	 */
	public IPSignalManager getSignalManager();

	/**
	 * Get the tasks associated with this debug session
	 * 
	 * @return
	 * @since 4.0
	 */
	public TaskSet getTasks();

	/**
	 * Get the task with supplied id
	 * 
	 * @param id
	 * @return
	 * @since 4.0
	 */
	public TaskSet getTasks(int id);

	/**
	 * Check if the debugger is ready
	 * 
	 * @return
	 */
	public boolean isReady();

	/**
	 * Reload debug targets
	 * 
	 * @param tasks
	 * @param refresh
	 * @param register
	 * @since 4.0
	 */
	public void reloadDebugTargets(TaskSet tasks, boolean refresh, boolean register);
}
