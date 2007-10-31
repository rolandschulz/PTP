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
package org.eclipse.ptp.debug.internal.core.pdi;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDITaskManager;


/**
 * @author clement
 *
 */
public class TaskManager extends SessionObject implements IPDITaskManager {
	private BitList terminatedProcs;
	private BitList suspendedProcs;
	private BitList registeredProcs;
	private BitList pendingProcs;
	private BitList canStepReturnProcs;

	public TaskManager(Session session) {
		super(session, null);
		terminatedProcs = new BitList(session.getTotalTasks());
		suspendedProcs = new BitList(session.getTotalTasks());
		registeredProcs = new BitList(session.getTotalTasks());
		pendingProcs = new BitList(session.getTotalTasks());
		canStepReturnProcs = new BitList(session.getTotalTasks());
	}
	public void shutdown() {
		/*
		terminatedProcs = null;
		suspendedProcs = null;
		registeredProcs = null;
		pendingProcs = null;
		canStepReturnProcs = null;
		*/
	}
	public BitList getCanStepReturnTasks() {
		return canStepReturnProcs;
	}
	public BitList getPendingTasks() {
		return pendingProcs;
	}
	//only terminated tasks
	public BitList getTerminatedTasks() {
		return terminatedProcs;
	}
	//only suspended tasks
	public BitList getSuspendedTasks() {
		return suspendedProcs;
	}
	//only registered tasks
	public BitList getRegisteredTasks() {
		return registeredProcs;
	}
	public boolean canAllStepReturn(BitList tasks) {
		return getCannotStepReturnTasks(tasks.copy()).isEmpty();
	}
	public boolean isAllPending(BitList tasks) {
		return getNonPendingTasks(tasks.copy()).isEmpty();
	}
	public boolean isAllRunning(BitList tasks) {
		return (!isAllSuspended(tasks) && !isAllTerminated(tasks));
	}
	public boolean isAllSuspended(BitList tasks) {
		return getNonSuspendedTasks(tasks.copy()).isEmpty();
	}
	public boolean isAllTerminated(BitList tasks) {
		return getNonTerminatedTasks(tasks.copy()).isEmpty();
	}
	public boolean isAllRegistered(BitList tasks) {
		return getUnregisteredTasks(tasks.copy()).isEmpty();
	}
	//Returns suspended tasks only
	public BitList getSuspendedTasks(BitList tasks) {
		tasks.and(getSuspendedTasks());
		return tasks;
	}
	//Returns terminated or running tasks only
	public BitList getNonSuspendedTasks(BitList tasks) {
		tasks.andNot(getSuspendedTasks());
		return tasks;
	}
	//Returns running tasks only
	public BitList getRunningTasks(BitList tasks) {
		tasks.andNot(getTerminatedTasks());
		tasks.andNot(getSuspendedTasks());
		return tasks;
	}
	//Returns terminated or suspended tasks only
	public BitList getNonRunningTasks(BitList tasks) {
		tasks.andNot(getRunningTasks(tasks.copy()));
		return tasks;
	}
	//Returns terminated tasks only
	public BitList getTerminatedTasks(BitList tasks) {
		tasks.and(getTerminatedTasks());
		return tasks;
	}
	//Returns running or suspended tasks only
	public BitList getNonTerminatedTasks(BitList tasks) {
		tasks.andNot(getTerminatedTasks());
		return tasks;
	}
	public BitList getPendingTasks(BitList tasks) {
		tasks.and(getPendingTasks());
		return tasks;
	}
	//Returns non pending tasks
	public BitList getNonPendingTasks(BitList tasks) {
		tasks.andNot(getPendingTasks());
		return tasks;
	}
	public BitList getCanStepReturnTasks(BitList tasks) {
		tasks.and(getCanStepReturnTasks());
		return tasks;
	}
	public BitList getCannotStepReturnTasks(BitList tasks) {
		tasks.andNot(getCanStepReturnTasks());
		return tasks;
	}
	//Returns unregistered tasks only
	public BitList getUnregisteredTasks(BitList tasks) {
		tasks.andNot(getRegisteredTasks());
		return tasks;
	}
	//Returns unregistered tasks only
	public BitList getRegisteredTasks(BitList tasks) {
		tasks.and(getRegisteredTasks());
		return tasks;
	}
	public void setCanStepReturnTasks(boolean isAdd, BitList tasks) {
		BitList stepReturnTasks = getCanStepReturnTasks();
		if (isAdd)
			addTasks(stepReturnTasks, tasks);
		else
			removeTasks(stepReturnTasks, tasks);
	}
	public void setPendingTasks(boolean isAdd, BitList tasks) {
		BitList pendingTasks = getPendingTasks();
		if (isAdd)
			addTasks(pendingTasks, tasks);
		else
			removeTasks(pendingTasks, tasks);
	}
	public void setTerminateTasks(boolean isAdd, BitList tasks) {
		BitList terminatedTasks = getTerminatedTasks();
		if (isAdd) {
			addTasks(terminatedTasks, tasks);
			setSuspendTasks(false, tasks);//remove suspended tasks
		}
		else
			removeTasks(terminatedTasks, tasks);
	}
	public void setSuspendTasks(boolean isAdd, BitList tasks) {
		BitList suspendedTasks = getSuspendedTasks();
		if (isAdd)
			addTasks(suspendedTasks, tasks);
		else {
			removeTasks(suspendedTasks, tasks);
			setCanStepReturnTasks(false, tasks);//remove can step return tasks			
		}
	}
	public void setRegisterTasks(boolean isAdd, BitList tasks) {
		BitList registeredTasks = getRegisteredTasks();
		if (isAdd)
			addTasks(registeredTasks, tasks);
		else
			removeTasks(registeredTasks, tasks);
	}
	private synchronized void addTasks(BitList curTasks, BitList newTasks) {
		if (curTasks.size() < newTasks.size()) {
			newTasks.or(curTasks);
			curTasks =  newTasks.copy();
		}
		curTasks.or(newTasks);
	}
	private synchronized void removeTasks(BitList curTasks, BitList newTasks) {
		curTasks.andNot(newTasks);
	}	
}
