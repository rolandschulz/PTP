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
package org.eclipse.ptp.debug.internal.core.pdi.model;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.PDebugUtils;
import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.event.IPDIEvent;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariable;
import org.eclipse.ptp.debug.core.pdi.model.IPDIGlobalVariableDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIMixedInstruction;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegister;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterGroup;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRuntimeOptions;
import org.eclipse.ptp.debug.core.pdi.model.IPDISharedLibrary;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;
import org.eclipse.ptp.debug.internal.core.pdi.Locator;
import org.eclipse.ptp.debug.internal.core.pdi.Lock;
import org.eclipse.ptp.debug.internal.core.pdi.RegisterManager;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;
import org.eclipse.ptp.debug.internal.core.pdi.VariableManager;
import org.eclipse.ptp.debug.internal.core.pdi.event.CreatedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.DestroyedEvent;
import org.eclipse.ptp.debug.internal.core.pdi.event.ThreadInfo;
import org.eclipse.ptp.debug.internal.core.pdi.request.GetInfoThreadsRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetThreadSelectRequest;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugStackFrame;

/**
 * @author clement
 *
 */
public class Target extends SessionObject implements IPDITarget {
	private Thread[] noThreads = new Thread[0];
	private Thread[] currentThreads;
	private int currentThreadId;
	private String fEndian = null;
	private final Lock lock = new Lock();
	
