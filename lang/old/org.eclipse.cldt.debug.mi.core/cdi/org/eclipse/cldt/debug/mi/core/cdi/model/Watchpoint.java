/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.cldt.debug.mi.core.cdi.model;

import org.eclipse.cldt.debug.core.cdi.CDIException;
import org.eclipse.cldt.debug.core.cdi.ICDICondition;
import org.eclipse.cldt.debug.core.cdi.model.ICDIWatchpoint;
import org.eclipse.cldt.debug.mi.core.output.MIBreakpoint;

/**
 */
public class Watchpoint extends Breakpoint implements ICDIWatchpoint {

	int watchType;
	String what;

	public Watchpoint(Target target, String expression, int type, int wType, ICDICondition cond) {
		super(target, type, null, cond); //$NON-NLS-1$
		watchType = wType;
		what = expression;
	}

	/**
	 * @see org.eclipse.cldt.debug.core.cdi.ICDIWatchpoint#getWatchExpression()
	 */
	public String getWatchExpression() throws CDIException {
		if (what == null) {
			MIBreakpoint[] miPoints = getMIBreakpoints();
			if (miPoints != null && miPoints.length > 0) {
				return miPoints[0].getWhat();
			}
		}
		return what;
	}

	/**
	 * @see org.eclipse.cldt.debug.core.cdi.ICDIWatchpoint#isReadType()
	 */
	public boolean isReadType() {
		return ((watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ);
//		MIBreakpoint miPoint = getMIBreakpoint();
//		if (miPoint != null)
//			return getMIBreakpoint().isReadWatchpoint() || getMIBreakpoint().isAccessWatchpoint();
//		return ((watchType & ICDIWatchpoint.READ) == ICDIWatchpoint.READ);
	}

	/**
	 * @see org.eclipse.cldt.debug.core.cdi.ICDIWatchpoint#isWriteType()
	 */
	public boolean isWriteType() {
		return ((watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE);
//		MIBreakpoint miPoint = getMIBreakpoint();
//		if (miPoint != null)
//			return getMIBreakpoint().isAccessWatchpoint() || getMIBreakpoint().isWriteWatchpoint();
//		return ((watchType & ICDIWatchpoint.WRITE) == ICDIWatchpoint.WRITE);
	}

}
