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
package org.eclipse.ptp.debug.external.core.cdi.breakpoints;

import org.eclipse.ptp.debug.core.cdi.IPCDICondition;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIExceptionpoint;


/**
 * @author Clement chu
 * 
 */
public class Exceptionpoint extends Breakpoint implements IPCDIExceptionpoint {
	String fClazz;
	boolean fStopOnThrow;
	boolean fStopOnCatch;
	public Exceptionpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch, IPCDICondition cond) {
		super(IPCDIBreakpoint.REGULAR, cond);
		fClazz = clazz;
		fStopOnThrow = stopOnThrow;
		fStopOnCatch = stopOnCatch;
	}
	public String getExceptionName() {
		return fClazz;
	}
	public boolean isStopOnThrow() {
		return fStopOnThrow;
	}
	public boolean isStopOnCatch() {
		return fStopOnCatch;
	}
}