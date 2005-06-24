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

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDISignal;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThreadStorageDescriptor;

public class Thread extends PTPObject implements ICDIThread {
	
	int id;
	String name;

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

	public ICDIStackFrame[] getStackFrames() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.getStackFrames()");
		return null;
	}

	public ICDIStackFrame[] getStackFrames(int fromIndex, int len) throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.getStackFrames()");
		return null;
	}

	public int getStackFrameCount() throws CDIException {
		// Auto-generated method stub
		System.out.println("Thread.getStackFrameCount()");
		return 0;
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
