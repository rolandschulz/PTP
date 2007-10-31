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
package org.eclipse.ptp.debug.internal.core.pdi;

import java.util.Hashtable;
import java.util.Map;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDIThreadManager;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.internal.core.pdi.model.Target;
import org.eclipse.ptp.debug.internal.core.pdi.model.Thread;
import org.eclipse.ptp.debug.internal.core.pdi.request.GetInfoThreadsRequest;

/**
 * @author clement
 *
 */
public class ThreadManager extends Manager implements IPDIThreadManager {
	static final Thread[] noThreads = new Thread[0];
	private Map<BitList, ThreadSet> threadMap;

	class ThreadSet {
		IPDIThread[] currentThreads;
		int currentThreadId;
		ThreadSet(IPDIThread[] threads, int id) {
			currentThreads = threads;
			currentThreadId = id;
		}
	}
	public ThreadManager(Session session) {
		super(session, true);
		threadMap = new Hashtable<BitList, ThreadSet>();
	}
	public void shutdown() {
		threadMap.clear();
	}
	public IPDIThread[] getThreads() throws PDIException {
		return new IPDIThread[] {};
	}
	public IPDIThread[] getThreads(BitList qTasks) throws PDIException {
		ThreadSet set =  (ThreadSet)threadMap.get(qTasks);
		if (set == null) {
			set = getPThreads(qTasks);
			threadMap.put(qTasks, set);
		}
		return set.currentThreads;
	}
	public ThreadSet getPThreads(BitList qTasks) throws PDIException {
		Thread[] pthreads = noThreads;
		int currentThreadId = 0;
		
		Target target = (Target)session.findTarget(qTasks);
		GetInfoThreadsRequest request = new GetInfoThreadsRequest(qTasks);
		session.getEventRequestManager().addEventRequest(request);
		String[] ids = request.getThreadIds(qTasks);
		if (ids.length > 0) {
			pthreads = new Thread[ids.length];
			for (int i=0; i<ids.length; i++) {
				pthreads[i] = new Thread(session, target, Integer.parseInt(ids[i]));
			}
		}
		else {
			pthreads = new Thread[] { new Thread(session, target, 0) };
		}
		currentThreadId = pthreads[0].getId();
		if (currentThreadId == 0 && pthreads.length > 1) {
			currentThreadId = pthreads[1].getId();
		}
		return new ThreadSet(pthreads, currentThreadId);
	}
	public void update(BitList qTasks) throws PDIException {
	}
}
