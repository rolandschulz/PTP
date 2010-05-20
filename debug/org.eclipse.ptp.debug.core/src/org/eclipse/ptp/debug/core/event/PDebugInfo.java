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
package org.eclipse.ptp.debug.core.event;

import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.debug.core.TaskSet;

/**
 * @author Clement
 */
public class PDebugInfo implements IPDebugInfo {
	private TaskSet allTasks = null;
	private TaskSet allRegTasks = null;
	private TaskSet allUnregTasks = null;
	private IPJob job = null;

	public PDebugInfo(IPDebugInfo info) {
		this.job = info.getJob();
		this.allTasks = info.getAllTasks();
		this.allRegTasks = info.getAllRegisteredTasks();
		this.allUnregTasks = info.getAllUnregisteredTasks();
	}

	/**
	 * @since 4.0
	 */
	public PDebugInfo(IPJob job, TaskSet allTasks, TaskSet allRegTasks, TaskSet allUnregTasks) {
		this.job = job;
		this.allTasks = allTasks;
		this.allRegTasks = allRegTasks;
		this.allUnregTasks = allUnregTasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.event.IPDebugInfo#getAllTasks()
	 */
	/**
	 * @since 4.0
	 */
	public TaskSet getAllTasks() {
		return allTasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.event.IPDebugInfo#getAllRegisteredTasks()
	 */
	/**
	 * @since 4.0
	 */
	public TaskSet getAllRegisteredTasks() {
		return allRegTasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.event.IPDebugInfo#getAllUnregisteredTasks()
	 */
	/**
	 * @since 4.0
	 */
	public TaskSet getAllUnregisteredTasks() {
		return allUnregTasks;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.event.IPDebugInfo#getJob()
	 */
	public IPJob getJob() {
		return job;
	}
}
