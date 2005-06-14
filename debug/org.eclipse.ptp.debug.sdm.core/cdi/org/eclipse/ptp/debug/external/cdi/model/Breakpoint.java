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
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.ptp.debug.external.actionpoint.DebugActionpoint;

/**
 */
public class Breakpoint extends PTPObject implements ICDILocationBreakpoint {

	ICDILocation fLocation;
	ICDICondition condition;
	DebugActionpoint[] debugActionpoints;
	
	int type;
	boolean enable;

	public Breakpoint(Target target, int kind, ICDILocation loc, ICDICondition cond) {
		super(target);
		type = kind;
		fLocation = loc;
		condition = cond;
		enable = true;
	}

	public DebugActionpoint[] getDebugActionpoints() {
		return debugActionpoints;
	}

	public void setMIBreakpoints(DebugActionpoint[] newDebugActionpoints) {
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

	public void setCondition0(ICDICondition newCondition) {
		condition = newCondition;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean on) throws CDIException {
		System.out.println("Breakpoint.setEnabled()");
	}

	public void setEnabled0(boolean on) {
		enable = on;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDILocationBreakpoint#getLocation()
	 */
	public ICDILocation getLocation() throws CDIException {
		System.out.println("Breakpoint.getLocation()");
		if (fLocation == null) {
			return null;
		}
		return fLocation;
	}

	public void setLocation(ICDILocation loc) {
		fLocation = loc;
	}
}
