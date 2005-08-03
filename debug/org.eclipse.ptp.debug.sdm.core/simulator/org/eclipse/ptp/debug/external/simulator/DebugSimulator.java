 package org.eclipse.ptp.debug.external.simulator;

import java.util.ArrayList;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.event.EBreakpointHit;
import org.eclipse.ptp.debug.external.model.MProcess;

public class DebugSimulator extends AbstractDebugger {

	final int RUNNING = 10;
	final int SUSPENDED = 11;
	
	int state = 0;
	boolean finished = false;
	
	private Process debuggerProcess = null;
	
	SimQueue debuggerCommands = null;
	SimQueue[] procCommands = null;
	
	private void initializeSimulatedProcessesCode(SimQueue dQ, SimQueue[] procs) {
		ArrayList cmd, cmd2;
		
		cmd = new ArrayList();
		cmd.add(0, "0");
		cmd.add(1, "print");
		cmd.add(2, "DebuggerOutput");
		
		for (int i = 0; i < 30; i++) {
			dQ.addItem(cmd);
		}
		
		cmd = new ArrayList();
		cmd.add(0, "0");
		cmd.add(1, "print");
		cmd.add(2, "ProcessOutput");
		
		cmd2 = new ArrayList();
		cmd2.add(0, "0");
		cmd2.add(1, "break");
		cmd2.add(2, "5");

		for (int j = 0; j < procs.length; j++) {
			procs[j].addItem(cmd2);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
			procs[j].addItem(cmd);
		}
	}
	
	protected void startDebugger(IPJob job) {
		int numSlaves = job.getProcesses().length;
		state = SUSPENDED;
		
		debuggerCommands = new SimQueue();
		procCommands = new SimQueue[numSlaves];
		for (int i = 0; i < numSlaves; i++) {
			procCommands[i] = new SimQueue();
		}
		
		initializeSimulatedProcessesCode(debuggerCommands, procCommands);
		
		debuggerProcess = new SimProcess("Debugger", 1, debuggerCommands, this, debugSession);
		
		MProcess.resetGlobalCounter();
		IPProcess[] procs = job.getProcesses();
		for (int i = 0; i < procs.length; i++) {
			MProcess proc = new MProcess();
			Process p = new SimProcess("proc" + i, 1, procCommands[i], this, debugSession);
			proc.setDebugInfo(p); /* We store the process in the "debug info" */
			proc.setPProcess(procs[i]);
			allSet.addProcess(proc);
		}

	}
	
	protected void stopDebugger() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.stopDebugger()");
		finished = true;
	}

	public Process getSessionProcess() {
		return debuggerProcess;
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
	
	public void go() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.go()");
		state = RUNNING;
	}

	public void kill() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.kill()");
		int listSize = allSet.getSize();
		//long start = System.currentTimeMillis();
		
		for (int i = 0; i < listSize; i++) {
			//System.out.println("terminating: " + allSet.getProcess(i).getName());
			((Process) allSet.getProcess(i).getDebugInfo()).destroy();
		}

		//long end = System.currentTimeMillis();
		
		//double totalseconds = (double)(end - start) / (double)1000;
		//System.out.println("DebugSimulator.terminate() takes " + totalseconds + " seconds");
	}

	public void halt() {
		// Auto-generated method stub
		System.out.println("DebugSimulator.halt()");
		state = SUSPENDED;
	}
	
	public void load(String prg) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.load()");
		
	}

	public void load(String prg, int numProcs) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.load()");
		
	}

	public void run(String[] args) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.run()");
		
	}

	public void run() {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.run()");
		
	}

	public void detach() {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.detach()");
		
	}

	public void step() {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.step()");
		
	}

	public void step(int count) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.step()");
		
	}

	public void stepOver() {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.stepOver()");
		
	}

	public void stepOver(int count) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.stepOver()");
		
	}

	public void stepFinish() {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.stepFinish()");
		
	}

	public void breakpoint(String loc) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.breakpoint()");
		
	}

	public void breakpoint(String loc, int count) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.breakpoint()");
		
	}

	public void breakpoint(String loc, String cond) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.breakpoint()");
		
	}

	public void watchpoint(String var) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.watchpoint()");
		
	}

	public void delete(int[] ids) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.delete()");
		
	}

	public void delete(String type) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.delete()");
		
	}

	public void disable(int[] ids) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.disable()");
		
	}

	public void disable(String type) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.disable()");
		
	}

	public void enable(int[] ids) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.enable()");
		
	}

	public void enable(String type) {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.enable()");
		
	}

	public void restart() {
		// Auto-generated method stub
		System.out.println("DebugSimulator2.restart()");
		
	}
}
