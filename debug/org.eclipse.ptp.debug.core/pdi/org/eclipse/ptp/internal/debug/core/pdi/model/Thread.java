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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.debug.core.pdi.IPDILocator;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIRegisterManager;
import org.eclipse.ptp.debug.core.pdi.manager.IPDIVariableManager;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrameDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorage;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.core.pdi.request.IPDIGetStackInfoDepthRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDIListStackFramesRequest;
import org.eclipse.ptp.debug.core.pdi.request.IPDISetCurrentStackFrameRequest;
import org.eclipse.ptp.internal.debug.core.pdi.SessionObject;

/**
 * @author clement
 *
 */
public class Thread extends SessionObject implements IPDIThread {
	private static IPDIStackFrame[] noStack = new IPDIStackFrame[0];
	private int id;
	private String name;
	private IPDIStackFrame currentFrame = null;
	private List<IPDIStackFrame> currentFrames = null;
	private int stackdepth = 0;
	private IPDITarget target = null;

	public Thread(IPDISession session, IPDITarget target, int threadId) {
		this(session, target, threadId, null);
	}
	
	public Thread(IPDISession session, IPDITarget target, int threadId, String threadName) {
		super(session, target.getTasks());
		this.id = threadId;
		this.name = threadName;
		this.target = target;
	}
	
