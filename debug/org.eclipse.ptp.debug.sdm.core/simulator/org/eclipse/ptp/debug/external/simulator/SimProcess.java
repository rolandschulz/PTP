package org.eclipse.ptp.debug.external.simulator;

import java.io.InputStream;
import java.io.OutputStream;

public class SimProcess extends Process {

	SimThread[] threads;
	
	boolean finished;
	InputStream err;
	InputStream in;
	OutputStream out;
	String name;
	
	public SimProcess(String nm, int numThreads, int numStackFrames) {
		super();
		finished = false;
		name = nm;
		
		threads = new SimThread[numThreads];
		for (int i = 0; i < numThreads; i++) {
			threads[i] = new SimThread(numStackFrames, i);
		}
		
		//err = new SimInputStream(name, 3, 10);
		err = null;
		in = new SimInputStream(name);
		out = new SimOutputStream();
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
			Thread.sleep(1000);
			finished = true;
		} catch (InterruptedException e) {
		}
		
		return 0;
	}

	public void destroy() {
		// Auto-generated method stub
		System.out.println("SimProcess.destroy()");
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
