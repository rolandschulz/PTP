/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cldt.debug.mi.core.cdi.model;

import org.eclipse.cldt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cldt.debug.core.cdi.model.ICDIExceptionpoint;

/**
 * Exceptionpoint
 */
public class Exceptionpoint extends Breakpoint implements ICDIExceptionpoint {

	String fClazz;
	boolean fStopOnThrow;
	boolean fStopOnCatch;

	/**
	 */
	public Exceptionpoint(Target target, String clazz, boolean stopOnThrow, boolean stopOnCatch) {
		super(target, ICDIBreakpoint.REGULAR, null, null);
		fClazz = clazz;
		fStopOnThrow = stopOnThrow;
		fStopOnCatch = stopOnCatch;
	}

	public String getExceptionName() {
		return fClazz;
	}

	/**
	 * @param target
	 * @param miBreak
	 */
//	public Exceptionpoint(Target target, MIBreakpoint miBreak) {
//		super(target, miBreak);
//	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint#isStopOnThrow()
	 */
	public boolean isStopOnThrow() {
		return fStopOnThrow;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExceptionpoint#isStopOnCatch()
	 */
	public boolean isStopOnCatch() {
		return fStopOnCatch;
	}

}
