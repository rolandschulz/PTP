package org.eclipse.ptp.debug.external.simulator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

import org.eclipse.ptp.debug.external.DebugSession;

public class SimProcess extends Process {
	
	boolean finished;
	
	SimThread[] threads;
	
	InputStream err;
	InputStream in;
	OutputStream out;
	
	String name;
	SimQueue commands;
	
	Thread procThread;
	
	DebugSimulator dSim;
	DebugSession dSes;
	
	public SimProcess(String nm, int numThreads, SimQueue cmds, DebugSimulator debugger, DebugSession dSession) {
		super();
		dSim = debugger;
		dSes = dSession;
		
		finished = false;
		name = nm;
		commands = cmds;
		
		threads = new SimThread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new SimThread(i, name, dSim, dSes);
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
