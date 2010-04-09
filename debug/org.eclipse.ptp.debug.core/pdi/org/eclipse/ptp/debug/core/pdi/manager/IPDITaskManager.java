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

import org.eclipse.ptp.debug.core.TaskSet;

/**
 * Represent expression manager to manage processes
 * 
 * @author clement
 * 
 */
public interface IPDITaskManager extends IPDIManager {
	/**
	 * @param tasks
	 * @return
	 */
	public boolean canAllStepReturn(TaskSet tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public TaskSet getCannotStepReturnTasks(TaskSet tasks);

	/**
	 * @return
	 */
	public TaskSet getCanStepReturnTasks();

	/**
	 * @param tasks
	 * @return
	 */
	public TaskSet getCanStepReturnTasks(TaskSet tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public TaskSet getNonPendingTasks(TaskSet tasks);

	/**
	 * Find terminated or suspended tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public TaskSet getNonRunningTasks(TaskSet tasks);

	/**
	 * Find terminated or running tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public TaskSet getNonSuspendedTasks(TaskSet tasks);

	/**
	 * Find running or suspended tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public TaskSet getNonTerminatedTasks(TaskSet tasks);

	/**
	 * @return
	 */
	public TaskSet getPendingTasks();

	/**
	 * @param tasks
	 * @return
	 */
	public TaskSet getPendingTasks(TaskSet tasks);

	/**
	 * Get all registered tasks
	 * 
	 * @return
	 */
	public TaskSet getRegisteredTasks();

	/**
	 * Find registered tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public TaskSet getRegisteredTasks(TaskSet tasks);

	/**
	 * Find running tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public TaskSet getRunningTasks(TaskSet tasks);

	/**
	 * Get all suspended tasks
	 * 
	 * @return
	 */
	public TaskSet getSuspendedTasks();

	/**
	 * Find suspended tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public TaskSet getSuspendedTasks(TaskSet tasks);

	/**
	 * Get all terminated tasks
	 * 
	 * @return
	 */
	public TaskSet getTerminatedTasks();

	/**
	 * Find terminated tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public TaskSet getTerminatedTasks(TaskSet tasks);

	/**
	 * Find unregistered tasks
	 * 
	 * @param tasks
	 * @return
	 */
	public TaskSet getUnregisteredTasks(TaskSet tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllPending(TaskSet tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllRegistered(TaskSet tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllRunning(TaskSet tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllSuspended(TaskSet tasks);

	/**
	 * @param tasks
	 * @return
	 */
	public boolean isAllTerminated(TaskSet tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setCanStepReturnTasks(boolean isAdd, TaskSet tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setPendingTasks(boolean isAdd, TaskSet tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setRegisterTasks(boolean isAdd, TaskSet tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setSuspendTasks(boolean isAdd, TaskSet tasks);

	/**
	 * @param isAdd
	 * @param tasks
	 */
	public void setTerminateTasks(boolean isAdd, TaskSet tasks);
}
