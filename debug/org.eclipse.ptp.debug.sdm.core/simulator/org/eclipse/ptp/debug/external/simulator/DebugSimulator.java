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
	Thread dThread = null;
	
	ArrayList pCommands = null;
	Thread processesThread = null;
	
	private Process debuggerProcess = null;
	Thread debuggerThread = null;
	Queue debuggerCommands = null;
	
	protected void startDebugger(IPJob job) {
		state = SUSPENDED;
		
		debuggerCommands = new Queue();
		debuggerProcess = new SimProcess("Debugger", 1, debuggerCommands);
		debuggerThread = new Thread() {
			public void run() {
				while (!finished) {
					try {
						ArrayList command = new ArrayList();
						command.add(0, "print");
						command.add(1, "1");
						command.add(2, "DebuggerOutput");
						debuggerCommands.addItem(command);
						System.out.println("---------------- DebuggerOutput");
						Thread.sleep(5000);
					} catch (InterruptedException e) {
					}
				}
			}
		};

		processesThread = new Thread() {
			public void run() {
				while (!finished) {
					try {
						ArrayList command = new ArrayList();
						command.add(0, "print");
						command.add(1, "1");
						command.add(2, "ProcessOutput");
						for (int i = 0; i < pCommands.size(); i++) {
							((Queue) pCommands.get(i)).addItem(command);
						}
						System.out.println("---------------- ProcessOutput");
						Thread.sleep(3000);
					} catch (InterruptedException e) {
					}
				}
			}
		};
		
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
		
		
		MProcess.resetGlobalCounter();
		IPProcess[] procs = job.getProcesses();
		pCommands = new ArrayList();
		for (int i = 0; i < procs.length; i++) {
			MProcess proc = new MProcess();
			Queue q = new Queue();
			Process p = new SimProcess("proc" + i, 1, q);
			proc.setDebugInfo(p); /* We store the process in the "debug info" */
			proc.setPProcess(procs[i]);
			pCommands.add(i, q);
			allSet.addProcess(proc);
		}
		
		debuggerThread.start();
		processesThread.start();

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
