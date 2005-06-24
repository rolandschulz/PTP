package org.eclipse.ptp.debug.external.simulator;

import java.io.IOException;

import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.DebugConfig;
import org.eclipse.ptp.debug.external.model.MProcess;

public class DebugSimulator extends AbstractDebugger {

	/* NOTE: copy the file process.sh in this folder to /tmp */
	
	private Process debugProcess = null;
	
	public DebugSimulator(DebugConfig dConf) {
		super(dConf);
	}

	public void initDebugger() {
		super.initDebugger();
		try {
			debugProcess = Runtime.getRuntime().exec("/bin/bash /tmp/process.sh debugger 2000");
		} catch (IOException e) {
		}
	}
	
	public void destroyDebugger() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.destroyDebugger()");
	}

	public void load(String prg) {
		load(prg, 1);
	}
	
	public void load(String prg, int numProcs) {
		super.load(prg, numProcs);
		MProcess.resetGlobalCounter();
		try {
			for (int i = 0; i < numProcs; i++) {
				MProcess proc = new MProcess();
				Process p = Runtime.getRuntime().exec("/bin/bash /tmp/process.sh proc" + i + " 1600");
				proc.setDebugInfo(p); /* We store the process in the "debug info" */
				allSet.addProcess(proc);
			}
		} catch (IOException e) {
		}
	}

	public void run() {
		this.run(null);
	}
	
	public void run(String[] args) {
		super.run(args);
	}

	public Process getSessionProcess() {
		return debugProcess;
	}

	public void breakpoint(String loc) {
		super.breakpoint(loc);
		System.out.println("DebugSimulator.breakpoint()");
	}
	
	public Process[] getProcesses() {
		int listSize = allSet.getSize();
		Process[] procs = new Process[listSize];
		for (int i = 0; i < listSize; i++) {
			procs[i] = (Process) (allSet.getProcess(i)).getDebugInfo();
		}
		return procs;
	}

	public void disconnect() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.disconnect()");
		int listSize = allSet.getSize();
		for (int i = 0; i < listSize; i++) {
			System.out.println("disconnecting: " + allSet.getProcess(i).getName());
		}

	}

	public void resume() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.resume()");
		int listSize = allSet.getSize();
		for (int i = 0; i < listSize; i++) {
			//System.out.println("resuming: " + allSet.getProcess(i).getName());
		}

	}

	public void restart() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.restart()");
		int listSize = allSet.getSize();
		for (int i = 0; i < listSize; i++) {
			System.out.println("restarting: " + allSet.getProcess(i).getName());
		}

	}

	public void terminate() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.terminate()");
		int listSize = allSet.getSize();
		
		long start = System.currentTimeMillis();
		
		for (int i = 0; i < listSize; i++) {
			//System.out.println("terminating: " + allSet.getProcess(i).getName());
			((Process) allSet.getProcess(i).getDebugInfo()).destroy();
		}

		long end = System.currentTimeMillis();
		
		double totalseconds = (double)(end - start) / (double)1000;
		System.out.println("DebugSimulator.terminate() takes " + totalseconds + " seconds");

	}

	public void suspend() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.suspend()");
		int listSize = allSet.getSize();
		for (int i = 0; i < listSize; i++) {
			//System.out.println("suspending: " + allSet.getProcess(i).getName());
		}

	}

}