	/**
	 * 
	 */
	public void clearState() {
		stackdepth = 0;
		currentFrame = null;
		currentFrames = null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#createThreadStorage(org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorageDescriptor)
	 */
	public IPDIThreadStorage createThreadStorage(IPDIThreadStorageDescriptor varDesc) throws PDIException {
		if (varDesc instanceof ThreadStorageDescriptor) {
			IPDIVariableManager varMgr = session.getVariableManager();
			return varMgr.createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		return null;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#equals(org.eclipse.ptp.debug.core.pdi.model.IPDIThread)
	 */
	public boolean equals(IPDIThread thread) {
		if (thread instanceof Thread) {
			Thread pthread = (Thread) thread;
			return id == pthread.getId();
		}
		return super.equals(thread);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#getCurrentStackFrame()
	 */
	public IPDIStackFrame getCurrentStackFrame() throws PDIException {
		if (currentFrame == null) {
			IPDIStackFrame[] frames = getStackFrames(0, 0);
			if (frames.length > 0) {
				currentFrame = (StackFrame)frames[0];
			}
		}
		return currentFrame;
	}
	
	/**
	 * @return
	 */
	public int getId() {
		return id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#getStackFrameCount()
	 */
	public int getStackFrameCount() throws PDIException {
		if (stackdepth == 0) {
			Target target = (Target)getTarget();
			IPDIThread currentThread = (IPDIThread)target.getCurrentThread();
			target.lockTarget();
			try {
				target.setCurrentThread(this, false);
				IPDIGetStackInfoDepthRequest request = session.getRequestFactory().getGetStackInfoDepthRequest(getTasks());
				session.getEventRequestManager().addEventRequest(request);
				stackdepth = request.getDepth(getTasks());
			} 
			finally {
				target.setCurrentThread(currentThread, false);
				target.releaseTarget();
			}
		}
		return stackdepth;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#getStackFrames()
	 */
	public IPDIStackFrame[] getStackFrames() throws PDIException {
		int depth = getStackFrameCount();

		// refresh if we have nothing or if we have just a subset get everything.
		if (currentFrames == null || currentFrames.size() < depth) {
			currentFrames = new ArrayList<IPDIStackFrame>();
			Target target = (Target)getTarget();
			IPDIThread currentThread = (IPDIThread)target.getCurrentThread();
			target.lockTarget();
			try {
				target.setCurrentThread(this, false);
				IPDIListStackFramesRequest request = session.getRequestFactory().getListStackFramesRequest(session, getTasks());
				session.getEventRequestManager().addEventRequest(request);
				IPDIStackFrameDescriptor[] frames = request.getStackFrames(getTasks());
				for (IPDIStackFrameDescriptor frame : frames) {
					IPDILocator locator = frame.getLocator();
					currentFrames.add(session.getModelFactory().newStackFrame(session, this, depth - frame.getLevel(), 
							locator.getFile(), locator.getFunction(), locator.getLineNumber(), locator.getAddress()));
				}
			} finally {
				target.setCurrentThread(currentThread, false);
				target.releaseTarget();
			}
			// assign the currentFrame if it was not done yet.
			if (currentFrame == null) {
				for (int i = 0; i < currentFrames.size(); i++) {
					IPDIStackFrame stack = (IPDIStackFrame) currentFrames.get(i);
					if (stack.getLevel() == depth) {
						currentFrame = (StackFrame)stack;
					}
				}
			}
		}
		return (IPDIStackFrame[]) currentFrames.toArray(noStack);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#getStackFrames(int, int)
	 */
	public IPDIStackFrame[] getStackFrames(int low, int high) throws PDIException {
		if (currentFrames == null || currentFrames.size() < high) {
			currentFrames = new ArrayList<IPDIStackFrame>();
			Target target = (Target)getTarget();
			IPDIThread currentThread = target.getCurrentThread();
			target.lockTarget();
			try {
				target.setCurrentThread(this, false);
				int depth = getStackFrameCount();
				int upperBound;
				if (high < depth) {
					upperBound = Math.min(depth, STACKFRAME_DEFAULT_DEPTH);
				}
				else {
					upperBound = depth;
				}
				IPDIListStackFramesRequest request = session.getRequestFactory().getListStackFramesRequest(session,
						getTasks(), 0, upperBound);
				session.getEventRequestManager().addEventRequest(request);
				IPDIStackFrameDescriptor[] frames = request.getStackFrames(getTasks());
				for (IPDIStackFrameDescriptor frame : frames) {
					IPDILocator locator = frame.getLocator();
					currentFrames.add(session.getModelFactory().newStackFrame(session, this, depth - frame.getLevel(), 
							locator.getFile(), locator.getFunction(), locator.getLineNumber(), locator.getAddress()));
				}
			} finally {
				target.setCurrentThread(currentThread, false);
				target.releaseTarget();
			}
			if (currentFrame == null) {
				for (int i=0; i<currentFrames.size(); i++) {
					StackFrame f = (StackFrame)currentFrames.get(i);
					if (f.getLevel() == 0) {
						currentFrame = f;
					}
				}
			}
		}
		List<IPDIStackFrame> list = ((high - low + 1) <= currentFrames.size()) ? currentFrames.subList(low, high + 1) : currentFrames;
		return (IPDIStackFrame[])list.toArray(noStack);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#getTarget()
	 */
	public IPDITarget getTarget() {
		return target;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#getThreadStorageDescriptors()
	 */
	public IPDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws PDIException {
		IPDIVariableManager varMgr = session.getVariableManager();
		return varMgr.getThreadStorageDescriptors(this);
	}
	
	public void setCurrentStackFrame(IPDIStackFrame stackframe) throws PDIException {
		currentFrame = stackframe;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIThread#setCurrentStackFrame(org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, boolean)
	 */
	public void setCurrentStackFrame(IPDIStackFrame stackframe, boolean doUpdate) throws PDIException {
		int frameLevel = 0;
		if (stackframe != null) {
			frameLevel = stackframe.getLevel();
		}
		// Check to see if we are already at this level
		if (currentFrame != null && currentFrame.getLevel() == frameLevel) {
			if (stackframe != null) {
				Thread aThread = (Thread)stackframe.getThread();
				if (aThread != null && aThread.getId() == getId()) {
					return;
				}
			}
		}
		Target target = (Target)getTarget();
		int level = getStackFrameCount() - frameLevel;
		target.lockTarget();
		try {
			target.setCurrentThread(this, doUpdate);
			IPDISetCurrentStackFrameRequest request = session.getRequestFactory().getSetCurrentStackFrameRequest(getTasks(), level);
			session.getEventRequestManager().addEventRequest(request);
			request.waitUntilCompleted(getTasks());
			currentFrame = stackframe;
			if (doUpdate) {
				IPDIRegisterManager regMgr = session.getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update(target.getTasks());
				}
				IPDIVariableManager varMgr = session.getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(target.getTasks());
				}
			}
		} 
		finally {
			target.releaseTarget();
		}
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = Integer.toString(id);
		if (name != null) {
			str += " " + name; //$NON-NLS-1$
		}
		return str;
	}
	
	/**
	 * 
	 */
	public void updateState() {
		try {
			getCurrentStackFrame();
		} catch (PDIException e) {}
	}
}
