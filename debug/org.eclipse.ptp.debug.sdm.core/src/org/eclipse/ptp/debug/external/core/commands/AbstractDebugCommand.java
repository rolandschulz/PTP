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
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.cdi.PCDIException;


/**
 * @author Clement chu
 * 
 */
public abstract class AbstractDebugCommand implements IDebugCommand {
	protected final Object lock = new Object(); 
	
	protected BitList tasks = null;
	protected Object result = null;
	protected boolean waitForReturn = false;
	protected boolean interrupt = false;
	private boolean isFlush = false;
	protected int timeout = 10000;
	protected boolean waitInQueue = false;
	
	public AbstractDebugCommand(BitList tasks) {
		this(tasks, false, false, false);
	}
	public AbstractDebugCommand(BitList tasks, boolean interrupt, boolean waitForReturn) {
		this(tasks, interrupt, waitForReturn, false);
	}
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
	public Object getReturn() {
		return result;
	}
	public void setReturn(BitList tasks, Object result) {
		synchronized (lock) {
			this.result = result;
			if (tasks != null) {
				this.tasks = tasks;
			}
			lock.notifyAll();
		}
	}
	/**
	 * @return true - normal, false - flush
	 */
	public boolean waitForReturn() throws PCDIException {
		if (!isWaitForReturn())
			return true;

		synchronized (lock) {
			try {
				if (isFlush)
					return false;
				
				if (getReturn() == null) {
					lock.wait(timeout);
					if (getReturn() == null) {
						if (isFlush)
							return false;
						
						throw new PCDIException("Time out - Command: " + getName());
					}
					else {
						if (getReturn() instanceof PCDIException) {
							throw (PCDIException)getReturn();
						}
					}
				}
			} catch (InterruptedException e) {
				throw new PCDIException(e);
			}
			return true;
		}
	}
	
	public void cancelWaiting() {
		synchronized (lock) {
			waitForReturn = false;
			lock.notifyAll();
		}
	}
	
	public void flush() {
		synchronized (lock) {
			isFlush = true;
			lock.notifyAll();
		}
	}
	protected void setTimeout(int timeout) {
		this.timeout = timeout;
	}
	
	/**
	 * 
	 * @param obj compared object
	 * @return 0 means equals, otherwise not equals
	 */
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
}
