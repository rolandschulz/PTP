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
package org.eclipse.ptp.debug.external.simulator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SimProcess extends Process {
	
	boolean finished;
	
	SimThread[] threads;
	
	InputStream err;
	InputStream in;
	OutputStream out;
	
	
	int id;
	String name;
	SimQueue commands;
	
	Thread procThread;
	
	DebugSimulator dSim;
	
	public SimProcess(String nm, int pId, int numThreads, SimQueue cmds, DebugSimulator debugger) {
		super();
		dSim = debugger;
		
		finished = false;
		id = pId;
		name = nm;
		commands = cmds;
		
		threads = new SimThread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new SimThread(i, pId, dSim);
		}
		
		err = null;
		in = new SimInputStream();
		out = new SimOutputStream();
		
		procThread = new Thread() {
			public void run() {
				while (true) {
					try {
						ArrayList command = (ArrayList) commands.removeItem();
						
						String destination = (String) command.get(0);
						String cmd = (String) command.get(1);
						String arg = (String) command.get(2);
						
						if (!destination.equals("-1")) {
							threads[Integer.parseInt(destination)].runCommand((SimInputStream) in, cmd, arg);
						} else {
							if (cmd.equals("sleep")) {
								Thread.sleep(Integer.parseInt(arg));
							} else if (cmd.equals("exitProcess")) {
								break;
							}
						}
						Thread.sleep(3000);
					} catch (InterruptedException e) {
					}
				}
				finished = true;
				((SimInputStream) in).destroy();
			}
		};
		procThread.start();
	}
	
	public int exitValue() {
		if (finished)
			return 0;
		else
			throw new IllegalThreadStateException();
	}
	
	public int waitFor() throws InterruptedException {
		try {
			procThread.join();
		} catch (InterruptedException e) {
		}
		return 0;
	}

	public void destroy() {
		finished = true;
		((SimInputStream) in).destroy();
	}
	
	public InputStream getErrorStream() {
		return err;
	}

	public InputStream getInputStream() {
		return in;
	}

	public OutputStream getOutputStream() {
		return out;
	}

	public SimThread getThread(int tId) {
		return threads[tId];
	}
	
	public SimThread[] getThreads() {
		return threads;
	}
	
	public int getThreadCount() {
		return threads.length;
	}
}
