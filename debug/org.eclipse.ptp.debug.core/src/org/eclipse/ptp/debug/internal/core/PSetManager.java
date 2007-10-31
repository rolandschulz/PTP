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
package org.eclipse.ptp.debug.internal.core;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDISession;

/**
 * @author clement
 *
 */
public class PSetManager {
	private PSession session = null;
	private Map<String, BitList> setMap = null;
	public PSetManager(PSession session) {
		this.session = session;
	}
	public PSession getSession() {
		return session;
	}
	protected IPDISession getPDISession() {
		return getSession().getPDISession();
	}
	public void dispose(IProgressMonitor monitor) {
		setMap.clear();
	}
	public void initialize(IProgressMonitor monitor) {
		setMap = new Hashtable<String, BitList>();
		createSet(PreferenceConstants.SET_ROOT_ID, session.getTasks());
	}
	public BitList getTasks(String sid) {
		return setMap.get(sid);
	}
	public void createSet(String sid, BitList tasks) {
		BitList oldTasks = getTasks(sid);
		if (oldTasks == null) {
			setMap.put(sid, tasks);
		}
		else {
			oldTasks.or(tasks);
		}
	}
	public void addTasks(String sid, BitList tasks) {
		createSet(sid, tasks);
	}
	public void deleteSets(String sid) {
		setMap.remove(sid);
	}
	public void removeTasks(String sid, BitList tasks) {
		BitList oldTasks = getTasks(sid);
		if (oldTasks != null) {
			oldTasks.andNot(tasks);
		}
		if (oldTasks.isEmpty()) {
			deleteSets(sid);
		}
	}
}
