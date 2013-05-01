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
package org.eclipse.ptp.internal.debug.core;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.debug.core.IPSession;
import org.eclipse.ptp.debug.core.IPSetManager;
import org.eclipse.ptp.debug.core.TaskSet;

/**
 * @author clement
 * 
 */
public class PSetManager implements IPSetManager {
	private final IPSession session;
	private final Map<String, TaskSet> setMap = new HashMap<String, TaskSet>();

	public PSetManager(IPSession session) {
		this.session = session;
		createSet(PreferenceConstants.SET_ROOT_ID, session.getTasks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPSetManager#addTasks(java.lang.String
	 * , org.eclipse.ptp.core.util.TaskSet)
	 */
	public void addTasks(String sid, TaskSet tasks) {
		createSet(sid, tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPSetManager#createSet(java.lang.
	 * String, org.eclipse.ptp.core.util.TaskSet)
	 */
	public void createSet(String sid, TaskSet tasks) {
		TaskSet oldTasks = getTasks(sid);
		if (oldTasks == null) {
			setMap.put(sid, tasks);
		} else {
			oldTasks.or(tasks);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPSetManager#deleteSets(java.lang
	 * .String)
	 */
	public void deleteSets(String sid) {
		setMap.remove(sid);
	}

	/**
	 * @param monitor
	 */
	public void dispose(IProgressMonitor monitor) {
		setMap.clear();
	}

	/**
	 * @return
	 */
	public IPSession getSession() {
		return session;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPSetManager#getTasks(java.lang.String
	 * )
	 */
	public TaskSet getTasks(String sid) {
		return setMap.get(sid);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.IPSetManager#removeTasks(java.lang
	 * .String, org.eclipse.ptp.core.util.TaskSet)
	 */
	public void removeTasks(String sid, TaskSet tasks) {
		TaskSet oldTasks = getTasks(sid);
		if (oldTasks != null) {
			oldTasks.andNot(tasks);
			if (oldTasks.isEmpty()) {
				deleteSets(sid);
			}
		}
	}
}