	public Target(Session session, BitList tasks) {
		super(session, tasks);
		currentThreads = noThreads;
	}
	public void lockTarget() {
		lock.aquire();
	}
	public void releaseTarget() {
		lock.release();
	}
	public IPDITarget getTarget() {
		return this;
	}	
	public void setCurrentThread(IPDIThread pthread) throws PDIException {
		if (pthread instanceof Thread) {
			setCurrentThread(pthread, true);
		} else {
			throw new PDIException(getTasks(), "Target - Unknown_thread");
		}
	}
	public void setCurrentThread(IPDIThread pthread, boolean doUpdate) throws PDIException {
		if (pthread instanceof Thread) {
			setCurrentThread((Thread)pthread, doUpdate);
		} else {
			throw new PDIException(getTasks(), "Target - Unknown_thread");
		}
	}
	public synchronized void setSupended(boolean state) {
		notifyAll();
	}
	public void setCurrentThread(Thread pthread, boolean doUpdate) throws PDIException {
		int id = pthread.getId();
		if (id == 0) {
			return;
		}
		if (currentThreadId != id) {
			SetThreadSelectRequest request = new SetThreadSelectRequest(session, getTasks(), id);
			session.getEventRequestManager().addEventRequest(request);
			currentThreadId = request.getThreadId(getTasks());
			ProxyDebugStackFrame frame = request.getStackFrame(getTasks());
			if (frame != null) {
				int depth = pthread.getStackFrameCount();
				IPDILocator locator = new Locator(frame.getLocator().getFile(), frame.getLocator().getFunction(), frame.getLocator().getLineNumber(), frame.getLocator().getAddress());
				pthread.currentFrame = new StackFrame(session, pthread, depth - frame.getLevel(), locator, null);
			}

			if (doUpdate) {
				RegisterManager regMgr = session.getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update(getTasks());
				}
				VariableManager varMgr = session.getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(getTasks());
				}
			}
		}
		if (currentThreadId != id) {
			session.getEventManager().fireEvents(new IPDIEvent[] { new DestroyedEvent(new ThreadInfo(session, getTasks(), id, getThread(id))) });
			throw new PDIException(getTasks(), "Target - Cannot switch to thread " + id);
		}
	}
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
			for (int j = 0; j < oldThreads.length; j++) {
				if (currentThreads[i].getId() == oldThreads[j].getId()) {
					oldThreads[j].clearState();
					currentThreads[i] = oldThreads[j];
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
			for (int j=0; j<events.length; j++) {
				int id = ((Integer)pList.get(j)).intValue();
				events[j] = new CreatedEvent(new ThreadInfo(session, getTasks(), id, getThread(id)));
			}
			session.getEventManager().fireEvents(events);
		}
		//Fire destroyed event for old threads
		List<Integer> dList = new ArrayList<Integer>(oldThreads.length);
		for (int i = 0; i < oldThreads.length; i++) {
			boolean found = false;
			for (int j = 0; j < currentThreads.length; j++) {
				if (currentThreads[j].getId() == oldThreads[i].getId()) {
					found = true;
					break;
				}
			}
			if (!found) {
				dList.add(new Integer(oldThreads[i].getId()));
			}
		}
		if (!dList.isEmpty()) {
			IPDIEvent[] events = new IPDIEvent[dList.size()];
			for (int j=0; j<events.length; j++) {
				int id = ((Integer)dList.get(j)).intValue();
				events[j] = new DestroyedEvent(new ThreadInfo(session, getTasks(), id, getThread(id)));
			}
			session.getEventManager().fireEvents(events);
		}
	}	
	public synchronized Thread[] getPThreads() throws PDIException {
		Thread[] pthreads = noThreads;
		lockTarget();
		try {
			GetInfoThreadsRequest request = new GetInfoThreadsRequest(getTasks());
			session.getEventRequestManager().addEventRequest(request);
			String[] ids = request.getThreadIds(getTasks());
			if (ids.length > 0) {
				pthreads = new Thread[ids.length];
				for (int i=0; i<ids.length; i++) {
					int tid = 0;
					try {
						tid = Integer.parseInt(ids[i]);
					} catch (NumberFormatException e) {
						e.printStackTrace();
					}
					pthreads[i] = new Thread(session, this, tid);
				}
			}
			else {
				pthreads = new Thread[] { new Thread(session, this, 0) };
			}
			currentThreadId = pthreads[0].getId();
			if (currentThreadId == 0 && pthreads.length > 1) {
				currentThreadId = pthreads[1].getId();
			}
		}
		finally {
			releaseTarget();
		}
		return pthreads;
	}	
	public synchronized Thread getCurrentThread() throws PDIException {
		Thread[] threads = getThreads();
		for (int i = 0; i < threads.length; i++) {
			Thread pthread = (Thread)threads[i];
			if (pthread.getId() == currentThreadId) {
				return pthread;
			}
		}
		return null;
	}
	public synchronized Thread[] getThreads() throws PDIException {
		if (currentThreads.length == 0) {
			currentThreads = getPThreads();
		}
		return currentThreads;
	}
	public Thread getThread(int tid) {
		Thread th = null;
		if (currentThreads != null) {
			for (int i = 0; i < currentThreads.length; i++) {
				Thread pthread = currentThreads[i];
				if (pthread.getId() == tid) {
					th = pthread;
					break;
				}
			}
		}
		return th;
	}
	public boolean isLittleEndian() throws PDIException {
		//TODO - not implemented yet
		PDebugUtils.println("---- called isLittleEndian");
		if (fEndian == null) {
			//"le" : "be"
		}
		return true;
	}
	public IPDISignal[] getSignals() throws PDIException {
		return session.getSignalManager().getSignals(getTasks());
	}
	public IPDISharedLibrary[] getSharedLibraries() throws PDIException {
		// TODO Auto-generated method stub
		throw new PDIException(getTasks(), "Not implemented yet - Target: getSharedLibraries()");
	}
	public String[] getSourcePaths() throws PDIException {
		return session.getSourceManager().getSourcePaths(getTasks());
	}
	public void setSourcePaths(String[] srcPaths) throws PDIException {
		session.getSourceManager().setSourcePaths(getTasks(), srcPaths);
	}
	public IPDIInstruction[] getInstructions(BigInteger startAddress, BigInteger endAddress) throws PDIException {
		return session.getSourceManager().getInstructions(getTasks(), startAddress, endAddress);
	}
	public IPDIInstruction[] getInstructions(String filename, int linenum, int lines) throws PDIException {
		return session.getSourceManager().getInstructions(getTasks(), filename, linenum, lines);
	}
	public IPDIInstruction[] getInstructions(String filename, int linenum) throws PDIException {
		return session.getSourceManager().getInstructions(getTasks(), filename, linenum);
	}
	public IPDIMixedInstruction[] getMixedInstructions(BigInteger startAddress, BigInteger endAddress) throws PDIException {
		return session.getSourceManager().getMixedInstructions(getTasks(), startAddress, endAddress);
	}
	public IPDIMixedInstruction[] getMixedInstructions(String filename, int linenum, int lines) throws PDIException {
		return session.getSourceManager().getMixedInstructions(getTasks(), filename, linenum, lines);
	}
	public IPDIMixedInstruction[] getMixedInstructions(String filename, int linenum) throws PDIException {
		return session.getSourceManager().getMixedInstructions(getTasks(), filename, linenum);
	}
	public IPDIRegister createRegister(IPDIRegisterDescriptor varDesc) throws PDIException {
		if (varDesc instanceof RegisterDescriptor) {
			return session.getRegisterManager().createRegister((RegisterDescriptor)varDesc);
		}
		return null;
	}
	public IPDIGlobalVariableDescriptor getGlobalVariableDescriptors(String filename, String function, String name) throws PDIException {
		return session.getVariableManager().getGlobalVariableDescriptor(getTasks(), filename, function, name);
	}
	public IPDIRegisterGroup[] getRegisterGroups() throws PDIException {
		return session.getRegisterManager().getRegisterGroups(getTasks());
	}
	public IPDIRuntimeOptions getRuntimeOptions() {
		// TODO Auto-generated method stub
		return null;
	}

	public IPDIGlobalVariable createGlobalVariable(IPDIGlobalVariableDescriptor varDesc) throws PDIException {
		if (varDesc instanceof GlobalVariableDescriptor) {
			VariableManager varMgr = ((Session)getSession()).getVariableManager();
			return varMgr.createGlobalVariable((GlobalVariableDescriptor)varDesc);
		}
		return null;
	}
	public String evaluateExpressionToString(IPDIStackFrame context, String expr) throws PDIException {
		Target target = (Target)context.getTarget();
		Thread currentThread = (Thread)target.getCurrentThread();
		StackFrame currentFrame = currentThread.getCurrentStackFrame();
		target.setCurrentThread((IPDIThread)context.getThread(), false);
		((Thread)context.getThread()).setCurrentStackFrame((StackFrame)context, false);
		try {
			IAIF aif = null;
			Variable var = session.getVariableManager().getVariableByName(tasks, expr);
			if (var == null) {
				aif = session.getExpressionManager().getExpressValue(getTasks(), expr);
			}
			else {
				aif = var.getAIF();
			}
			if (aif != null)
				return aif.getValue().toString();
			throw new PDIException(getTasks(), "Unknonw varibale: " + expr);
		} finally {
			target.setCurrentThread(currentThread, false);
			currentThread.setCurrentStackFrame(currentFrame, false);
		}
	}
}
