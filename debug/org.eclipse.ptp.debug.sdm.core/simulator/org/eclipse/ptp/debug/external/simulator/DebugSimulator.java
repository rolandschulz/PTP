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
			debugProcess = Runtime.getRuntime().exec("/bin/bash /tmp/process.sh debugger 6");
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
				Process p = Runtime.getRuntime().exec("/bin/bash /tmp/process.sh proc" + i + " 2");
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

	public Process[] getProcesses() {
		int listSize = allSet.getSize();
		Process[] procs = new Process[listSize];
		for (int i = 0; i < listSize; i++) {
			procs[i] = (Process) (allSet.getProcess(i)).getDebugInfo();
		}
		return procs;
	}
}
