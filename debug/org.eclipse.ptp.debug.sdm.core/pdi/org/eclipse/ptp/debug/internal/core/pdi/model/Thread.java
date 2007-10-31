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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorage;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.internal.core.pdi.RegisterManager;
import org.eclipse.ptp.debug.internal.core.pdi.Session;
import org.eclipse.ptp.debug.internal.core.pdi.SessionObject;
import org.eclipse.ptp.debug.internal.core.pdi.VariableManager;
import org.eclipse.ptp.debug.internal.core.pdi.request.GetStackInfoDepthRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.ListStackFramesRequest;
import org.eclipse.ptp.debug.internal.core.pdi.request.SetCurrentStackFrameRequest;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugLocator;
import org.eclipse.ptp.proxy.debug.client.ProxyDebugStackFrame;

/**
 * @author clement
 *
 */
public class Thread extends SessionObject implements IPDIThread {
	final public static int STACKFRAME_DEFAULT_DEPTH = 200;
	static IPDIStackFrame[] noStack = new IPDIStackFrame[0];
	int id;
	String name;
	StackFrame currentFrame = null;
	List<IPDIStackFrame> currentFrames = null;
	int stackdepth = 0;
	Target target = null;

	public Thread(Session session, Target target, int threadId) {
		this(session, target, threadId, null);
	}
	public Thread(Session session, Target target, int threadId, String threadName) {
		super(session, target.getTasks());
		this.id = threadId;
		this.name = threadName;
		this.target = target;
	}
	public int getId() {
		return id;
	}
	public void clearState() {
		stackdepth = 0;
		currentFrame = null;
		currentFrames = null;
	}
	public String toString() {
		String str = Integer.toString(id);
		if (name != null) {
			str += " " + name;
		}
		return str;
	}
	public void updateState() {
		try {
			getCurrentStackFrame();
		} catch (PDIException e) {}
	}
	public StackFrame getCurrentStackFrame() throws PDIException {
		if (currentFrame == null) {
			IPDIStackFrame[] frames = getStackFrames(0, 0);
			if (frames.length > 0) {
				currentFrame = (StackFrame)frames[0];
			}
		}
		return currentFrame;
	}
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
				ListStackFramesRequest request = new ListStackFramesRequest(getTasks());
				session.getEventRequestManager().addEventRequest(request);
				ProxyDebugStackFrame[] frames = request.getStackFrames(getTasks());
				for (ProxyDebugStackFrame frame : frames) {
					ProxyDebugLocator locator = frame.getLocator();
					currentFrames.add(new StackFrame(session, this, depth - frame.getLevel(), locator.getFile(), locator.getFunction(), locator.getLineNumber(), locator.getAddress(), null));
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
	public int getStackFrameCount() throws PDIException {
		if (stackdepth == 0) {
			Target target = (Target)getTarget();
			IPDIThread currentThread = (IPDIThread)target.getCurrentThread();
			target.lockTarget();
			try {
				target.setCurrentThread(this, false);
				GetStackInfoDepthRequest request = new GetStackInfoDepthRequest(getTasks());
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
				ListStackFramesRequest request = new ListStackFramesRequest(getTasks(), 0, upperBound);
				session.getEventRequestManager().addEventRequest(request);
				ProxyDebugStackFrame[] frames = request.getStackFrames(getTasks());
				for (ProxyDebugStackFrame frame : frames) {
					ProxyDebugLocator locator = frame.getLocator();
					currentFrames.add(new StackFrame(session, this, depth - frame.getLevel(), locator.getFile(), locator.getFunction(), locator.getLineNumber(), locator.getAddress(), null));
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
	public void setCurrentStackFrame(StackFrame stackframe, boolean doUpdate) throws PDIException {
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
			SetCurrentStackFrameRequest request = new SetCurrentStackFrameRequest(session, getTasks(), level);
			session.getEventRequestManager().addEventRequest(request);
			request.waitUntilCompleted(getTasks());
			currentFrame = stackframe;
			if (doUpdate) {
				RegisterManager regMgr = session.getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update(target.getTasks());
				}
				VariableManager varMgr = session.getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(target.getTasks());
				}
			}
		} 
		finally {
			target.releaseTarget();
		}
	}
	public boolean equals(IPDIThread thread) {
		if (thread instanceof Thread) {
			Thread pthread = (Thread) thread;
			return id == pthread.getId();
		}
		return super.equals(thread);
	}
	public IPDIThreadStorage createThreadStorage(IPDIThreadStorageDescriptor varDesc) throws PDIException {
		if (varDesc instanceof ThreadStorageDescriptor) {
			VariableManager varMgr = session.getVariableManager();
			return varMgr.createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		return null;
	}
	public IPDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws PDIException {
		VariableManager varMgr = session.getVariableManager();
		return varMgr.getThreadStorageDescriptors(this);
	}
	public IPDITarget getTarget() {
		return target;
	}
}
