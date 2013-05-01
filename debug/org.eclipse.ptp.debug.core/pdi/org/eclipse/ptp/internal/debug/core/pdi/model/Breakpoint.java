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
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public abstract class Breakpoint extends SessionObject implements IPDIBreakpoint {
	private static int internal_counter = 0;

	private IPDICondition condition;
	private int bpid = -1; //by default id is -1
	private int type;
	private boolean enable;
	private boolean deleted = false;
	private int internal_id;

	private TaskSet pendingTasks; // tasks remaining to set breakpoint

	public Breakpoint(IPDISession session, TaskSet tasks, int type, IPDICondition condition, boolean enabled) {
		super(session, tasks);
		this.type = type;
		this.condition = condition;
		this.enable = enabled;
		this.internal_id = internal_counter++;
 		this.pendingTasks = tasks.copy();
	}
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#getBreakpointID()
	 */
	public int getBreakpointID() {
		return bpid;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#getCondition()
	 */
	public IPDICondition getCondition() throws PDIException {
		if (condition == null) {
			condition = new Condition(0, new String(), null);
		}
		return condition;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#getInternalID()
	 */
	public int getInternalID() {
		return internal_id;
	}
	
	public TaskSet getPendingTasks() {
		return pendingTasks;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#isDeleted()
	 */
	public boolean isDeleted() {
		return deleted;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#isEnabled()
	 */
	public boolean isEnabled() { 
		return enable;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#isHardware()
	 */
	public boolean isHardware() {
		return (type == IPDIBreakpoint.HARDWARE);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#isTemporary()
	 */
	public boolean isTemporary() {
		return (type == IPDIBreakpoint.TEMPORARY);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#setBreakpointID(int)
	 */
	public void setBreakpointID(int bpid) {
		this.bpid = bpid;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#setCondition(org.eclipse.ptp.debug.core.pdi.IPDICondition)
	 */
	public void setCondition(IPDICondition condition) {
		this.condition = condition;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#setDeleted()
	 */
	public void setDeleted() {
		this.deleted = true;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIBreakpoint#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		this.enable = enabled;
	}
}
