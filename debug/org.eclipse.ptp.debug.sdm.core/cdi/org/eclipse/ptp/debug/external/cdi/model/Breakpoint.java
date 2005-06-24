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
package org.eclipse.ptp.debug.external.cdi.model;


import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.ptp.debug.external.actionpoint.DebugActionpoint;

/**
 */
public class Breakpoint extends PTPObject implements ICDIBreakpoint {

	ICDICondition condition;
	DebugActionpoint[] debugActionpoints;
	
	int type;
	boolean enable;

	public Breakpoint(Target target, int kind, ICDICondition cond) {
		super(target);
		type = kind;
		condition = cond;
		enable = true;
	}

	public DebugActionpoint[] getDebugActionpoints() {
		return debugActionpoints;
	}

	public void setDebugActionpoints(DebugActionpoint[] newDebugActionpoints) {
		debugActionpoints = newDebugActionpoints;
	}

	public boolean isDeferred() {
		return (debugActionpoints == null || debugActionpoints.length == 0);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#getCondition()
	 */
	public ICDICondition getCondition() throws CDIException {
		System.out.println("Breakpoint.getCondition()");
		if (condition == null) {
			return null;
		}
		return condition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isEnabled()
	 */
	public boolean isEnabled() throws CDIException {
		return enable;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		return (type == ICDIBreakpoint.HARDWARE);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		return (type == ICDIBreakpoint.TEMPORARY);
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setCondition(ICDICondition)
	 */
	public void setCondition(ICDICondition newCondition) throws CDIException {
		System.out.println("Breakpoint.setCondition()");
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean on) throws CDIException {
		System.out.println("Breakpoint.setEnabled()");
	}
}
