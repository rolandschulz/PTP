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
package org.eclipse.ptp.debug.external.core.commands;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;


/** This is an abstract class of debug command.  It contains all common actions and functions of all debug commands.
 * @author Clement chu
 * 
 */
public abstract class AbstractDebugCommand implements IDebugCommand {
	protected final Object lock = new Object();
	
	protected BitList tasks = null;
	protected BitList check_tasks = null;
	protected Object result = null;
	protected boolean waitForReturn = false;
	protected boolean interrupt = false;
	protected boolean flush = false;
	protected boolean cancelled = false;
	protected long timeout = 20000;
	protected boolean waitInQueue = false;
	protected boolean waitAgain = false;
	protected boolean blocked = false;
	
	protected boolean command_finish = false;
	protected int priority = PRIORITY_M;
	
	/** constructor
	 * @param tasks
	 */
	public AbstractDebugCommand(BitList tasks) {
		this(tasks, false, true);
	}
	/** constructor
	 * @param tasks the tasks for this command
	 * @param interrupt whether this command can interrupt other commands or not
	 * @param waitForReturn whether this command should wait for return value
	 */
	public AbstractDebugCommand(BitList tasks, boolean interrupt, boolean waitForReturn) {
		this(tasks, interrupt, waitForReturn, true);
	}
	/** constructor
	 * @param tasks the tasks for this command
	 * @param interrupt whether this command can interrupt other commands or not
	 * @param waitForReturn whether this command should wait for return value
	 * @param waitInQueue whether this command should be queuing or jump the queue (no need to wait to execuate)
	 */
	public AbstractDebugCommand(BitList tasks, boolean interrupt, boolean waitForReturn, boolean waitInQueue) {
		if (tasks == null) {
			throw new IllegalArgumentException("Tasks cannot be null"); 
		}
		this.tasks = tasks;
		this.interrupt = interrupt;
		this.waitForReturn = waitForReturn;
		this.waitInQueue = waitInQueue;
		presetTimeout(PTPDebugCorePlugin.getDefault().getCommandTimeout());
	}
	public int getPriority() {
		return priority;
	}
	public void setPriority(int priority) {
		this.priority = priority;
	}
	public boolean isWaitInQueue() {
		return waitInQueue;
	}
	public boolean canInterrupt() {
		return interrupt;
	}
	public BitList getTasks() {
		return tasks;
	}
	public boolean isWaitForReturn() {
		synchronized (lock) {
			return waitForReturn;
		}
	}
	protected Object getReturn() {
		synchronized (lock) {
			return result;
		}
	}
	protected boolean checkReturn() throws PCDIException {
		synchronized (lock) {
			Object result = getReturn();
			if (result == null) {
				if (check_tasks != null && !check_tasks.isEmpty()) {
					throw new PCDIException("Incomplete - Command " + getCommandName());
				}
				throw new PCDIException("Time out - Command " + getCommandName(), IPCDIErrorEvent.DBG_NORMAL);
			}
			if (isBlocked()) {
				throw new PCDIException("Time out - Command " + getCommandName(), IPCDIErrorEvent.DBG_NORMAL);
			}
			if (result.equals(RETURN_INCOMPLETE)) {
				throw new PCDIException("Incomplete - Command " + getCommandName());
			}
			if (result.equals(RETURN_NOTHING)) {
				throw new PCDIException("Unknown error - Command " + getCommandName());
			}
			if (result instanceof PCDIException) {
				//if (((PCDIException)result).getErrorCode() == IPCDIErrorEvent.DBG_NORMAL) {
					//return false;
				//}
				throw (PCDIException)getReturn();
			}
			if (result.equals(RETURN_ERROR)) {
				throw new PCDIException("Tasks do not match with <" + getCommandName() + "> command.");
			}
			if (result.equals(RETURN_CANCEL)) {
				throw new PCDIException("Cancelled - command " + getCommandName());
			}
			if (result.equals(RETURN_FLUSH)) {
				return false;
			}		
			return true;
		}
	}
	protected boolean isBlocked() {
    	synchronized (lock) {
    		return blocked;
    	}
	}
    protected void releaseLock() {
    	synchronized (lock) {
    		waitAgain = false;
    		blocked = false;
    		lock.notifyAll();
    	}
    }
    protected void lockAgain() {
    	synchronized (lock) {
    		waitAgain = true;
    		lock.notify();
    	}
    }
	//wait again for return back
	protected void doWait(long timeout) throws InterruptedException {
		synchronized (lock) {
			do {
				waitAgain = false;
				blocked = true;
				lock.wait(timeout);
			} while (waitAgain);
		}
	}
	public boolean waitForReturn() throws PCDIException {
		synchronized (lock) {
			return waitForReturn(timeout);
		}
	}
	public boolean waitForReturn(long timeout) throws PCDIException {
		synchronized (lock) {
			//no need to wait return back
			if (!isWaitForReturn() || command_finish)
				return true;
	
			//start waiting
			try {
				doWait(timeout);
			} catch (InterruptedException e) {
				throw new PCDIException(e);
			}
			try {
				return checkReturn();
			} finally {
				command_finish = true;
			}
		}
	}
	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	public boolean isFisinhed() {
		return command_finish;
	}
	protected boolean isCanncelled() {
		return cancelled;
	}
	protected boolean isFlush() {
		return flush;
	}
	public void doCancelWaiting() {
		synchronized (lock) {
			command_finish = true;
			cancelled = true;
			setReturn(RETURN_CANCEL);
		}
	}
	public void doFlush() {
		synchronized (lock) {
			if (getReturn() == null) {
				command_finish = true;
				flush = true;
				setReturn(RETURN_FLUSH);
			}
		}
	}
	protected void setCheckTasks() {
		synchronized (lock) {
			if (check_tasks == null) {
				check_tasks = tasks.copy();
			}
		}
	}
	public void setResult(Object result) {
		synchronized (lock) {
			this.result = result;
		}
	}
	public void setReturn(Object result) {
		synchronized (lock) {
			setCheckTasks();
			setResult(result);
			releaseLock();
		}
	}
	public void setReturn(BitList return_tasks, Object result) {
		synchronized (lock) {
			setCheckTasks();
			if (return_tasks != null) {
				//check whether return tasks is same as command tasks
				check_tasks.andNot(return_tasks);
				if (check_tasks.isEmpty()) {
					setResult(result);
					releaseLock();
				}
				else {
					//if return tasks is not equal to command tasks, wait again
					lockAgain();
				}
			}
			else {
				setReturn(RETURN_INCOMPLETE);
				/*
				setWaitAgain(false);
				this.result = result;
				lock.notifyAll();
				*/
			}
		}
	}
	public int compareTo(IDebugCommand obj) {
		if (!getCommandName().equals(obj.getCommandName()))
			return -1;
		
		BitList cpyTasks = getTasks().copy();
		cpyTasks.andNot(obj.getTasks());
		return cpyTasks.isEmpty()?0:-1;
	}
	public Object getResultValue() throws PCDIException {
		if (getReturn() == null) {
			waitForReturn();
		}
		if (getReturn() instanceof PCDIException) {
			throw (PCDIException)getReturn();
		}
		return getReturn();
	} 
	protected void waitSuspendExecCommand(IAbstractDebugger debugger) throws PCDIException {
		synchronized (lock) {
			if (!debugger.isSuspended(tasks.copy())) {
				try {
					wait(2000);
					//wait again if tasks are not suspended
					if (!debugger.isSuspended(tasks.copy())) {
						wait(2000);
					}
				} catch (InterruptedException e) {
					throw new PCDIException(e);
				}
			}
			//if tasks are still not suspended, then cancel it
			if (command_finish || !debugger.isSuspended(tasks.copy())) {
				PDebugUtils.println("************************************ WAIT SUSPEND FAILURE");			
				doFlush();
			}
			else {
				exec(debugger);
			}
		}
	}
	protected void checkBeforeExecCommand(IAbstractDebugger debugger) throws PCDIException {
		if (debugger.isSuspended(tasks.copy())) {
			exec(debugger);
		}
		else {
			doFlush();
		}		
	}
	protected BitList suspendRunningTasks(IAbstractDebugger debugger) throws PCDIException {
		BitList tmpTasks = tasks.copy();
		debugger.filterSuspendTasks(tmpTasks);
		if (!tmpTasks.isEmpty()) {
			IDebugCommand cmd = new HaltCommand(tmpTasks, true);
			debugger.postInterruptCommand(cmd);
			try {
				cmd.execCommand(debugger);
			} finally {
				debugger.postInterruptCommand(null);
			}
		}
		return tmpTasks;
	}
	protected void resumeSuspendedTasks(IAbstractDebugger debugger, BitList suspendedTasks) throws PCDIException {
		debugger.go(suspendedTasks);
	}
	public synchronized void execCommand(IAbstractDebugger debugger) throws PCDIException {
		preExecCommand(debugger);
		waitForReturn();
	}
	protected void presetTimeout(int timeout) {
		int size = tasks.cardinality();
		setTimeout(timeout * (size>0?size:1));
	}
	protected abstract void preExecCommand(IAbstractDebugger debugger) throws PCDIException;
	protected abstract void exec(IAbstractDebugger debugger) throws PCDIException;
}
