package org.eclipse.ptp.debug.external.simulator;

import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;

public class SimProcess extends Process {

	SimThread[] threads;
	
	boolean finished;
	InputStream err;
	InputStream in;
	OutputStream out;
	String name;
	Queue commands;
	
	Thread procThread;
	
	public SimProcess(String nm, int numThreads, Queue cmds) {
		super();
		finished = false;
		name = nm;
		commands = cmds;
		
		threads = new SimThread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new SimThread(i);
		}
		
		err = null;
		in = new SimInputStream();
		out = new SimOutputStream();
		
		procThread = new Thread() {
			public void run() {
				while (true) {
					try {
						ArrayList command = (ArrayList) commands.removeItem();
						String cmd = (String) command.get(0);
						if (cmd.equals("print")) {
							String thread = (String) command.get(1);
							String str = (String) command.get(2);
							((SimInputStream) in).printString(str + " from process " + name + " & thread " + thread);
						} else if (cmd.equals("sleep")) {
							String second = (String) command.get(1);
							Thread.sleep(Integer.parseInt(second));
						} else if (cmd.equals("exitProcess")) {
							break;
						}
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
		// Auto-generated method stub
		System.out.println("SimProcess.exitValue()");
		if (!finished)
			throw new IllegalThreadStateException();
		else
			return 0;
	}
	
	public int waitFor() throws InterruptedException {
		// Auto-generated method stub
		System.out.println("SimProcess.waitFor()");
		
		try {
			//Thread.sleep(10000);
			procThread.join();
			//finished = true;
			//((SimInputStream) in).destroy();
		} catch (InterruptedException e) {
		}
		
		return 0;
	}

	public void destroy() {
		// Auto-generated method stub
		System.out.println("SimProcess.destroy()");
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
