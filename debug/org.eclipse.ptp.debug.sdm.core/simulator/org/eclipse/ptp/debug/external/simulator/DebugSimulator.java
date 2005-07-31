package org.eclipse.ptp.debug.external.simulator;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.event.EBreakpointHit;
import org.eclipse.ptp.debug.external.model.MProcess;

public class DebugSimulator extends AbstractDebugger {

	private Process debugProcess = null;
	
	final int RUNNING = 10;
	final int SUSPENDED = 11;
	
	int state = 0;
	boolean finished = false;
	Thread dThread = null;
	
	public DebugSimulator() {
		super();
	}

	protected void startDebugger() {
		//debugProcess = Runtime.getRuntime().exec("/bin/bash /tmp/process.sh debugger 6");
		debugProcess = new SimProcess("Debugger", 1, 1);
		
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
	
	protected void stopDebugger() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.destroyDebugger()");
		finished = true;
	}

	public void load(String prg) {
		load(prg, 1);
	}
	
	public void load(String prg, int numProcs) {
		MProcess.resetGlobalCounter();
		for (int i = 0; i < numProcs; i++) {
			MProcess proc = new MProcess();
			//Process p = Runtime.getRuntime().exec("/bin/bash /tmp/process.sh proc" + i + " 4");
			Process p = new SimProcess("proc" + i, 1, 1);
			proc.setDebugInfo(p); /* We store the process in the "debug info" */
			allSet.addProcess(proc);
		}
	}

	public void run() {
		this.run(null);
	}
	
	public void run(String[] args) {
	}

	public Process getSessionProcess() {
		return debugProcess;
	}

	public void breakpoint(String loc) {
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
	
	public Process getProcess() {
		return getProcess(0);
	}
	
	public void detach() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.disconnect()");
		int listSize = allSet.getSize();
		for (int i = 0; i < listSize; i++) {
			System.out.println("disconnecting: " + allSet.getProcess(i).getName());
		}

	}

	public void go() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.go()");
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

	public void kill() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.kill()");
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

	public void halt() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.halt()");
		int listSize = allSet.getSize();
		for (int i = 0; i < listSize; i++) {
			System.out.println("suspending: " + allSet.getProcess(i).getName());
		}
		state = SUSPENDED;
	}

	public void initPTP(IPJob job) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.initPTP()");
		
		MProcess.resetGlobalCounter();
		IPProcess[] procs = job.getProcesses();
		
		for (int i = 0; i < procs.length; i++) {
			MProcess proc = new MProcess();
			Process p = new SimProcess("proc" + i, 1, 1);
			proc.setDebugInfo(p); /* We store the process in the "debug info" */
			proc.setPProcess(procs[i]);
			allSet.addProcess(proc);
		}
	}

	public void step() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.step()");
		
	}

	public void step(int count) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.step()");
		
	}

	public void stepOver() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.stepOver()");
		
	}

	public void stepOver(int count) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.stepOver()");
		
	}

	public void stepFinish() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.stepFinish()");
		
	}

	public void breakpoint(String loc, int count) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.breakpoint()");
		
	}

	public void breakpoint(String loc, String cond) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.breakpoint()");
		
	}

	public void watchpoint(String var) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.watchpoint()");
		
	}

	public void delete(int[] ids) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.delete()");
		
	}

	public void delete(String type) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.delete()");
		
	}

	public void disable(int[] ids) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.disable()");
		
	}

	public void disable(String type) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.disable()");
		
	}

	public void enable(int[] ids) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.enable()");
		
	}

	public void enable(String type) {
		// Auto-generated method stub
		System.out.println("DebugSimulator.enable()");
		
	}

}
