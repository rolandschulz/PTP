package org.eclipse.ptp.debug.external.simulator;

import java.io.InputStream;
import java.io.OutputStream;

public class SimProcess extends Process {

	boolean finished;
	InputStream err;
	InputStream in;
	OutputStream out;
	String name;
	
	public SimProcess(String nm) {
		super();
		finished = false;
		name = nm;
		//err = new SimInputStream(name, 3, 10);
		err = null;
		in = new SimInputStream(name, 10, 6);
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
		// Auto-generated method stub
		System.out.println("SimProcess.getErrorStream()");
		return err;
	}

	public InputStream getInputStream() {
		// Auto-generated method stub
		System.out.println("SimProcess.getInputStream()");
		return in;
	}

	public OutputStream getOutputStream() {
		// Auto-generated method stub
		System.out.println("SimProcess.getOutputStream()");
		return out;
	}

}
