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
package org.eclipse.ptp.debug.external.commands;

import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.external.IAbstractDebugger;

/**
 * @author Clement chu
 * 
 */
public class SetFunctionBreakpointCommand extends AbstractDebugCommand {
	private ICDIFunctionBreakpoint funcBpt = null;
	//private IAbstractDebugger debugger = null;
	
	public SetFunctionBreakpointCommand(BitList tasks, ICDIFunctionBreakpoint funcBpt) {
		super(tasks, true, false);
		this.funcBpt = funcBpt;
	}
	public void execCommand(IAbstractDebugger debugger) throws PCDIException {
		//this.debugger = debugger;
		debugger.setFunctionBreakpoint(tasks, funcBpt);
		debugger.handleBreakpointCreatedEvent(tasks);
	}
	/*
	public void setReturn(Object result) {
		super.setReturn(result);
		if (result.equals(OK)) {
			debugger.handleBreakpointCreatedEvent(tasks);
		}
	}
	public void setLineBreakpoint() throws PCDIException {
		if (waitForReturn()) {
			if (result.equals(OK)) {
				return;
			}
		}
		throw new PCDIException("Function breakpoint cannot set in " + tasks.toString());		
	}
	*/
}
