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
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;


/** This is an abstract class of debug command.  It contains all common actions and functions of all debug commands.
 * @author Clement chu
 * 
 */
public abstract class AbstractDebugCommand implements IDebugCommand {
	protected final Object lock = new Object(); 
	
	protected BitList tasks = null;
	protected Object result = null;
	protected boolean waitForReturn = false;
	protected boolean interrupt = false;
	private boolean flush = false;
	private boolean cancelled = false;
	protected long timeout = 10000;
	protected boolean waitInQueue = false;
	
	/** constructor
	 * @param tasks
	 */
	public AbstractDebugCommand(BitList tasks) {
		this(tasks, false, true);
	}
	/** constructor
	 * @param tasks the tasks for this command
	 * @param interrupt whether this command can interrupted or not
	 * @param waitForReturn whether this command should wait for return value
	 */
	public AbstractDebugCommand(BitList tasks, boolean interrupt, boolean waitForReturn) {
		this(tasks, interrupt, waitForReturn, true);
	}
	/** constructor
	 * @param tasks the tasks for this command
	 * @param interrupt whether this command can interrupted or not
	 * @param waitForReturn whether this command should wait for return value
	 * @param waitInQueue whether this command should be queuing or jump the queue (no need to wait to execuate)
	 */
	public AbstractDebugCommand(BitList tasks, boolean interrupt, boolean waitForReturn, boolean waitInQueue) {
		this.tasks = tasks;
		this.interrupt = interrupt;
		this.waitForReturn = waitForReturn;
		this.waitInQueue = waitInQueue;
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
		return waitForReturn;
	}
	protected Object getReturn() {
		return result;
	}
	//wait again for return back
	protected void doWait(long timeout) throws InterruptedException {
		synchronized (lock) {
			lock.wait(timeout);
		}
	}
	protected boolean checkReturn() throws PCDIException {
		Object result = getReturn();
		if (result == null) {
			throw new PCDIException("Time out - Command " + getName());
		}
		if (result.equals(RETURN_NOTHING)) {
			throw new PCDIException("Unknown error - Command " + getName());
		}
		if (result instanceof PCDIException) {
			if (((PCDIException)result).getErrorCode() == IPCDIErrorEvent.DBG_NORMAL) {
				return false;
			}
			throw (PCDIException)getReturn();
		}
		if (result.equals(RETURN_ERROR)) {
			throw new PCDIException("Tasks do not match with <" + getName() + "> command.");
		}
		if (result.equals(RETURN_CANCEL)) {
			throw new PCDIException("Cancelled - command " + getName());
		}
		if (result.equals(RETURN_FLUSH)) {
			return false;
		}		
		return true;
	}
	public boolean waitForReturn() throws PCDIException {
		return waitForReturn(timeout);
	}
	public boolean waitForReturn(long timeout) throws PCDIException {
		//no need to wait return back
		if (!isWaitForReturn())
			return true;

		//start waiting
		try {
			doWait(timeout);
		} catch (InterruptedException e) {
			throw new PCDIException(e);
		}
		return checkReturn();
	}
	protected void setTimeout(long timeout) {
		this.timeout = timeout;
	}
	protected boolean isCanncelled() {
		return cancelled;
	}
	protected boolean isFlush() {
		return flush;
	}
	public void doCancelWaiting() {
		setReturn(RETURN_CANCEL);
	}
	public void doFlush() {
		setReturn(RETURN_FLUSH);
	}
	public void setReturn(Object result) {
		synchronized (lock) {
			this.result = result;
			lock.notifyAll();
		}
	}
	public void setReturn(BitList tasks, Object result) {
		synchronized (lock) {
			/*
			if (this.tasks != null && tasks != null) {
				tasks.andNot(this.tasks);
			}
			this.result = (tasks==null || tasks.isEmpty())?RETURN_ERROR:result;
			*/
			this.result = result;
			lock.notifyAll();
		}
	}
	public int compareTo(Object obj) {
		if (obj instanceof IDebugCommand) {
			if (!getName().equals(((IDebugCommand) obj).getName()))
				return -1;
			
			BitList cpyTasks = getTasks().copy();
			cpyTasks.andNot(((IDebugCommand) obj).getTasks());
			return cpyTasks.isEmpty()?0:-1;
		}
		return -1;
	}
	
	public void execCommand(IAbstractDebugger debugger, long timeout) throws PCDIException {
		setTimeout(timeout);
		execCommand(debugger);
		waitForReturn();
	}
	public Object getResult() throws PCDIException {
		if (getReturn() == null) {
			waitForReturn();
		}
		return getReturn();
	}
	protected abstract void execCommand(IAbstractDebugger debugger) throws PCDIException;
}
