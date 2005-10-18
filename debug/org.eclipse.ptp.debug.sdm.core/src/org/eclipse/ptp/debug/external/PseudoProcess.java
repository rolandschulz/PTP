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

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IProcessEvent;
import org.eclipse.ptp.core.IProcessListener;
import org.eclipse.ptp.core.util.Queue;

public class PseudoProcess extends Process implements IProcessListener {
	
	boolean finished;
	final String EXIT = "exitPseudoProcess";
	
	InputStream err;
	InputStream in;
	OutputStream out;
	
	Queue outputs;
	Thread procThread;
	IPProcess process;
	
	public PseudoProcess(IPProcess proc) {
		super();
		process = proc;
		process.addProcessListener(this);
				
		finished = false;
		outputs = new Queue();
		
		err = null;
		in = new PseudoInputStream();
		out = new PseudoOutputStream();
		
		procThread = new Thread() {
			public void run() {
				while (true) {
					try {
						String output = (String) outputs.removeItem();
						if (output.equals(EXIT)) {
							break;
						}
						((PseudoInputStream) in).printString(output);
					} catch (InterruptedException e) {
					}
				}
				destroy();
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
		process.removerProcessListener(this);
		((PseudoInputStream) in).destroy();
		try {
			((PseudoInputStream) in).close();
			((PseudoOutputStream) out).close();
		} catch (IOException e) {
		}
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

	public void processEvent(IProcessEvent event) {
		switch (event.getType()) {
			case IProcessEvent.STATUS_CHANGE_TYPE:
				break;
			case IProcessEvent.STATUS_EXIT_TYPE:
				outputs.addItem(EXIT);
				break;
			case IProcessEvent.STATUS_SIGNALNAME_TYPE:
				break;
			case IProcessEvent.ADD_OUTPUT_TYPE:
				outputs.addItem(event.getInput());
				break;			
		}
	}

}
