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
	 * Check if all tasks can step return
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public boolean canAllStepReturn(TaskSet tasks);

	/**
	 * Get tasks that cannot step return
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getCannotStepReturnTasks(TaskSet tasks);

	/**
	 * Get tasks that can step return
	 * 
	 * @return
	 * @since 4.0
	 */
	public TaskSet getCanStepReturnTasks();

	/**
	 * Get tasks that can step return from the supplied set
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getCanStepReturnTasks(TaskSet tasks);

	/**
	 * Get non pending tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getNonPendingTasks(TaskSet tasks);

	/**
	 * Find terminated or suspended tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getNonRunningTasks(TaskSet tasks);

	/**
	 * Find terminated or running tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getNonSuspendedTasks(TaskSet tasks);

	/**
	 * Find running or suspended tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getNonTerminatedTasks(TaskSet tasks);

	/**
	 * @return
	 * @since 4.0
	 */
	public TaskSet getPendingTasks();

	/**
	 * Get pending tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getPendingTasks(TaskSet tasks);

	/**
	 * Get all registered tasks
	 * 
	 * @return
	 * @since 4.0
	 */
	public TaskSet getRegisteredTasks();

	/**
	 * Find registered tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getRegisteredTasks(TaskSet tasks);

	/**
	 * Find running tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getRunningTasks(TaskSet tasks);

	/**
	 * Get all suspended tasks
	 * 
	 * @return
	 * @since 4.0
	 */
	public TaskSet getSuspendedTasks();

	/**
	 * Find suspended tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getSuspendedTasks(TaskSet tasks);

	/**
	 * Get all terminated tasks
	 * 
	 * @return
	 * @since 4.0
	 */
	public TaskSet getTerminatedTasks();

	/**
	 * Find terminated tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getTerminatedTasks(TaskSet tasks);

	/**
	 * Find unregistered tasks
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public TaskSet getUnregisteredTasks(TaskSet tasks);

	/**
	 * Check if all tasks are pending
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public boolean isAllPending(TaskSet tasks);

	/**
	 * Check if all tasks are registered
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public boolean isAllRegistered(TaskSet tasks);

	/**
	 * Check if all tasks are running
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public boolean isAllRunning(TaskSet tasks);

	/**
	 * Check if all tasks are suspended
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public boolean isAllSuspended(TaskSet tasks);

	/**
	 * Check if all tasks are terminated
	 * 
	 * @param tasks
	 * @return
	 * @since 4.0
	 */
	public boolean isAllTerminated(TaskSet tasks);

	/**
	 * Set tasks that can step return
	 * 
	 * @param isAdd
	 * @param tasks
	 * @since 4.0
	 */
	public void setCanStepReturnTasks(boolean isAdd, TaskSet tasks);

	/**
	 * Set pending tasks
	 * 
	 * @param isAdd
	 * @param tasks
	 * @since 4.0
	 */
	public void setPendingTasks(boolean isAdd, TaskSet tasks);

	/**
	 * Set registered tasks
	 * 
	 * @param isAdd
	 * @param tasks
	 * @since 4.0
	 */
	public void setRegisterTasks(boolean isAdd, TaskSet tasks);

	/**
	 * Set suspended tasks
	 * 
	 * @param isAdd
	 * @param tasks
	 * @since 4.0
	 */
	public void setSuspendTasks(boolean isAdd, TaskSet tasks);

	/**
	 * Set terminated asks
	 * 
	 * @param isAdd
	 * @param tasks
	 * @since 4.0
	 */
	public void setTerminateTasks(boolean isAdd, TaskSet tasks);
}
