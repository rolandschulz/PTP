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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;

/**
 * @author clement
 *
 */
public class Exceptionpoint extends Breakpoint implements IPDIExceptionpoint {
	private String clazz;
	private boolean stopOnThrow;
	private boolean stopOnCatch;
	private IPDIFunctionBreakpoint[] funcBpts = {};
	
	public Exceptionpoint(IPDISession session, TaskSet tasks, String clazz, boolean stopOnThrow, boolean stopOnCatch, IPDICondition condition, boolean enabled, IPDIFunctionBreakpoint[] funcBpts) {
		super(session, tasks, IPDIBreakpoint.REGULAR, condition, enabled);
		this.clazz = clazz;
		this.stopOnThrow = stopOnThrow;
		this.stopOnCatch = stopOnCatch;
		this.funcBpts = funcBpts;
	}
	
	/**
	 * @return
	 */
	public String getExceptionName() {
		return clazz;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint#isStopOnThrow()
	 */
	public boolean isStopOnThrow() {
		return stopOnThrow;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint#isStopOnCatch()
	 */
	public boolean isStopOnCatch() {
		return stopOnCatch;
	}
	
	/**
	 * @return
	 */
	public IPDIFunctionBreakpoint[] getFunctionBreakpoints() {
		return funcBpts;
	}
}
