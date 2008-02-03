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
package org.eclipse.ptp.debug.core.pdi.request;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Abstract base class of events that return results
 * 
 * @author clement
 *
 */
public abstract class AbstractEventResultRequest extends AbstractEventRequest implements IPDIInternalEventRequest {
	private final Object lock = new Object();
	protected Map<BitList, Object> results = new HashMap<BitList, Object>();
	protected long DEFAULT_TIMEOUT = 5000;
	
	public AbstractEventResultRequest(BitList tasks) {
		super(tasks);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventRequest#completed(org.eclipse.ptp.core.util.BitList, java.lang.Object)
	 */
	public boolean completed(BitList cTasks, Object result) {
		//if (!(result instanceof IProxyDebugEvent))
		//	return false;
		if (tasks.intersects(cTasks)) {
			storeResult(cTasks.copy(), result);
			notifyWaiting();
			return super.completed(cTasks, result);
		}
		return false;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventRequest#getResponseAction()
	 */
	public int getResponseAction() {
    	return ACTION_TERMINATED;
    }
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest#getResult(org.eclipse.ptp.core.util.BitList)
	 */
	public Object getResult(BitList  qTasks) throws PDIException {
		if (findResult(qTasks))
			return results.get(qTasks);
		
		for (Iterator<BitList> i = results.keySet().iterator(); i.hasNext();) {
			BitList sTasks = i.next();
			if (sTasks.intersects(qTasks))
				return results.get(sTasks);
		}
		throw new PDIException(qTasks, getName() + ": No request task found");
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest#getResultMap(org.eclipse.ptp.core.util.BitList)
	 */
	public Map<BitList, Object> getResultMap(BitList qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		return results;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest#waitUntilCompleted(org.eclipse.ptp.core.util.BitList)
	 */
	public void waitUntilCompleted(BitList qTasks) throws PDIException {
		waiting();
		if (status == ERROR)
			throw new PDIException(qTasks, getErrorMessage());
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest#waitUntilCompleted(org.eclipse.ptp.core.util.BitList, org.eclipse.core.runtime.IProgressMonitor)
	 */
	public void waitUntilCompleted(BitList qTasks, IProgressMonitor monitor) throws PDIException {
		while (!tasks.isEmpty() && (status == UNKNOWN || status == RUNNING)) {
			if (monitor.isCanceled()) {
				error("This request is interrupted.");
				break;
			}
			lockRequest(500);
			monitor.worked(1);
		}
		if (status == ERROR)
			throw new PDIException(qTasks, getErrorMessage());
		if (status == DONE)
			monitor.done();
	}
	
	/**
	 * 
	 */
	private void notifyWaiting() {
		releaseRequest();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.internal.core.pdi.request.AbstractEventRequest#doFinish()
	 */
	protected void doFinish() throws PDIException {
		notifyWaiting();
	}
	
	/**
	 * @param qTasks
	 * @return
	 */
	protected boolean findResult(BitList qTasks) {
		return results.containsKey(qTasks);
	}
	
	/**
	 * @param timeout
	 */
	protected void lockRequest(long timeout) {
		synchronized (lock) {
			try {
				lock.wait(timeout);
			}
			catch (InterruptedException ex) {} 
		}
	}
	
	/**
	 * 
	 */
	protected void releaseRequest() {
		synchronized (lock) {
			lock.notifyAll();
		}
	}
	
	/**
	 * @param rTasks
	 * @param result
	 */
	protected abstract void storeResult(BitList rTasks, Object result);

	/**
	 * @param rTasks
	 * @param result
	 */
	protected void storeUnknownResult(BitList rTasks, Object result) {
		results.put(rTasks, result);
	}
	
	/**
	 * 
	 */
	protected void waiting() {
		while (!tasks.isEmpty() && (status == UNKNOWN || status == RUNNING)) {
			lockRequest(DEFAULT_TIMEOUT);
		}
	}
}
