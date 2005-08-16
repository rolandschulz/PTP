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
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIExpression;
import org.eclipse.cdt.debug.core.cdi.CDIException;

public class ParallelDebugger extends AbstractDebugger {

	protected void startDebugger(IPJob job) {
	}
	
	protected void stopDebugger() {
	}
	
	public void go(IPCDIDebugProcessSet procs) throws CDIException {
	}

	public void kill(IPCDIDebugProcessSet procs) throws CDIException {
	}

	public void halt(IPCDIDebugProcessSet procs) throws CDIException {
	}
	
	public void stepInto(IPCDIDebugProcessSet procs, int count) throws CDIException {
	}

	public void stepOver(IPCDIDebugProcessSet procs, int count) throws CDIException {
	}

	public void stepFinish(IPCDIDebugProcessSet procs, int count) throws CDIException {
	}

	public void setLineBreakpoint(IPCDIDebugProcessSet procs, ICDIBreakpoint bpt) throws CDIException {
	}

	public void setFunctionBreakpoint(IPCDIDebugProcessSet procs, ICDIBreakpoint bpt) throws CDIException {
	}

	public void deleteBreakpoints(ICDIBreakpoint[] bp) throws CDIException {
	}
	
	public ICDIStackFrame[] listStackFrames(IPCDIDebugProcessSet procs) throws CDIException {
		return null;
	}
	
	public void setCurrentStackFrame(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws CDIException {
		
	}
	
	public ICDIExpression evaluateExpression(IPCDIDebugProcessSet procs, String expr) throws CDIException {
		return null;
	}
	
	public ICDIVariable[] listVariables(IPCDIDebugProcessSet procs, ICDIStackFrame frame) throws CDIException {
		return null;
	}
	
	public ICDIVariable[] listGlobalVariables(IPCDIDebugProcessSet procs) throws CDIException {
		return null;
	}
}
