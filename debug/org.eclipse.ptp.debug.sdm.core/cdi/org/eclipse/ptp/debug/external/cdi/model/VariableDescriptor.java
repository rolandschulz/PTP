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
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.ptp.debug.external.cdi.Session;
import org.eclipse.ptp.debug.external.cdi.VariableManager;
import org.eclipse.ptp.debug.external.cdi.model.type.IncompleteType;
import org.eclipse.ptp.debug.external.cdi.model.type.IntType;

/**
 */
public abstract class VariableDescriptor extends PTPObject implements ICDIVariableDescriptor {
	
	String fName;
	String fFullName;
	StackFrame fStackFrame;
	Thread fThread;
	int position;
	int stackdepth;

	public VariableDescriptor(VariableDescriptor desc) {
		super((Target)desc.getTarget());
		fName = desc.getName();
		fFullName = desc.fFullName;
		try {
			fStackFrame = (StackFrame)desc.getStackFrame();
			fThread = (Thread)desc.getThread();
		} catch (CDIException e) {
		}
		position = desc.getPosition();
		stackdepth = desc.getStackDepth();
	}
	
	public VariableDescriptor(Target target, Thread thread, StackFrame stack, String n, String fn, int pos, int depth) {
		super(target);
		fName = n;
		fFullName = fn;
		fStackFrame = stack;
		fThread = thread;
		position = pos;
		stackdepth = depth;
	}

	public ICDIThread getThread() throws CDIException {
		return fThread;
	}
	
	public ICDIStackFrame getStackFrame() throws CDIException {
		return fStackFrame;
	}
	
	public String getName() {
		System.out.println("VariableDescriptor.getName()");
		return fName;
	}
	
	public String getTypeName() throws CDIException {
		System.out.println("VariableDescriptor.getTypeName()");
		// FIXME Donny
		return "int";
	}
	
	public String getQualifiedName() throws CDIException {
		System.out.println("VariableDescriptor.getQualifiedName()");
		return fName;
	}
	
	public ICDIType getType() throws CDIException {
		System.out.println("VariableDescriptor.getQualifiedName()");
		String typeName = getTypeName();
		Target target = (Target) getTarget();
		
		if (typeName.equals("int")) {
			return new IntType(target, typeName);
		}
		
		return new IncompleteType((Target) getTarget(), getTypeName());
	}
	
	public int sizeof() throws CDIException {
		System.out.println("VariableDescriptor.sizeof()");
		return 0;
	}

	public int getPosition() {
		System.out.println("VariableDescriptor.getPosition()");
		return 0;
	}
	
	public int getStackDepth() {
		System.out.println("VariableDescriptor.getStackDepth()");
		return 0;
	}
	

	
	public boolean equals(ICDIVariableDescriptor varDesc) {
		System.out.println("VariableDescriptor.equals()");
		if (varDesc instanceof VariableDescriptor) {
			VariableDescriptor desc = (VariableDescriptor) varDesc;
			if (desc.getName().equals(getName())) {
				// Check the threads
				ICDIThread varThread = null;
				ICDIThread ourThread = null;
				try {
					varThread = desc.getThread();
					ourThread = getThread();
				} catch (CDIException e) {
					// ignore
				}
				if ((ourThread == null && varThread == null) ||
						(varThread != null && ourThread != null && varThread.equals(ourThread))) {
					// check the stackFrames
					ICDIStackFrame varFrame = null;
					ICDIStackFrame ourFrame = null;
					try {
						varFrame = desc.getStackFrame();
						ourFrame = getStackFrame();
					} catch (CDIException e) {
						// ignore
					}
					if (ourFrame == null && varFrame == null) {
						return true;
					} else if (varFrame != null && ourFrame != null && varFrame.equals(ourFrame)) {
						if (desc.getStackDepth() == getStackDepth()) {
							if (desc.getPosition() == getPosition()) {
								return true;
							}
						}
					}
				}
				return false;
			}
		}
		return super.equals(varDesc);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsArray(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor, int, int)
	 */
	public ICDIVariableDescriptor getVariableDescriptorAsArray(int start, int length) throws CDIException {
		System.out.println("VariableDescriptor.getVariableDescriptorAsArray()");
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		//return mgr.getVariableDescriptorAsArray(this, start, length);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsType(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor, java.lang.String)
	 */
	public ICDIVariableDescriptor getVariableDescriptorAsType(String type) throws CDIException {
		System.out.println("VariableDescriptor.getVariableDescriptorAsType()");
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		//return mgr.getVariableDescriptorAsType(this, type);
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#createVariable()
	 */
	public ICDIVariable createVariable() throws CDIException {
		System.out.println("VariableDescriptor.createVariable()");
		Session session = (Session)getTarget().getSession();
		VariableManager mgr = session.getVariableManager();
		return mgr.createVariable(this);
	}
}
