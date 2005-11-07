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

package org.eclipse.ptp.debug.external.debugger;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.proxy.ProxyDebugClient;

public class DebuggerCommandQueue {
	private Queue			cmdQueue;
	private ProxyDebugClient	cmdProxy;
	private Thread			cmdThread;
	private IDebuggerCommand	cmdInProgress;
	private BitList			cmdProcs;
	private boolean			exitThread;
	
	public DebuggerCommandQueue(ProxyDebugClient proxy) {
		cmdProxy = proxy;
		cmdQueue = new Queue();
		cmdInProgress = null;
		exitThread = false;
		
		cmdThread = new Thread("Command Queue Thread") {
			public void run() {
				System.out.println("cmd thread starting...");
				while (!exitThread) {
					if (!waitForCommand())
						break;
					
					try {
						cmdInProgress = (IDebuggerCommand) cmdQueue.removeItem();
					} catch (InterruptedException e1) {
						break;
					}
					cmdProcs = cmdInProgress.getProcs().copy();
					
					try {
						cmdInProgress.execute(cmdProxy);
					} catch (PCDIException e) {
						// TODO fireEvent(exception...);
						e.printStackTrace();
					}
				}
				System.out.println("cmd thread exiting...");
			}
		};
		cmdThread.start();
	}
	
	private synchronized boolean waitForCommand() {
		try {
			while (cmdInProgress != null || cmdQueue.isEmpty()) {
				wait();
			}
		} catch (InterruptedException e) {
			return false;
		}
		
		return true;
	}
	
	public synchronized void addCommand(IDebuggerCommand cmd) {
		cmdQueue.addItem(cmd);
		notifyAll();
	}
	
	public synchronized void updateCompleted(BitList procs) {
		if (cmdInProgress != null) {
			cmdProcs.andNot(procs);
			if (cmdProcs.isEmpty()) {
				cmdInProgress = null;
				notifyAll();
			}
		}
	}
	
	public void flushCommands() {
		cmdQueue.clearItems();
	}
	
	public void shutdown() {
		exitThread = true;
	}
}
