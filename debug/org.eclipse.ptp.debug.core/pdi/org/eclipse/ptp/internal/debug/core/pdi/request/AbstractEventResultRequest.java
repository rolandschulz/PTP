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
package org.eclipse.ptp.internal.debug.core.pdi.request;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.osgi.util.NLS;
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * Abstract base class of events that return results
 * 
 * @author clement
 * 
 */
public abstract class AbstractEventResultRequest extends AbstractEventRequest implements IPDIInternalEventRequest {
	private final Object lock = new Object();
	protected Map<TaskSet, Object> results = new HashMap<TaskSet, Object>();
	protected long DEFAULT_TIMEOUT = 5000;

	/**
	 * @since 4.0
	 */
	public AbstractEventResultRequest(TaskSet tasks) {
		super(tasks);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventRequest#
	 * completed(org.eclipse.ptp.core.util.TaskSet, java.lang.Object)
	 */
	@Override
	public boolean completed(TaskSet cTasks, Object result) {
		// if (!(result instanceof IProxyDebugEvent))
		// return false;
		if (tasks.intersects(cTasks)) {
			storeResult(cTasks.copy(), result);
			notifyWaiting();
			return super.completed(cTasks, result);
		}
		return false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventRequest#
	 * getResponseAction()
	 */
	@Override
	public int getResponseAction() {
		return ACTION_TERMINATED;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest#getResult
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public Object getResult(TaskSet qTasks) throws PDIException {
		if (findResult(qTasks)) {
			return results.get(qTasks);
		}

		for (TaskSet sTasks : results.keySet()) {
			if (sTasks.intersects(qTasks)) {
				return results.get(sTasks);
			}
		}
		throw new PDIException(qTasks, NLS.bind(Messages.AbstractEventResultRequest_0, getName()));
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest#getResultMap
	 * (org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public Map<TaskSet, Object> getResultMap(TaskSet qTasks) throws PDIException {
		waitUntilCompleted(qTasks);
		return results;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest#
	 * waitUntilCompleted(org.eclipse.ptp.core.util.TaskSet)
	 */
	/**
	 * @since 4.0
	 */
	public void waitUntilCompleted(TaskSet qTasks) throws PDIException {
		waiting();
		if (status == ERROR) {
			throw new PDIException(qTasks, getErrorMessage());
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.request.IPDIInternalEventRequest#
	 * waitUntilCompleted(org.eclipse.ptp.core.util.TaskSet,
	 * org.eclipse.core.runtime.IProgressMonitor)
	 */
	/**
	 * @since 4.0
	 */
	public void waitUntilCompleted(TaskSet qTasks, IProgressMonitor monitor) throws PDIException {
		while (!tasks.isEmpty() && (status == UNKNOWN || status == RUNNING)) {
			if (monitor.isCanceled()) {
				error(Messages.AbstractEventResultRequest_1);
				break;
			}
			lockRequest(500);
			monitor.worked(1);
		}
		if (status == ERROR) {
			throw new PDIException(qTasks, getErrorMessage());
		}
		if (status == DONE) {
			monitor.done();
		}
	}

	/**
	 * 
	 */
	private void notifyWaiting() {
		releaseRequest();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.internal.debug.core.pdi.request.AbstractEventRequest#
	 * doFinish()
	 */
	/**
	 * @since 4.0
	 */
	@Override
	protected void doFinish() throws PDIException {
		notifyWaiting();
	}

	/**
	 * @param qTasks
	 * @return
	 * @since 4.0
	 */
	protected boolean findResult(TaskSet qTasks) {
		return results.containsKey(qTasks);
	}

	/**
	 * @param timeout
	 */
	protected void lockRequest(long timeout) {
		synchronized (lock) {
			try {
				lock.wait(timeout);
			} catch (InterruptedException ex) {
			}
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
	 * @since 4.0
	 */
	protected abstract void storeResult(TaskSet rTasks, Object result);

	/**
	 * @param rTasks
	 * @param result
	 * @since 4.0
	 */
	protected void storeUnknownResult(TaskSet rTasks, Object result) {
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
