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
package org.eclipse.ptp.debug.external;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.commands.IDebugCommand;

/**
 * @author Clement chu
 * 
 */
public class DebugCommandQueue extends Thread {
	private List queue = null;
	private boolean isTerminated = false;
	private IDebugCommand currentCommand = null;
	private IAbstractDebugger debugger = null;
	
	public DebugCommandQueue(IAbstractDebugger debugger) {
		this.debugger = debugger;
		queue = Collections.synchronizedList(new LinkedList());
	}
	public void setTerminated() {
		isTerminated = true;
	}
	
	public void run()  {
		while (!isTerminated) {
			if (!waitForCommand()) {
				break;
			}
			currentCommand = getCommand();
			try {
				currentCommand.execCommand(debugger);
				System.out.println("***** CURRENT COMMAND: " + currentCommand);
				if (!currentCommand.waitForReturn()) {
					System.out.println("************ ERROR in DebugCommandQueue -- wait for return, cmd: " + currentCommand);
				}
			} catch (PCDIException e) {
				System.out.println("************ ERROR in DebugCommandQueue -- execCommand, cmd: " + currentCommand + ", err: " + e.getMessage());
			} finally {
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

	public IDebugCommand getCommand() {
		synchronized (queue) {
			return (IDebugCommand)queue.remove(0);
		}
	}
	public void addCommand(IDebugCommand command) {
		synchronized (queue) {
			if (!queue.contains(command)) {
				if (command.canInterrupt() && currentCommand != null) {
					setCommandReturn(null);
					try {
						//To make sure all events fired via AsbtractDebugger, so wait 1 sec here
						queue.wait(500);
					} catch (InterruptedException e) {}
				}
				queue.add(command);
				queue.notifyAll();
			}
			else {
				System.out.println("************ ERROR in DebugCommandQueue -- duplicate, cmd: " + currentCommand);
			}
		}
	}
	public void flushCommands() {
		synchronized (queue) {
			IDebugCommand[] commands = (IDebugCommand[])queue.toArray(new IDebugCommand[0]);
			for (int i=commands.length-1; i>-1; i--) {
				commands[i].flush();
			}
			queue.clear();
		}
	}
	public void setCommandReturn(Object result) {
		synchronized (queue) {
			if (currentCommand != null) {
				if (result == null) {
					flushCommands();
					currentCommand.flush();
				}
				else {
					currentCommand.setReturn(result);
				}
			}
		}
	}
}
