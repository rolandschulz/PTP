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
package org.eclipse.ptp.debug.external.core.cdi.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocation;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIStackFrame;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThread;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThreadStorage;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.cdi.VariableManager;
import org.eclipse.ptp.debug.external.core.cdi.model.variable.ThreadStorageDescriptor;
import org.eclipse.ptp.debug.external.core.commands.GetStackInfoDepthCommand;
import org.eclipse.ptp.debug.external.core.commands.ListStackFramesCommand;
import org.eclipse.ptp.debug.external.core.commands.SetCurrentStackFrameCommand;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugStackframe;

/**
 * @author Clement chu
 *
 */
public class Thread extends PObject implements IPCDIThread {
	static IPCDIStackFrame[] noStack = new IPCDIStackFrame[0];
	int id;
	String name;
	StackFrame currentFrame;
	List currentFrames;
	int stackdepth = 0;

	final public static int STACKFRAME_DEFAULT_DEPTH = 200;
	
	public Thread(Target target, int threadId) {
		this(target, threadId, null);
	}
	public Thread(Target target, int threadId, String threadName) {
		super(target);
		id = threadId;
		name = threadName;
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
		} catch (PCDIException e) {}
	}
	public StackFrame getCurrentStackFrame() throws PCDIException {
		if (currentFrame == null) {
			IPCDIStackFrame[] frames = getStackFrames(0, 0);
			if (frames.length > 0) {
				currentFrame = (StackFrame)frames[0];
			}
		}
		return currentFrame;
	}
	public IPCDIStackFrame[] getStackFrames() throws PCDIException {
		int depth = getStackFrameCount();

		// refresh if we have nothing or if we have just a subset get everything.
		if (currentFrames == null || currentFrames.size() < depth) {
			currentFrames = new ArrayList();
			Target target = (Target)getTarget();
			IPCDIThread currentThread = (IPCDIThread)target.getCurrentThread();
			target.setCurrentThread(this, false);
			ListStackFramesCommand command = new ListStackFramesCommand(target.getTask());
			try {
				target.getDebugger().postCommand(command);
				ProxyDebugStackframe[] frames = command.getStackFrames();
				for (int i = 0; i < frames.length; i++) {
					currentFrames.add(new StackFrame(this, depth - frames[i].getLevel(), frames[i].getLocator(), null));
				}
			} finally {
				target.setCurrentThread(currentThread, false);
			}

			// assign the currentFrame if it was not done yet.
			if (currentFrame == null) {
				for (int i = 0; i < currentFrames.size(); i++) {
					IPCDIStackFrame stack = (IPCDIStackFrame) currentFrames.get(i);
					//TODO -- checking correct?
					if (stack.getLevel() == depth) {
						currentFrame = (StackFrame)stack;
					}
				}
			}
		}
		return (IPCDIStackFrame[]) currentFrames.toArray(noStack);
	}
	public int getStackFrameCount() throws PCDIException {
		if (stackdepth == 0) {
			Target target = (Target)getTarget();
			IPCDIThread currentThread = (IPCDIThread)target.getCurrentThread();
			target.setCurrentThread(this, false);

			GetStackInfoDepthCommand command = new GetStackInfoDepthCommand(target.getTask());
			try {
				target.getDebugger().postCommand(command);
				stackdepth = command.getDepth();
			} catch (PCDIException e) {
				// First try fails, retry. gdb patches up the corrupt frame
				// so retry should give us a frame count that is safe.
				command = new GetStackInfoDepthCommand(target.getTask());
				target.getDebugger().postCommand(command);
				stackdepth = command.getDepth();
				if (stackdepth > 0) {
					stackdepth--;
				}
			} finally {
				target.setCurrentThread(currentThread, false);
			}
		}
		return stackdepth;
	}
	public IPCDIStackFrame[] getStackFrames(int low, int high) throws PCDIException {
		if (currentFrames == null || currentFrames.size() < high) {
			currentFrames = new ArrayList();
			Target target = (Target)getTarget();
			IPCDIThread currentThread = target.getCurrentThread();
			target.setCurrentThread(this, false);
			try {
				int depth = getStackFrameCount();
				int upperBound;
				if (high < depth) {
					upperBound = Math.min(depth, STACKFRAME_DEFAULT_DEPTH);
				}
				else {
					upperBound = depth;
				}

				ListStackFramesCommand command = new ListStackFramesCommand(target.getTask(), 0, upperBound);
				target.getDebugger().postCommand(command);
				ProxyDebugStackframe[] frames = command.getStackFrames();
				for (int i = 0; i < frames.length; i++) {
					currentFrames.add(new StackFrame(this, depth - frames[i].getLevel(), frames[i].getLocator(), null));
				}
			} finally {
				target.setCurrentThread(currentThread, false);
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
		List list = ((high - low + 1) <= currentFrames.size()) ? currentFrames.subList(low, high + 1) : currentFrames;
		return (IPCDIStackFrame[])list.toArray(noStack);
		/* old design
		if (currentFrames == null || currentFrames.size() < high) {
			getStackFrames();
		}
		List list = ((high - low + 1) <= currentFrames.size()) ? currentFrames.subList(low, high + 1) : currentFrames;
		return (IPCDIStackFrame[])list.toArray(noStack);
		*/
	}
	public void setCurrentStackFrame(StackFrame stackframe, boolean doUpdate) throws PCDIException {
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
		SetCurrentStackFrameCommand command = new SetCurrentStackFrameCommand(target.getTask(), level);
		target.getDebugger().postCommand(command);
		if (command.isWaitForReturn()) {
			currentFrame = stackframe;
			if (doUpdate) {
				/*
				RegisterManager regMgr = ((Session)target.getSession()).getRegisterManager();
				if (regMgr.isAutoUpdate()) {
					regMgr.update(target);
				}
				*/
				VariableManager varMgr = ((Session)target.getSession()).getVariableManager();
				if (varMgr.isAutoUpdate()) {
					varMgr.update(target);
				}
			}
		}
	}	
	public void stepInto() throws PCDIException {
		stepInto(1);
	}
	public void stepInto(int count) throws PCDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepInto(count);
	}
	public void stepIntoInstruction() throws PCDIException {
		stepIntoInstruction(1);
	}
	public void stepIntoInstruction(int count) throws PCDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepIntoInstruction(count);
	}
	public void stepOver() throws PCDIException {
		stepOver(1);
	}
	public void stepOver(int count) throws PCDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepOver(count);
	}
	public void stepOverInstruction() throws PCDIException {
		stepOverInstruction(1);
	}
	public void stepOverInstruction(int count) throws PCDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepOverInstruction(count);
	}
	public void stepReturn() throws PCDIException {
		getCurrentStackFrame().stepReturn();
	}
	public void runUntil(IPCDILocation location) throws PCDIException {
		stepUntil(location);
	}
	public void stepUntil(IPCDILocation location) throws PCDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepUntil(location);
	}
	public boolean isSuspended() {
		return getTarget().isSuspended();
	}
	public void suspend() throws PCDIException {
		getTarget().suspend();
	}
	public void resume() throws PCDIException {
		resume(false);
	}
	public void resume(boolean passSignal) throws PCDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(passSignal);
	}
	public void resume(IPCDILocation location) throws PCDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(location);
	}
	public void resume(IPCDISignal signal) throws PCDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(signal);
	}
	public void jump(IPCDILocation location) throws PCDIException {
		resume(location);
	}
	public void signal() throws PCDIException {
		resume(false);
	}
	public void signal(IPCDISignal signal) throws PCDIException {
		resume(signal);
	}
	public boolean equals(IPCDIThread thread) {
		if (thread instanceof Thread) {
			Thread cthread = (Thread) thread;
			return id == cthread.getId();
		}
		return super.equals(thread);
	}
	/*
	public IPCDIBreakpoint[] getBreakpoints() throws PCDIException {
		Target target = (Target)getTarget();
		IPCDIBreakpoint[] bps = target.getBreakpoints();
		ArrayList list = new ArrayList(bps.length);
		for (int i = 0; i < bps.length; i++) {
			IPCDICondition condition = bps[i].getCondition();
			if (condition == null) {
				continue;
			}
			String[] threadIds = condition.getThreadIds();
			for (int j = 0; j < threadIds.length; j++) {
				int tid = 0;
				try {
					tid = Integer.parseInt(threadIds[j]);
				} catch (NumberFormatException e) {
					//
				}
				if (tid == getId()) {
					list.add(bps[i]);
				}
			}
		}
		return (IPCDIBreakpoint[]) list.toArray(new IPCDIBreakpoint[list.size()]);
	}
	*/
	public IPCDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws PCDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager varMgr = session.getVariableManager();
		return varMgr.getThreadStorageDescriptors(this);
	}
	public IPCDIThreadStorage createThreadStorage(IPCDIThreadStorageDescriptor varDesc) throws PCDIException {
		if (varDesc instanceof ThreadStorageDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager varMgr = session.getVariableManager();
			return varMgr.createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		return null;
	}
}
