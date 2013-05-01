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
package org.eclipse.ptp.internal.debug.core.event;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.event.IPDebugInfo;
import org.eclipse.ptp.debug.core.launch.IPLaunch;

/**
 * @author Clement
 * @since 5.0
 */
public class PDebugInfo implements IPDebugInfo {
	private TaskSet allTasks = null;
	private TaskSet allRegTasks = null;
	private TaskSet allUnregTasks = null;
	private IPLaunch launch = null;

	public PDebugInfo(IPDebugInfo info) {
		this.launch = info.getLaunch();
		this.allTasks = info.getAllTasks();
		this.allRegTasks = info.getAllRegisteredTasks();
		this.allUnregTasks = info.getAllUnregisteredTasks();
	}

	/**
	 * @since 5.0
	 */
	public PDebugInfo(IPLaunch launch, TaskSet allTasks, TaskSet allRegTasks, TaskSet allUnregTasks) {
		this.launch = launch;
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
	 * @see org.eclipse.ptp.debug.core.event.IPDebugInfo#getLaunch()
	 */
	/**
	 * @since 5.0
	 */
	public IPLaunch getLaunch() {
		return launch;
	}
}
