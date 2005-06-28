package org.eclipse.ptp.debug.external.simulator;

import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.DebugConfig;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.event.EBreakpointHit;
import org.eclipse.ptp.debug.external.model.MProcess;

public class DebugSimulator extends AbstractDebugger {

	private Process debugProcess = null;
	private DebugSession debugSession = null;
	
	final int RUNNING = 10;
	final int SUSPENDED = 11;
	
	int state = 0;
	boolean finished = false;
	Thread dThread = null;
	
	public DebugSimulator(DebugConfig dConf) {
		super(dConf);
	}

	public void initDebugger(DebugSession dS) {
		super.initDebugger();
		//debugProcess = Runtime.getRuntime().exec("/bin/bash /tmp/process.sh debugger 6");
		debugProcess = new SimProcess("Debugger");
		debugSession = dS;
		
		state = SUSPENDED;
		
		dThread = new Thread() {
			public void run() {
				while (!finished) {
					try {
						if (state == RUNNING) {
							state = SUSPENDED;
							fireEvent(new EBreakpointHit(debugSession));
						}
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		
		dThread.start();
	}
	
	public void destroyDebugger() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.destroyDebugger()");
		finished = true;
	}

	public void load(String prg) {
		load(prg, 1);
	}
	
	public void load(String prg, int numProcs) {
		super.load(prg, numProcs);
		MProcess.resetGlobalCounter();
		for (int i = 0; i < numProcs; i++) {
			MProcess proc = new MProcess();
			//Process p = Runtime.getRuntime().exec("/bin/bash /tmp/process.sh proc" + i + " 4");
			Process p = new SimProcess("proc" + i);
			proc.setDebugInfo(p); /* We store the process in the "debug info" */
			allSet.addProcess(proc);
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

	public Process getProcess(int num) {
		return (Process) (allSet.getProcess(num)).getDebugInfo();
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
			System.out.println("resuming: " + allSet.getProcess(i).getName());
		}
		state = RUNNING;
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
			System.out.println("suspending: " + allSet.getProcess(i).getName());
		}
		state = SUSPENDED;
	}

}
