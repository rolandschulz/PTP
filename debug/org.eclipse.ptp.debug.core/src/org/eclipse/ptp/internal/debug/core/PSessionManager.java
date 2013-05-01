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

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.ptp.debug.core.IPSession;

/**
 * @author clement
 * 
 */
public class PSessionManager implements IDebugEventSetListener {
	private final Map<String, IPSession> sessionMap = new Hashtable<String, IPSession>();

	public PSessionManager() {
		DebugPlugin.getDefault().addDebugEventListener(this);
	}

	public void addSession(String jobId, IPSession session) {
		synchronized (sessionMap) {
			sessionMap.put(jobId, session);
		}
	}

	public IPSession getSession(String jobId) {
		synchronized (sessionMap) {
			return sessionMap.get(jobId);
		}
	}

	public IPSession[] getSessions() {
		synchronized (sessionMap) {
			return sessionMap.values().toArray(new IPSession[0]);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.debug.core.IDebugEventSetListener#handleDebugEvents(org.eclipse
	 * .debug.core.DebugEvent[])
	 */
	public void handleDebugEvents(DebugEvent[] events) {
	}

	public void shutdown() {
		DebugPlugin.getDefault().removeDebugEventListener(this);
		sessionMap.clear();
	}
}
