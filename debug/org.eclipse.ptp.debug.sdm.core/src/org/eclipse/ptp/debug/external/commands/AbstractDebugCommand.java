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
package org.eclipse.ptp.debug.external.commands;

import org.eclipse.ptp.core.util.BitList;

/**
 * @author Clement chu
 * 
 */
public abstract class AbstractDebugCommand implements IDebugCommand {
	protected final long WAIT_COMMAND_RETURN_TIME = 30000; 
	protected final Object lock = new Object(); 
	
	protected BitList tasks = null;
	protected Object result = null;
	protected boolean waitForReturn = false;
	protected boolean interrupt = false;
	
	public AbstractDebugCommand(BitList tasks) {
		this(tasks, false, false);
	}
	public AbstractDebugCommand(BitList tasks, boolean interrupt, boolean waitForReturn) {
		this.tasks = tasks;
		this.interrupt = interrupt;
		this.waitForReturn = waitForReturn;
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
	public void setReturn(Object result) {
		synchronized (lock) {
			this.result = result;
			lock.notifyAll();
		}
	}
	public boolean waitForReturn() {
		if (!isWaitForReturn())
			return true;

		synchronized (lock) {
			try {
				if (getReturn() == null) {
					lock.wait(WAIT_COMMAND_RETURN_TIME);
					if (getReturn() == null) {
						return false;
					}
				}
			} catch (InterruptedException e) {
				return false;
			}
			return true;
		}
	}
}
