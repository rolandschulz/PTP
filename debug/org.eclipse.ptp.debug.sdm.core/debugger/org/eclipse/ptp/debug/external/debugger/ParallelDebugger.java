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

package org.eclipse.ptp.debug.external.debugger;

import java.util.ArrayList;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.external.AbstractDebugger;
import org.eclipse.ptp.debug.external.model.MProcess;

public class ParallelDebugger extends AbstractDebugger throws DebugException {

	protected void startDebugger(IPJob job) {
	}
	
	protected void stopDebugger() {
	}
	
	public void run(String[] args) {
	}

	public void go(ProcessSet p) {
	}

	public void kill(ProcessSet p) {
	}

	public void halt(ProcessSet p) {
	}
	
	public void stepInto(ProcessSet p, int count) {
	}

	public void stepOver(ProcessSet p, int count) {
	}

	public void stepFinish(ProcessSet p, int count) {
	}

	public BreakPoint setLineBreakpoint(ProcessSet p, String file, int line) {
	}

	public BreakPoint setFunctionBreakpoint(ProcessSet p, String file, String func) {
	}

	public void deleteBreakpoint(BreakPoint bp) {
	}
	
	public StackFrame[] listStackFrames(ProcessSet p) {
	}
	
	public StackFrame moveStackFrame(ProcessSet p, int count, boolean down) {
		
	}
	
	public Expression evaluateExpression(ProcessSet p, String expr) {
		
	}
	
	public Variable[] listVariables(ProcessSet p, StackFrame f) {
		
	}
	
	public Variable[] listGlobalVariables(ProcessSet p) {
		
	}
}
