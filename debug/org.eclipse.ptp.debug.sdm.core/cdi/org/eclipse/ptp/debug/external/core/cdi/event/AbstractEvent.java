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
/**********************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
***********************************************************************/
package org.eclipse.ptp.debug.external.core.cdi.event;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIObject;


public abstract class AbstractEvent implements IPCDIEvent {
	IPCDISession session = null;
	BitList tasks = null;
	IPCDIObject object = null;

	public AbstractEvent(IPCDISession session, BitList tasks, IPCDIObject object) {
		this.session = session;
		this.tasks = tasks;
		this.object = object;
	}
	public AbstractEvent(IPCDISession session, BitList tasks) {
		this(session, tasks, null);
	}
	public IPJob getDebugJob() {
		return session.getJob();
	}
	public BitList getAllProcesses() {
		if (tasks != null)
			return tasks.copy();
		return tasks;
	}
	public BitList getAllUnregisteredProcesses() {
		BitList regTasks = session.getRegisteredTargets();
		BitList orgTasks = getAllProcesses();
		orgTasks.andNot(regTasks);
		return orgTasks;
	}

	public BitList getAllRegisteredProcesses() {
		BitList regTasks = session.getRegisteredTargets();
		BitList orgTasks = getAllProcesses();
		orgTasks.and(regTasks);
		return orgTasks;
	}
	
	public IPCDIObject getSource() {
		return object;
	}
	public IPCDIObject getSource(int task_id) {
		if (containTask(task_id))
			return session.getTarget(task_id);
		return null;
	}
	public boolean containTask(int task_id) {
		try {
			return tasks.get(task_id);
		} catch (IndexOutOfBoundsException e) {
			return false;
		}
	}
}
