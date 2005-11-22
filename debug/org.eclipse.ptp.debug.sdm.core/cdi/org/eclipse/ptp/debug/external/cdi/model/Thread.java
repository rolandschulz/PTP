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
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi.model;

import java.util.ArrayList;
import java.util.List;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorage;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.variable.ThreadStorageDescriptor;
import org.eclipse.ptp.debug.external.commands.ListStackFramesCommand;
import org.eclipse.ptp.debug.external.commands.SetCurrentStackFrameCommand;

public class Thread extends PTPObject implements ICDIThread {
	static ICDIStackFrame[] noStack = new ICDIStackFrame[0];
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
	public StackFrame getCurrentStackFrame() throws CDIException {
		if (currentFrame == null) {
			ICDIStackFrame[] frames = getStackFrames(0, 0);
			if (frames.length > 0) {
				currentFrame = (StackFrame)frames[0];
			}
		}
		return currentFrame;
	}
	public ICDIStackFrame[] getStackFrames() throws CDIException {
		int depth = getStackFrameCount();

		// refresh if we have nothing or if we have just a subset get everything.
		if (currentFrames == null || currentFrames.size() < depth) {
			currentFrames = new ArrayList();
			Target target = (Target)getTarget();
			ICDIThread currentThread = target.getCurrentThread();
			target.setCurrentThread(this, false);

			Session session = (Session) target.getSession();			
			try {
				ListStackFramesCommand command = new ListStackFramesCommand(session.createBitList(target.getTargetID()));
				session.getDebugger().postCommand(command);
				ICDIStackFrame[] frames = command.getStackFrames();
				for (int i = 0; i < frames.length; i++) {
					currentFrames.add(frames[i]);
				}
			} finally {
				target.setCurrentThread(currentThread, false);
			}

			// assign the currentFrame if it was not done yet.
			if (currentFrame == null) {
				for (int i = 0; i < currentFrames.size(); i++) {
					ICDIStackFrame stack = (ICDIStackFrame) currentFrames.get(i);
					//TODO -- checking correct?
					if (stack.getLevel() == depth) {
						currentFrame = (StackFrame)stack;
					}
				}
			}
		}
		return (ICDIStackFrame[]) currentFrames.toArray(noStack);
	}
	public int getStackFrameCount() throws CDIException {
		if (stackdepth == 0 || currentFrames == null) {
			currentFrames = new ArrayList();
			final Target target = (Target) getTarget();
			final Session session = (Session) target.getSession();
			
			ListStackFramesCommand command = new ListStackFramesCommand(session.createBitList(target.getTargetID()));
			session.getDebugger().postCommand(command);
			ICDIStackFrame[] frames = command.getStackFrames();
			for (int i = 0; i < frames.length; i++) {
				currentFrames.add(frames[i]);
			}
			stackdepth = currentFrames.size();
			if (frames.length > 0) {
				currentFrame = (StackFrame)frames[0];
			}
		}
		return stackdepth;
	}
	public ICDIStackFrame[] getStackFrames(int low, int high) throws CDIException {
		if (currentFrames == null || currentFrames.size() < high) {
			getStackFrames();
		}
		List list = ((high - low + 1) <= currentFrames.size()) ? currentFrames.subList(low, high + 1) : currentFrames;
		return (ICDIStackFrame[])list.toArray(noStack);
	}
	public void setCurrentStackFrame(StackFrame stackframe, boolean doUpdate) throws CDIException {
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
		Session session = (Session) target.getSession();
		SetCurrentStackFrameCommand command = new SetCurrentStackFrameCommand(session.createBitList(target.getTargetID()), stackframe);
		session.getDebugger().postCommand(command);
		command.waitFinish();
		
		currentFrame = stackframe;
		if (doUpdate) {
			VariableManager varMgr = session.getVariableManager();
			if (varMgr.isAutoUpdate()) {
				varMgr.update(target);
			}
		}
	}	
	public void stepInto() throws CDIException {
		stepInto(1);
	}
	public void stepInto(int count) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepInto(count);
	}
	public void stepIntoInstruction() throws CDIException {
		stepIntoInstruction(1);
	}
	public void stepIntoInstruction(int count) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepIntoInstruction(count);
	}
	public void stepOver() throws CDIException {
		stepOver(1);
	}
	public void stepOver(int count) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepOver(count);
	}
	public void stepOverInstruction() throws CDIException {
		stepOverInstruction(1);
	}
	public void stepOverInstruction(int count) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepOverInstruction(count);
	}
	public void stepReturn() throws CDIException {
		getCurrentStackFrame().stepReturn();
	}
	public void runUntil(ICDILocation location) throws CDIException {
		stepUntil(location);
	}
	public void stepUntil(ICDILocation location) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().stepUntil(location);
	}
	public boolean isSuspended() {
		return getTarget().isSuspended();
	}
	public void suspend() throws CDIException {
		getTarget().suspend();
	}
	public void resume() throws CDIException {
		resume(false);
	}
	public void resume(boolean passSignal) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(passSignal);
	}
	public void resume(ICDILocation location) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(location);
	}
	public void resume(ICDISignal signal) throws CDIException {
		((Target)getTarget()).setCurrentThread(this);
		getTarget().resume(signal);
	}
	public void jump(ICDILocation location) throws CDIException {
		resume(location);
	}
	public void signal() throws CDIException {
		resume(false);
	}
	public void signal(ICDISignal signal) throws CDIException {
		resume(signal);
	}
	public boolean equals(ICDIThread thread) {
		if (thread instanceof Thread) {
			Thread cthread = (Thread) thread;
			return id == cthread.getId();
		}
		return super.equals(thread);
	}
	public ICDIBreakpoint[] getBreakpoints() throws CDIException {
		Target target = (Target)getTarget();
		ICDIBreakpoint[] bps = target.getBreakpoints();
		ArrayList list = new ArrayList(bps.length);
		for (int i = 0; i < bps.length; i++) {
			ICDICondition condition = bps[i].getCondition();
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
		return (ICDIBreakpoint[]) list.toArray(new ICDIBreakpoint[list.size()]);
	}
	public ICDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws CDIException {
		Session session = (Session)getTarget().getSession();
		VariableManager varMgr = session.getVariableManager();
		return varMgr.getThreadStorageDescriptors(this);
	}
	public ICDIThreadStorage createThreadStorage(ICDIThreadStorageDescriptor varDesc) throws CDIException {
		if (varDesc instanceof ThreadStorageDescriptor) {
			Session session = (Session)getTarget().getSession();
			VariableManager varMgr = session.getVariableManager();
			return varMgr.createThreadStorage((ThreadStorageDescriptor)varDesc);
		}
		return null;
	}
}
