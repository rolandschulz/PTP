/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
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
			args[i] = new SimVariable("arg" + i, "rType", "" + 1000 + i);
		}
		
		local = new SimVariable[2];
		for (int i = 0; i < local.length; i++) {
			local[i] = new SimVariable("local" + i, "rType", "" + 2000 + i);
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
