package org.eclipse.ptp.debug.external.simulator;

public class SimStackFrame {

	int level;
	String addr;
	String func = ""; //$NON-NLS-1$
	String file = ""; //$NON-NLS-1$
	int line;
	
	SimVariable[] args;
	SimVariable[] local;
	
	public SimStackFrame(int l, String iAddr, String iFunc, String iFile, int iLine) {
		level = l;
		addr = iAddr;
		func = iFunc;
		file = iFile;
		line = iLine;
		
		args = new SimVariable[2];
		for (int i = 0; i < args.length; i++) {
			args[i] = new SimVariable("rName", "rType");
		}
		
		local = new SimVariable[2];
		for (int i = 0; i < local.length; i++) {
			local[i] = new SimVariable("rName", "rType");
		}

	}
	
	public int getLevel() {
		return level;
	}
	
	public String getFile() {
		return file;
	}

	public String getFunction() {
		return func;
	}

	public int getLine() {
		return line;
	}
	
	public String getAddress() {
		return addr;
	}
	
	public SimVariable[] getArgs() {
		return args;
	}
	
	public SimVariable[] getLocalVars() {
		return local;
	}

}
