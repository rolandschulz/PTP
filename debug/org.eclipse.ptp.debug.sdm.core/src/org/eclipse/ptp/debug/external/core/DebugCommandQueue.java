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

import java.util.ArrayList;
import java.util.List;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IDebugCommand;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public class DebugCommandQueue extends Job {
	private List queue = new ArrayList();
	private boolean terminated = false;
	private boolean stopAddCommand = false;
	private IDebugCommand currentCommand = null;
	private IAbstractDebugger debugger = null;
	private IDebugCommand interruptCommand = null;
	
	public DebugCommandQueue(IAbstractDebugger debugger) {
        super("Debug Command Queue"); 
		this.debugger = debugger;
        setPriority(Job.SHORT);
        setSystem(true);
	}
	public synchronized boolean isTerminated() {
		return terminated;
	}
	public synchronized void setTerminated() {
		terminated = true;
		doFlushCommands(false);
	}
	public synchronized void setStopAddCommand(boolean stopAddCommand) {
		this.stopAddCommand = stopAddCommand;
	}
	public synchronized void setInterruptCommand(IDebugCommand interruptCommand) {
		this.interruptCommand = interruptCommand;
	}
	public synchronized IDebugCommand getInterruptCommand() {
		return interruptCommand;
	}
    protected IStatus run(IProgressMonitor monitor) {
        while (!queue.isEmpty()) {
			try {
				currentCommand = getCommand();
	        	if (terminated) {
	        		currentCommand.doFlush();
	        		break;
	        	}
	        	PDebugUtils.println("*** SEND COMMAND: " + currentCommand.getCommandName() + ", tasks: " + AbstractDebugger.showBitList(currentCommand.getTasks()));
				currentCommand.execCommand(debugger);
			} catch (PCDIException e) {
				debugger.handleErrorEvent(currentCommand.getTasks(), e.getMessage(), e.getErrorCode());
				currentCommand.doFlush();
			}
			finally {
				currentCommand = null;
			}
        }
        return Status.OK_STATUS;
    }
	/*
	private boolean waitForCommand() {
		synchronized (queue) {
			try {
				if (currentCommand != null || queue.isEmpty()) {
					queue.wait();
				}
			} catch (InterruptedException e) {
				e.printStackTrace();
				return false;
			}
			return true;
		}
	}
	*/
	private IDebugCommand getCommand() throws PCDIException {
        synchronized (queue) {
            if (!queue.isEmpty()) {
            	return (IDebugCommand) queue.remove(0);
            }
        }
		throw new PCDIException("No DebugCommand found");
	}
	public void addCommand(IDebugCommand command) {
		synchronized (queue) {
			if (command == null) 
				return;
			
			if (!stopAddCommand && !terminated && !contains(command)) {
				if (currentCommand != null && currentCommand.canInterrupt()) {
					if (!command.canInterrupt()) {
						command.doFlush();
						return;
					}
				}				
				
				if (command.isWaitInQueue()) {
					queue.add(command);
					//addCommandByPriority(command);
				} 
				else {
					PDebugUtils.println("************ DebugCommandQueue -- Add cmd JUMP: " + command.getCommandName());
					//jump the queue
					queue.add(0, command);
				}
				/*
				if (command.canInterrupt() && currentCommand != null) {
					//only do flush if the current command cannot interrupt
					if (!currentCommand.canInterrupt()) {
						currentCommand.doFlush();
					}
				}
				*/
				schedule();
			}
			else {
				command.doFlush();
				if (stopAddCommand || terminated) {
					PDebugUtils.println("************ ERROR in DebugCommandQueue -- debugger is terminated or stoped to add command, cmd: " + command.getCommandName());
				}
				else {
					//TODO how to deal with duplicate command
					PDebugUtils.println("************ ERROR in DebugCommandQueue -- duplicate, cmd: " + command.getCommandName());
				}
			}
		}
	}
	private void addCommandByPriority(IDebugCommand command) {
		synchronized (queue) {
			if (command.getPriority() > IDebugCommand.PRIORITY_L) {
				for (int i=0; i<queue.size(); i++) {
					if (((IDebugCommand)queue.get(i)).getPriority() < command.getPriority()) {
						PDebugUtils.println("************ DebugCommandQueue -- add cmd INSERT: " + command.getCommandName());
						queue.add(i, command);
						return;
					}
				}
			}
			PDebugUtils.println("************ DebugCommandQueue -- add cmd LAST: " + command.getCommandName());
			queue.add(command);
		}
	}
	private boolean contains(IDebugCommand command) {
		synchronized (queue) {
			//if (currentCommand != null && currentCommand.compareTo(command) == 0)
				//return true;
			int size = queue.size();
			if (size > 0) {
				//return (((IDebugCommand)queue.get(size-1)).compareTo(command) == 0);
				return (((IDebugCommand)queue.get(size-1)).equals(command));
			}
			return false;
		}
	}
	public IDebugCommand[] getCommands() {
		return (IDebugCommand[])queue.toArray(new IDebugCommand[0]);
	}
	public void setCommandReturn(BitList tasks, Object result) {
		synchronized (queue) {
			if (currentCommand != null) {
				PDebugUtils.println("*** SET COMMAND RETURN: " + currentCommand.getCommandName() + ", result: " + result + ", tasks: " + AbstractDebugger.showBitList(tasks));
				currentCommand.setReturn(tasks, result);		
			}
		}
	}
	public IDebugCommand getCurrentCommand() {
		return currentCommand;
	}
	private void doFlushCommands(boolean removeInterrupt) {
		synchronized (queue) {
			IDebugCommand[] commands = getCommands();
			for (int i=commands.length-1; i>-1; i--) {
				if (removeInterrupt || !commands[i].canInterrupt()) {
					removeCommand(commands[i]);
				}
			}
		}
	}
	private void removeCommand(IDebugCommand command) {
		synchronized (queue) {
			command.doFlush();
			queue.remove(command);
		}
	}
	public void cleanup(boolean removeInterrupt) {
		synchronized (queue) {
			doFlushCommands(removeInterrupt);
			if (currentCommand != null) {
				currentCommand.doFlush();
				currentCommand = null;
			}
		}
	}
}
