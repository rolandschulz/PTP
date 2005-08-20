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
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;
import org.eclipse.ptp.debug.external.IDebugger;
import org.eclipse.ptp.debug.external.cdi.Session;

public class Thread extends PTPObject implements ICDIThread {
	static ICDIStackFrame[] noStack = new ICDIStackFrame[0];
	int id;
	String name;
	StackFrame currentFrame;
	List currentFrames;
	int stackdepth = 0;
	int procNumber;
	
	final public static int STACKFRAME_DEFAULT_DEPTH = 200;

	public Thread(Target target, int threadId) {
		this(target, threadId, null);
	}

	public Thread(Target target, int threadId, String threadName) {
		super(target);
		procNumber = target.getTargetId();
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
		// get the frames depth
		int depth = getStackFrameCount();

		// refresh if we have nothing or if we have just a subset get everything.
		if (currentFrames == null || currentFrames.size() < depth) {
			currentFrames = new ArrayList();
			
			Target target = (Target) getTarget();
			Session session = (Session) target.getSession();
			IDebugger debugger = session.getDebugger();
			DebugProcessSet newSet = new DebugProcessSet(session, "", target.getTargetId());
			ICDIStackFrame[] frames = debugger.listStackFrames(newSet);
			
			for (int i = 0; i < frames.length; i++) {
				currentFrames.add(frames[i]);
			}
			
			// assign the currentFrame if it was not done yet.
			if (currentFrame == null) {
				for (int i = 0; i < currentFrames.size(); i++) {
					ICDIStackFrame stack = (ICDIStackFrame) currentFrames.get(i);
					
					/* For a thread with 1 stack, the level of the stack is 0, however
					 * the depth is 1
					 */
					if (stack.getLevel() + 1 == depth) {
						currentFrame = (StackFrame)stack;
					}
				}
			}

			//target.setCurrentThread(currentThread, false);
		}
		return (ICDIStackFrame[]) currentFrames.toArray(noStack);
	}

	public ICDIStackFrame[] getStackFrames(int fromIndex, int len) throws CDIException {
		getStackFrames();
		List list = currentFrames.subList(fromIndex, len);
		return (ICDIStackFrame[]) list.toArray(noStack);
	}

	public int getStackFrameCount() throws CDIException {
		if (stackdepth == 0) {
			Target target = (Target) getTarget();
			Session session = (Session) target.getSession();
			IDebugger debugger = session.getDebugger();
			DebugProcessSet newSet = new DebugProcessSet(session, "", target.getTargetId());
			ICDIStackFrame[] frames = debugger.listStackFrames(newSet);
			stackdepth = frames.length;
		}
		return stackdepth;
	}

	public ICDIThreadStorageDescriptor[] getThreadStorageDescriptors() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.getThreadStorageDescriptors()");
		return null;
	}

	public void resume() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.resume1()");
	}

	public void stepOver() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepOver()");
	}

	public void stepInto() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepInto()");
	}
	
	public String toString() {
		String str = Integer.toString(procNumber);
		str += " - " + Integer.toString(id);
		if (name != null) {
			str += " " + name; //$NON-NLS-1$
		}
		return str;
	}

	public void stepOverInstruction() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepOverInstruction()");
	}

	public void stepIntoInstruction() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepIntoInstruction()");
	}

	public void stepReturn() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepReturn()");
	}

	public void runUntil(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.runUntil()");
	}

	public void jump(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.jump()");
	}

	public void signal() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.signal()");
	}

	public void signal(ICDISignal signal) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.signal()");
	}

	public boolean equals(ICDIThread thread) {
		// Auto-generated method stub
		System.out.println("Thread.equals()");
		if (thread instanceof Thread) {
			Thread cthread = (Thread) thread;
			return id == cthread.getId();
		}
		return super.equals(thread);
	}

	public void stepOver(int count) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepOver()");
	}

	public void stepOverInstruction(int count) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepOverInstruction()");
	}

	public void stepInto(int count) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepInto()");
	}

	public void stepIntoInstruction(int count) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepIntoInstruction()");
	}

	public void stepUntil(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.stepUntil()");
	}

	public void resume(boolean passSignal) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.resume2()");
		
		Target target = (Target) getTarget();
		Session session = (Session) target.getSession();
		IDebugger debugger = session.getDebugger();
		DebugProcessSet newSet = new DebugProcessSet(session, "", target.getTargetId());
		debugger.go(newSet);
	}

	public void resume(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.resume3()");
	}

	public void resume(ICDISignal signal) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.resume4()");
	}

	public void suspend() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.suspend()");
	}

	public boolean isSuspended() {
		// Auto-generated method stub
		System.out.println("Thread.isSuspended()");
		return false;
	}
}
