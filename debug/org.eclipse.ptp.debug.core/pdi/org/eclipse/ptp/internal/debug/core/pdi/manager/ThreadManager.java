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
package org.eclipse.ptp.internal.debug.core.pdi.manager;


import java.util.Hashtable;
import java.util.Map;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIThreadManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetInfoThreadsRequest;

/**
 * @author clement
 *
 */
public class ThreadManager extends AbstractPDIManager implements IPDIThreadManager {
	private static final IPDIThread[] noThreads = new IPDIThread[0];
	private Map<TaskSet, IPDIThread[]> threadMap = new Hashtable<TaskSet, IPDIThread[]>();
	
	public ThreadManager(IPDISession session) {
		super(session, true);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.manager.IPDIThreadManager#getThreads(org.eclipse.ptp.core.util.TaskSet)
	 */
	public IPDIThread[] getThreads(TaskSet qTasks) throws PDIException {
		IPDIThread[] threads =  threadMap.get(qTasks);
		if (threads == null) {
			threads = getPThreads(qTasks);
			threadMap.put(qTasks, threads);
		}
		return threads;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#shutdown()
	 */
	public void shutdown() {
		threadMap.clear();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.AbstractPDIManager#update(org.eclipse.ptp.core.util.TaskSet)
	 */
	public void update(TaskSet qTasks) throws PDIException {
	}
	
	/**
	 * @param qTasks
	 * @return
	 * @throws PDIException
	 */
	private IPDIThread[] getPThreads(TaskSet qTasks) throws PDIException {
		IPDIThread[] pthreads = noThreads;
		int currentThreadId = 0;
		
		IPDITarget target = session.findTarget(qTasks);
		IPDIGetInfoThreadsRequest request = session.getRequestFactory().getGetInfoThreadsRequest(qTasks);
		session.getEventRequestManager().addEventRequest(request);
		String[] ids = request.getThreadIds(qTasks);
		if (ids.length > 0) {
			pthreads = new IPDIThread[ids.length];
			for (int i=0; i<ids.length; i++) {
				pthreads[i] = session.getModelFactory().newThread(session, target, Integer.parseInt(ids[i]));
			}
		}
		else {
			pthreads = new IPDIThread[] { session.getModelFactory().newThread(session, target, 0) };
		}
		currentThreadId = pthreads[0].getId();
		if (currentThreadId == 0 && pthreads.length > 1) {
			currentThreadId = pthreads[1].getId();
		}
		return pthreads;
	}
}
