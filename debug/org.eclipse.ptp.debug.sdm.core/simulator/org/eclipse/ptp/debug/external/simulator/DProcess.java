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

public class DProcess extends Process {
	
	boolean finished;
	
	InputStream err;
	InputStream in;
	OutputStream out;
	
	int id;
	String name;
	DQueue commands;
	
	Thread procThread;
	
	DebugSimulator dD;
	
	public DProcess(String nm, int pId, int numThreads, DQueue cmds, DebugSimulator debugger) {
		super();
		dD = debugger;
		
		finished = false;
		id = pId;
		name = nm;
		commands = cmds;
		
		err = null;
		in = new DInputStream();
		out = new DOutputStream();
		
		procThread = new Thread() {
			public void run() {
				while (true) {
					try {
						String output = (String) commands.removeItem();
						
						((DInputStream) in).printString(output);
						
						if (output.equals("exit")) {
							break;
						}
						
						//Thread.sleep(3000);
					} catch (InterruptedException e) {
					}
				}
				finished = true;
				((DInputStream) in).destroy();
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
		((DInputStream) in).destroy();
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

}
