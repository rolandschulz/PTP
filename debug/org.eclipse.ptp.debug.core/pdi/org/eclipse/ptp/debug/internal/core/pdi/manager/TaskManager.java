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
package org.eclipse.ptp.debug.internal.core.pdi.manager;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager;


/**
 * @author clement
 *
 */
public class TaskManager extends AbstractPDIManager implements IPDITaskManager {
	private BitList terminatedTasks;
	private BitList suspendedTasks;
	private BitList registeredTasks;
	private BitList pendingTasks;
	private BitList canStepReturnTasks;

	public TaskManager(IPDISession session) {
		super(session, false);
		terminatedTasks = new BitList(session.getTotalTasks());
		suspendedTasks = new BitList(session.getTotalTasks());
		registeredTasks = new BitList(session.getTotalTasks());
		pendingTasks = new BitList(session.getTotalTasks());
		canStepReturnTasks = new BitList(session.getTotalTasks());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#canAllStepReturn(org.eclipse.ptp.core.util.BitList)
	 */
	public boolean canAllStepReturn(BitList tasks) {
		return getCannotStepReturnTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getCannotStepReturnTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getCannotStepReturnTasks(BitList tasks) {
		tasks.andNot(getCanStepReturnTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getCanStepReturnTasks()
	 */
	public BitList getCanStepReturnTasks() {
		return canStepReturnTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getCanStepReturnTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getCanStepReturnTasks(BitList tasks) {
		tasks.and(getCanStepReturnTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getNonPendingTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getNonPendingTasks(BitList tasks) {
		tasks.andNot(getPendingTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getNonRunningTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getNonRunningTasks(BitList tasks) {
		tasks.andNot(getRunningTasks(tasks.copy()));
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getNonSuspendedTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getNonSuspendedTasks(BitList tasks) {
		tasks.andNot(getSuspendedTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getNonTerminatedTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getNonTerminatedTasks(BitList tasks) {
		tasks.andNot(getTerminatedTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getPendingTasks()
	 */
	public BitList getPendingTasks() {
		return pendingTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getPendingTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getPendingTasks(BitList tasks) {
		tasks.and(getPendingTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getRegisteredTasks()
	 */
	public BitList getRegisteredTasks() {
		return registeredTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getRegisteredTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getRegisteredTasks(BitList tasks) {
		tasks.and(getRegisteredTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getRunningTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getRunningTasks(BitList tasks) {
		tasks.andNot(getTerminatedTasks());
		tasks.andNot(getSuspendedTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getSuspendedTasks()
	 */
	public BitList getSuspendedTasks() {
		return suspendedTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getSuspendedTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getSuspendedTasks(BitList tasks) {
		tasks.and(getSuspendedTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getTerminatedTasks()
	 */
	public BitList getTerminatedTasks() {
		return terminatedTasks;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getTerminatedTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getTerminatedTasks(BitList tasks) {
		tasks.and(getTerminatedTasks());
		return tasks;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#getUnregisteredTasks(org.eclipse.ptp.core.util.BitList)
	 */
	public BitList getUnregisteredTasks(BitList tasks) {
		tasks.andNot(getRegisteredTasks());
		return tasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllPending(org.eclipse.ptp.core.util.BitList)
	 */
	public boolean isAllPending(BitList tasks) {
		return getNonPendingTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllRegistered(org.eclipse.ptp.core.util.BitList)
	 */
	public boolean isAllRegistered(BitList tasks) {
		return getUnregisteredTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllRunning(org.eclipse.ptp.core.util.BitList)
	 */
	public boolean isAllRunning(BitList tasks) {
		return (!isAllSuspended(tasks) && !isAllTerminated(tasks));
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllSuspended(org.eclipse.ptp.core.util.BitList)
	 */
	public boolean isAllSuspended(BitList tasks) {
		return getNonSuspendedTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#isAllTerminated(org.eclipse.ptp.core.util.BitList)
	 */
	public boolean isAllTerminated(BitList tasks) {
		return getNonTerminatedTasks(tasks.copy()).isEmpty();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setCanStepReturnTasks(boolean, org.eclipse.ptp.core.util.BitList)
	 */
	public void setCanStepReturnTasks(boolean isAdd, BitList tasks) {
		if (isAdd)
			canStepReturnTasks = addTasks(canStepReturnTasks, tasks);
		else
			removeTasks(canStepReturnTasks, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setPendingTasks(boolean, org.eclipse.ptp.core.util.BitList)
	 */
	public void setPendingTasks(boolean isAdd, BitList tasks) {
		if (isAdd)
			pendingTasks = addTasks(pendingTasks, tasks);
		else
			removeTasks(pendingTasks, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setRegisterTasks(boolean, org.eclipse.ptp.core.util.BitList)
	 */
	public void setRegisterTasks(boolean isAdd, BitList tasks) {
		if (isAdd)
			registeredTasks = addTasks(registeredTasks, tasks);
		else
			removeTasks(registeredTasks, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setSuspendTasks(boolean, org.eclipse.ptp.core.util.BitList)
	 */
	public void setSuspendTasks(boolean isAdd, BitList tasks) {
		if (isAdd)
			suspendedTasks = addTasks(suspendedTasks, tasks);
		else {
			removeTasks(suspendedTasks, tasks);
			setCanStepReturnTasks(false, tasks);//remove can step return tasks			
		}
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDITaskManager#setTerminateTasks(boolean, org.eclipse.ptp.core.util.BitList)
	 */
	public void setTerminateTasks(boolean isAdd, BitList tasks) {
		if (isAdd) {
			terminatedTasks = addTasks(terminatedTasks, tasks);
			setSuspendTasks(false, tasks);//remove suspended tasks
		}
		else
			removeTasks(terminatedTasks, tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#shutdown()
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
	 * @see org.eclipse.ptp.debug.internal.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.BitList)
	 */
	@Override
	public void update(BitList tasks) throws PDIException {
	}
	
	/**
	 * @param curTasks
	 * @param newTasks
	 */
	private synchronized BitList addTasks(BitList curTasks, BitList newTasks) {
		if (curTasks.size() < newTasks.size()) {
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
	private synchronized void removeTasks(BitList curTasks, BitList newTasks) {
		curTasks.andNot(newTasks);
	}	
}
