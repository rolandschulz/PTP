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
package org.eclipse.ptp.debug.external.cdi.breakpoints;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.external.cdi.BreakpointManager;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.model.Condition;
import org.eclipse.ptp.debug.external.cdi.model.PObject;

/**
 * @author Clement chu
 *
 */
public abstract class Breakpoint extends PObject implements IPCDIBreakpoint {
	ICDICondition condition;
	
	int type;
	boolean enable;

	public Breakpoint(int kind, ICDICondition cond) {
		super(null);
		type = kind;
		condition = cond;
		enable = true;
	}

	public ICDICondition getCondition() throws CDIException {
		if (condition == null) {
			condition =  new Condition(0, new String(), null);
		}
		return condition;
	}
	public boolean isEnabled() throws CDIException {
		return enable;
	}
	public boolean isHardware() {
		return (type == IPCDIBreakpoint.HARDWARE);
	}
	public boolean isTemporary() {
		return (type == IPCDIBreakpoint.TEMPORARY);
	}
	public void setCondition(ICDICondition newCondition) throws CDIException {
		Session session = (Session)getTarget().getSession();
		BreakpointManager mgr = session.getBreakpointManager();
		mgr.setCondition(this, newCondition);
		setCondition0(newCondition);
	}
	public void setCondition0(ICDICondition newCondition) {
		condition = newCondition;
	}
	public void setEnabled(boolean on) throws CDIException {
		Session session = (Session)getTarget().getSession();
		BreakpointManager mgr = session.getBreakpointManager();
		if (on == false && isEnabled() == true) { 
			mgr.disableBreakpoint(this);
		} else if (on == true && isEnabled() == false) {
			mgr.enableBreakpoint(this);
		}
	}
	public void setEnabled0(boolean on) {
		enable = on;
	}
}