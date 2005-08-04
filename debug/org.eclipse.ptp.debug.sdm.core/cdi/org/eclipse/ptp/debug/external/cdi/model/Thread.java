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
import org.eclipse.ptp.debug.external.simulator.SimProcess;
import org.eclipse.ptp.debug.external.simulator.SimStackFrame;

public class Thread extends PTPObject implements ICDIThread {
	static ICDIStackFrame[] noStack = new ICDIStackFrame[0];
	int id;
	String name;
	//StackFrame currentFrame;
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

	public ICDIStackFrame[] getStackFrames() throws CDIException {
		// get the frames depth
		int depth = getStackFrameCount();

		// refresh if we have nothing or if we have just a subset get everything.
		if (currentFrames == null || currentFrames.size() < depth) {
			currentFrames = new ArrayList();
			Target target = (Target) getTarget();
			SimProcess proc = (SimProcess) target.getProcess();
			SimStackFrame[] frames = proc.getThread(id).getStackFrames();
			for (int i = 0; i < frames.length; i++) {
				currentFrames.add(new StackFrame(this, frames[i], depth - frames[i].getLevel()));
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
			SimProcess proc = (SimProcess) target.getProcess();
			stackdepth = proc.getThread(id).getStackFrameCount();
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
		System.out.println("Thread.resume()");
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

	public boolean equals(ICDIThread thead) {
		// Auto-generated method stub
		System.out.println("Thread.equals()");
		return false;
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
		System.out.println("Thread.resume()");
	}

	public void resume(ICDILocation location) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.resume()");
	}

	public void resume(ICDISignal signal) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.resume()");
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
