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
package org.eclipse.ptp.internal.debug.core.pdi.manager;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager;


/**
 * @author clement
 *
 */
public class TaskManager extends AbstractPDIManager implements IPDITaskManager {
	private TaskSet terminatedTasks;
	private TaskSet suspendedTasks;
	private TaskSet registeredTasks;
	private TaskSet pendingTasks;
	private TaskSet canStepReturnTasks;

	public TaskManager(IPDISession session) {
		super(session, false);
		terminatedTasks = new TaskSet(session.getTotalTasks());
		suspendedTasks = new TaskSet(session.getTotalTasks());
		registeredTasks = new TaskSet(session.getTotalTasks());
		pendingTasks = new TaskSet(session.getTotalTasks());
		canStepReturnTasks = new TaskSet(session.getTotalTasks());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#canAllStepReturn(org.eclipse.ptp.core.util.TaskSet)
	 */
	public boolean canAllStepReturn(TaskSet tasks) {
		return getCannotStepReturnTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getCannotStepReturnTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getCannotStepReturnTasks(TaskSet tasks) {
		tasks.andNot(getCanStepReturnTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getCanStepReturnTasks()
	 */
	public TaskSet getCanStepReturnTasks() {
		return canStepReturnTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getCanStepReturnTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getCanStepReturnTasks(TaskSet tasks) {
		tasks.and(getCanStepReturnTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getNonPendingTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getNonPendingTasks(TaskSet tasks) {
		tasks.andNot(getPendingTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getNonRunningTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getNonRunningTasks(TaskSet tasks) {
		tasks.andNot(getRunningTasks(tasks.copy()));
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getNonSuspendedTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getNonSuspendedTasks(TaskSet tasks) {
		tasks.andNot(getSuspendedTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getNonTerminatedTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getNonTerminatedTasks(TaskSet tasks) {
		tasks.andNot(getTerminatedTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getPendingTasks()
	 */
	public TaskSet getPendingTasks() {
		return pendingTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getPendingTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getPendingTasks(TaskSet tasks) {
		tasks.and(getPendingTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getRegisteredTasks()
	 */
	public TaskSet getRegisteredTasks() {
		return registeredTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getRegisteredTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getRegisteredTasks(TaskSet tasks) {
		tasks.and(getRegisteredTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getRunningTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getRunningTasks(TaskSet tasks) {
		tasks.andNot(getTerminatedTasks());
		tasks.andNot(getSuspendedTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getSuspendedTasks()
	 */
	public TaskSet getSuspendedTasks() {
		return suspendedTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getSuspendedTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getSuspendedTasks(TaskSet tasks) {
		tasks.and(getSuspendedTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getTerminatedTasks()
	 */
	public TaskSet getTerminatedTasks() {
		return terminatedTasks;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getTerminatedTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getTerminatedTasks(TaskSet tasks) {
		tasks.and(getTerminatedTasks());
		return tasks;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getUnregisteredTasks(org.eclipse.ptp.core.util.TaskSet)
	 */
	public TaskSet getUnregisteredTasks(TaskSet tasks) {
		tasks.andNot(getRegisteredTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllPending(org.eclipse.ptp.core.util.TaskSet)
	 */
	public boolean isAllPending(TaskSet tasks) {
		return getNonPendingTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllRegistered(org.eclipse.ptp.core.util.TaskSet)
	 */
	public boolean isAllRegistered(TaskSet tasks) {
		return getUnregisteredTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllRunning(org.eclipse.ptp.core.util.TaskSet)
	 */
	public boolean isAllRunning(TaskSet tasks) {
		return (!isAllSuspended(tasks) && !isAllTerminated(tasks));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllSuspended(org.eclipse.ptp.core.util.TaskSet)
	 */
	public boolean isAllSuspended(TaskSet tasks) {
		return getNonSuspendedTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllTerminated(org.eclipse.ptp.core.util.TaskSet)
	 */
	public boolean isAllTerminated(TaskSet tasks) {
		return getNonTerminatedTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setCanStepReturnTasks(boolean, org.eclipse.ptp.core.util.TaskSet)
	 */
	public void setCanStepReturnTasks(boolean isAdd, TaskSet tasks) {
		if (isAdd)
			canStepReturnTasks = addTasks(canStepReturnTasks, tasks);
		else
			removeTasks(canStepReturnTasks, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setPendingTasks(boolean, org.eclipse.ptp.core.util.TaskSet)
	 */
	public void setPendingTasks(boolean isAdd, TaskSet tasks) {
		if (isAdd)
			pendingTasks = addTasks(pendingTasks, tasks);
		else
			removeTasks(pendingTasks, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setRegisterTasks(boolean, org.eclipse.ptp.core.util.TaskSet)
	 */
	public void setRegisterTasks(boolean isAdd, TaskSet tasks) {
		if (isAdd)
			registeredTasks = addTasks(registeredTasks, tasks);
		else
			removeTasks(registeredTasks, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setSuspendTasks(boolean, org.eclipse.ptp.core.util.TaskSet)
	 */
	public void setSuspendTasks(boolean isAdd, TaskSet tasks) {
		if (isAdd)
			suspendedTasks = addTasks(suspendedTasks, tasks);
		else {
			removeTasks(suspendedTasks, tasks);
			setCanStepReturnTasks(false, tasks);//remove can step return tasks			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setTerminateTasks(boolean, org.eclipse.ptp.core.util.TaskSet)
	 */
	public void setTerminateTasks(boolean isAdd, TaskSet tasks) {
		if (isAdd) {
			terminatedTasks = addTasks(terminatedTasks, tasks);
			setSuspendTasks(false, tasks);//remove suspended tasks
		}
		else
			removeTasks(terminatedTasks, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
		/*
		terminatedTasks = null;
		suspendedTasks = null;
		registeredTasks = null;
		pendingTasks = null;
		canStepReturnTasks = null;
		*/
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.TaskSet)
	 */
	@Override
	public void update(TaskSet tasks) throws PDIException {
	}
	
	/**
	 * @param curTasks
	 * @param newTasks
	 */
	private synchronized TaskSet addTasks(TaskSet curTasks, TaskSet newTasks) {
		if (curTasks.taskSize() < newTasks.taskSize()) {
			newTasks.or(curTasks);
			curTasks =  newTasks.copy();
		}
		curTasks.or(newTasks);
		return curTasks;
	}
	
	/**
	 * @param curTasks
	 * @param newTasks
	 */
	private synchronized void removeTasks(TaskSet curTasks, TaskSet newTasks) {
		curTasks.andNot(newTasks);
	}	
}
