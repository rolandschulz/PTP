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
package org.eclipse.ptp.debug.external.core;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public class DebugCommandQueue extends Thread {
	private List queue = null;
	private boolean isTerminated = false;
	private IDebugCommand currentCommand = null;
	private IAbstractDebugger debugger = null;
	private int command_timeout = 10000;
	
	public DebugCommandQueue(IAbstractDebugger debugger, int timeout) {
		this.debugger = debugger;
		this.command_timeout = timeout;
		queue = Collections.synchronizedList(new LinkedList());
	}
	public void setTerminated() {
		isTerminated = true;
		cleanup();
	}
		
	public void run() {
		while (!isTerminated) {
			if (!waitForCommand()) {
				break;
			}
			try {
				currentCommand = getCommand();
System.err.println("*** SEND COMMAND: " + currentCommand.getName() + ", tasks: " + debugger.showBitList(currentCommand.getTasks()));
				currentCommand.execCommand(debugger, command_timeout);
			} catch (PCDIException e) {
				debugger.handleErrorEvent(currentCommand.getTasks(), e.getMessage(), e.getErrorCode());
				currentCommand.doFlush();
			}
			finally {
				currentCommand = null;
			}
		}
	}
	private boolean waitForCommand() {
		synchronized (queue) {
			try {
				while (currentCommand != null || queue.isEmpty()) {
					queue.wait();
				}
			} catch (InterruptedException e) {
				return false;
			}
			return true;
		}
	}

	public IDebugCommand getCommand() throws PCDIException {
		synchronized (queue) {
			IDebugCommand command = (IDebugCommand)queue.remove(0);
			if (command == null)
				throw new PCDIException("No DebugCommand found");
			
			return command;
		}
	}
	public void addCommand(IDebugCommand command) {
		synchronized (queue) {
			if (!contains(command)) {
				if (command.isWaitInQueue()) {
					queue.add(command);
				} 
				else {
					//jump the queue
					queue.add(0, command);
				}
				if (command.canInterrupt() && currentCommand != null) {
					currentCommand.doFlush();
					try {
						//To make sure all events fired via AsbtractDebugger, so wait 0.5 sec here
						queue.wait(500);
					} catch (InterruptedException e) {}
				}
				queue.notifyAll();
			}
			else {
				//TODO how to deal with duplicate command
				System.err.println("************ ERROR in DebugCommandQueue -- duplicate, cmd: " + currentCommand);
			}
		}
	}
	private boolean contains(IDebugCommand command) {
		synchronized (queue) {
			//if (currentCommand != null && currentCommand.compareTo(command) == 0)
				//return true;
			int size = queue.size();
			if (size > 0) {
				return (((IDebugCommand)queue.get(size-1)).compareTo(command) == 0);
			}
			return false;
		}
	}
	public IDebugCommand[] getCommands() {
		return (IDebugCommand[])queue.toArray(new IDebugCommand[0]);
	}
	public void doFlushCommands() {
		synchronized (queue) {
			try {
				IDebugCommand[] commands = getCommands();
				for (int i=commands.length-1; i>-1; i--) {
					commands[i].doFlush();
				}
			} finally {
				queue.clear();
			}
		}
	}
	public void setCommandReturn(BitList tasks, Object result) {
		synchronized (queue) {
			if (currentCommand != null) {
				//if (result == null) {
					//doFlushCommands();
				//}
				currentCommand.setReturn(tasks, result);					
			}
		}
	}
	public void cleanup() {
		synchronized (queue) {
			//queue.clear();
			doFlushCommands();
		}
	}
}
