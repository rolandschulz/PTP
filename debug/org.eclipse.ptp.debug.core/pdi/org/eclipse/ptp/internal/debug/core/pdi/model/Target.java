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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.PDILocationFactory;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMixedInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegister;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterGroup;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRuntimeOptions;
import org.eclipse.ptp.debug.core.pdi.model.IPDISharedLibrary;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrameDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetInfoThreadsRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetThreadSelectRequest;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;
import org.eclipse.ptp.internal.debug.core.pdi.messages.Messages;

/**
 * @author clement
 * 
 */
public class Target extends SessionObject implements IPDITarget {
	private final Thread[] noThreads = new Thread[0];
	private Thread[] currentThreads;
	private int currentThreadId;
	private final ReentrantLock lock = new ReentrantLock();

	public Target(IPDISession session, TaskSet tasks) {
		super(session, tasks);
		currentThreads = noThreads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#createGlobalVariable(org.eclipse.ptp.debug.core.pdi.model.
	 * IPDIGlobalVariableDescriptor)
	 */
	public IPDIGlobalVariable createGlobalVariable(IPDIGlobalVariableDescriptor varDesc) throws PDIException {
		if (varDesc instanceof IPDIGlobalVariableDescriptor) {
			IPDIVariableManager varMgr = getSession().getVariableManager();
			return varMgr.createGlobalVariable(varDesc);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDITarget#createRegister(org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor)
	 */
	public IPDIRegister createRegister(IPDIRegisterDescriptor varDesc) throws PDIException {
		if (varDesc instanceof RegisterDescriptor) {
			return session.getRegisterManager().createRegister(varDesc);
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.debug.core.pdi.model.IPDITarget#evaluateExpressionToString(org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame
	 * , java.lang.String)
	 */
	public String evaluateExpressionToString(IPDIStackFrame context, String expr) throws PDIException {
		Target target = (Target) context.getTarget();
		Thread currentThread = target.getCurrentThread();
		IPDIStackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread(context.getThread(), false);
		((Thread) context.getThread()).setCurrentStackFrame(context, false);
		try {
			IAIF aif = null;
			IPDIVariable var = session.getVariableManager().getVariableByName(tasks, expr);
			if (var == null) {
				aif = session.getExpressionManager().getExpressionValue(getTasks(), expr);
			} else {
				aif = var.getAIF();
			}
			if (aif != null) {
				return aif.getValue().toString();
			}
			throw new PDIException(getTasks(), Messages.Target_0 + expr);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#getCurrentThread()
	 */
	public synchronized Thread getCurrentThread() throws PDIException {
		Thread[] threads = getThreads();
		for (Thread thread : threads) {
			Thread pthread = thread;
			if (pthread.getId() == currentThreadId) {
				return pthread;
			}
		}
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#getGlobalVariableDescriptors(java.lang.String, java.lang.String,
	 * java.lang.String)
	 */
	public IPDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name)
			throws PDIException {
		return session.getVariableManager().getGlobalVariableDescriptor(getTasks(), filename, function, name);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISourceManagement#getInstructions(java.math.BigInteger, java.math.BigInteger)
	 */
	public IPDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws PDIException {
		return session.getSourceManager().getInstructions(getTasks(), startAddress, endAddress);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISourceManagement#getInstructions(java.lang.String, int)
	 */
	public IPDIInstruction[] getInstructions(String filename, int linenum) throws PDIException {
		return session.getSourceManager().getInstructions(getTasks(), filename, linenum);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISourceManagement#getInstructions(java.lang.String, int, int)
	 */
	public IPDIInstruction[] getInstructions(String filename, int linenum, int lines) throws PDIException {
		return session.getSourceManager().getInstructions(getTasks(), filename, linenum, lines);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISourceManagement#getMixedInstructions(java.math.BigInteger,
	 * java.math.BigInteger)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws PDIException {
		return session.getSourceManager().getMixedInstructions(getTasks(), startAddress, endAddress);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISourceManagement#getMixedInstructions(java.lang.String, int)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws PDIException {
		return session.getSourceManager().getMixedInstructions(getTasks(), filename, linenum);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISourceManagement#getMixedInstructions(java.lang.String, int, int)
	 */
	public IPDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws PDIException {
		return session.getSourceManager().getMixedInstructions(getTasks(), filename, linenum, lines);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#getRegisterGroups()
	 */
	public IPDIRegisterGroup[] getRegisterGroups() throws PDIException {
		return session.getRegisterManager().getRegisterGroups(getTasks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#getRuntimeOptions()
	 */
	public IPDIRuntimeOptions getRuntimeOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISharedLibraryManagement#getSharedLibraries()
	 */
	public IPDISharedLibrary[] getSharedLibraries() throws PDIException {
		throw new PDIException(getTasks(), Messages.Target_1);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISourceManagement#getSourcePaths()
	 */
	public String[] getSourcePaths() throws PDIException {
		return session.getSourceManager().getSourcePaths(getTasks());
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#getThreads()
	 */
	public synchronized Thread[] getThreads() throws PDIException {
		if (currentThreads.length == 0) {
			currentThreads = getPThreads();
		}
		return currentThreads;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#lockTarget()
	 */
	public void lockTarget() {
		lock.lock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#releaseTarget()
	 */
	public void releaseTarget() {
		lock.unlock();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#setCurrentThread(org.eclipse.ptp.internal.debug.core.pdi.model.Thread,
	 * boolean)
	 */
	public void setCurrentThread(IPDIThread pthread, boolean doUpdate) throws PDIException {
		if (pthread instanceof IPDIThread) {
			int id = pthread.getId();
			if (id == 0) {
				return;
			}
			if (currentThreadId != id) {
				IPDISetThreadSelectRequest request = session.getRequestFactory().getSetThreadSelectRequest(session, getTasks(), id);
				session.getEventRequestManager().addEventRequest(request);
				currentThreadId = request.getThreadId(getTasks());
				IPDIStackFrameDescriptor frame = request.getStackFrame(getTasks());
				if (frame != null) {
					int depth = pthread.getStackFrameCount();
					IPDILocator locator = PDILocationFactory.newLocator(frame.getLocator().getFile(), frame.getLocator()
							.getFunction(), frame.getLocator().getLineNumber(), frame.getLocator().getAddress());
					pthread.setCurrentStackFrame(session.getModelFactory().newStackFrame(session, pthread,
							depth - frame.getLevel(), locator));
				}

				if (doUpdate) {
					IPDIRegisterManager regMgr = session.getRegisterManager();
					if (regMgr.isAutoUpdate()) {
						regMgr.update(getTasks());
					}
					IPDIVariableManager varMgr = session.getVariableManager();
					if (varMgr.isAutoUpdate()) {
						varMgr.update(getTasks());
					}
				}
			}
			if (currentThreadId != id) {
				session.getEventManager().fireEvents(
						new IPDIEvent[] { session.getEventFactory().newDestroyedEvent(
								session.getEventFactory().newThreadInfo(session, getTasks(), id, getThread(id))) });
				throw new PDIException(getTasks(), Messages.Target_2 + id);
			}
		} else {
			throw new PDIException(getTasks(), Messages.Target_3);
		}
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDISourceManagement#setSourcePaths(java.lang.String[])
	 */
	public void setSourcePaths(String[] srcPaths) throws PDIException {
		session.getSourceManager().setSourcePaths(getTasks(), srcPaths);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#setSupended(boolean)
	 */
	public synchronized void setSupended(boolean state) {
		notifyAll();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDITarget#updateState(int)
	 */
	public synchronized void updateState(int newThreadId) {
		Thread[] oldThreads = currentThreads;

		lockTarget();
		currentThreadId = newThreadId;
		try {
			currentThreads = getPThreads();
		} catch (PDIException e) {
			currentThreads = noThreads;
		}
		releaseTarget();

		List<Integer> pList = new ArrayList<Integer>(currentThreads.length);
		for (int i = 0; i < currentThreads.length; i++) {
			boolean found = false;
			for (Thread oldThread : oldThreads) {
				if (currentThreads[i].getId() == oldThread.getId()) {
					oldThread.clearState();
					currentThreads[i] = oldThread;
					found = true;
					break;
				}
			}
			if (!found) {
				pList.add(new Integer(currentThreads[i].getId()));
			}
		}
		if (!pList.isEmpty()) {
			IPDIEvent[] events = new IPDIEvent[pList.size()];
			for (int j = 0; j < events.length; j++) {
				int id = pList.get(j).intValue();
				events[j] = session.getEventFactory().newCreatedEvent(
						session.getEventFactory().newThreadInfo(session, getTasks(), id, getThread(id)));
			}
			session.getEventManager().fireEvents(events);
		}
		// Fire destroyed event for old threads
		List<Integer> dList = new ArrayList<Integer>(oldThreads.length);
		for (Thread oldThread : oldThreads) {
			boolean found = false;
			for (Thread currentThread : currentThreads) {
				if (currentThread.getId() == oldThread.getId()) {
					found = true;
					break;
				}
			}
			if (!found) {
				dList.add(new Integer(oldThread.getId()));
			}
		}
		if (!dList.isEmpty()) {
			IPDIEvent[] events = new IPDIEvent[dList.size()];
			for (int j = 0; j < events.length; j++) {
				int id = dList.get(j).intValue();
				events[j] = session.getEventFactory().newDestroyedEvent(
						session.getEventFactory().newThreadInfo(session, getTasks(), id, getThread(id)));
			}
			session.getEventManager().fireEvents(events);
		}
	}

	/**
	 * @return
	 * @throws PDIException
	 */
	private synchronized Thread[] getPThreads() throws PDIException {
		Thread[] pthreads = noThreads;
		lockTarget();
		try {
			IPDIGetInfoThreadsRequest request = session.getRequestFactory().getGetInfoThreadsRequest(getTasks());
			session.getEventRequestManager().addEventRequest(request);
			String[] ids = request.getThreadIds(getTasks());
			if (ids.length > 0) {
				pthreads = new Thread[ids.length];
				for (int i = 0; i < ids.length; i++) {
					int tid = 0;
					try {
						tid = Integer.parseInt(ids[i]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					pthreads[i] = new Thread(session, this, tid);
				}
			} else {
				pthreads = new Thread[] { new Thread(session, this, 0) };
			}
			currentThreadId = pthreads[0].getId();
			if (currentThreadId == 0 && pthreads.length > 1) {
				currentThreadId = pthreads[1].getId();
			}
		} finally {
			releaseTarget();
		}
		return pthreads;
	}

	/**
	 * @param tid
	 * @return
	 */
	private Thread getThread(int tid) {
		Thread th = null;
		if (currentThreads != null) {
			for (Thread pthread : currentThreads) {
				if (pthread.getId() == tid) {
					th = pthread;
					break;
				}
			}
		}
		return th;
	}
}
