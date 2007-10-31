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
package org.eclipse.ptp.debug.internal.core.pdi.model;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public abstract class Breakpoint extends SessionObject implements IPDIBreakpoint {
	static int internal_counter = 0;

	IPDICondition condition;
	int bpid = -1; //by default id is -1
	int type;
	boolean enable;
	int internal_id;
	
	public Breakpoint(Session session, BitList tasks, int type, IPDICondition condition, boolean enabled) {
		super(session, tasks);
		this.type = type;
		this.condition = condition;
		this.enable = enabled;
		this.internal_id = internal_counter++;
	}
	public void changeTasks(BitList tasks) {
		this.tasks = tasks;
	}
	public IPDICondition getCondition() throws PDIException {
		if (condition == null) {
			condition = new Condition(0, new String(), null);
		}
		return condition;
	}
	
	public boolean isEnabled() { 
		return enable;
	}
	public int getType() {
		return type;
	}
	public boolean isHardware() {
		return (type == IPDIBreakpoint.HARDWARE);
	}
	public boolean isTemporary() {
		return (type == IPDIBreakpoint.TEMPORARY);
	}
	public void setCondition(IPDICondition condition) {
		this.condition = condition;
	}
	public void setEnabled(boolean enabled) {
		this.enable = enabled;
	}
	public int getBreakpointID() {
		return bpid;
	}
	public void setBreakpointID(int bpid) {
		this.bpid = bpid;
	}
	public int getInternalID() {
		return internal_id;
	}
}
