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
package org.eclipse.ptp.debug.core.cdi.model;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariable;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.type.ICDIType;
import org.eclipse.ptp.debug.core.cdi.CDIResources;

/**
 */
public abstract class VariableDescriptor extends PTPObject implements ICDIVariableDescriptor {

	// Casting info.
	String[] castingTypes;
	int castingIndex;
	int castingLength;

	String fName;
	int position;
	StackFrame fStackFrame;
	Thread fThread;
	int stackdepth;

	String qualifiedName = null;
	String fFullName = null;
	ICDIType fType = null;
	String fTypename = null;
	String sizeof = null;

	/**
	 * Copy constructor.
	 * @param desc
	 */
	public VariableDescriptor(VariableDescriptor desc) {
		super((Target)desc.getTarget());
		fName = desc.getName();
		fFullName = desc.fFullName;
		sizeof = desc.sizeof;
		fType = desc.fType;
		try {
			fStackFrame = (StackFrame)desc.getStackFrame();
			fThread = (Thread)desc.getThread();
		} catch (CDIException e) {
		}
		position = desc.getPosition();
		stackdepth = desc.getStackDepth();
		castingIndex = desc.getCastingArrayStart();
		castingLength = desc.getCastingArrayEnd();
		castingTypes = desc.getCastingTypes();
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

	public int getPosition() {
		return position;
	}

	public int getStackDepth() {
		return stackdepth;
	}

	public void setCastingArrayStart(int start) {
		castingIndex = start;
	}
	public int getCastingArrayStart() {
		return castingIndex;
	}

	public void setCastingArrayEnd(int end) {
		castingLength = end;
	}
	public int getCastingArrayEnd() {
		return castingLength;
	}

	public void setCastingTypes(String[] t) {
		castingTypes = t;
	}
	public String[] getCastingTypes() {
		return castingTypes;
	}

	/**
	 * If the variable was a cast encode the string appropriately for GDB.
	 * For example castin to an array is of 2 elements:
	 *  (foo)@2
	 * @return
	 */
	public String encodeVariable() {
		String fn = getFullName();
		return fn;
	}

	public String getFullName() {
		if (fFullName == null) {
			fFullName = getName();
		}
		return fFullName;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.ICDIVariableDescriptor#getName()
	 */
	public String getName() {
		return fName;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariable#getType()
	 */
	public ICDIType getType() throws CDIException {
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#sizeof()
	 */
	public int sizeof() throws CDIException {
		return 0;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getStackFrame()
	 */
	public ICDIStackFrame getStackFrame() throws CDIException {
		return fStackFrame;
	}

	public ICDIThread getThread() throws CDIException {
		return fThread;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getTypeName()
	 */
	public String getTypeName() throws CDIException {
		if (fTypename == null) {
			fTypename = "test type";
		}
		return fTypename;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getQualifiedName()
	 */
	public String getQualifiedName() throws CDIException {
		if (qualifiedName == null) {
			qualifiedName = encodeVariable();
		}
		return qualifiedName;
	}

	public static boolean equalsCasting(VariableDescriptor var1, VariableDescriptor var2) {
		String[] castings1 = var1.getCastingTypes();
		String[] castings2 = var2.getCastingTypes();
		if (castings1 == null && castings2 == null) {
			return true;
		} else if (castings1 != null && castings2 != null && castings1.length == castings2.length) {
			for (int i = 0; i < castings1.length; ++i) {
				if (!castings1[i].equals(castings2[i])) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#equals(ICDIVariableDescriptor)
	 */
	public boolean equals(ICDIVariableDescriptor varDesc) {
		if (varDesc instanceof VariableDescriptor) {
			VariableDescriptor desc = (VariableDescriptor) varDesc;
			if (desc.getName().equals(getName())
				&& desc.getCastingArrayStart() == getCastingArrayStart()
				&& desc.getCastingArrayEnd() == getCastingArrayEnd()
				&& equalsCasting(desc, this)) {

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
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#getVariableDescriptorAsType(org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor, java.lang.String)
	 */
	public ICDIVariableDescriptor getVariableDescriptorAsType(String type) throws CDIException {
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor#createVariable()
	 */
	public ICDIVariable createVariable() throws CDIException {
		throw new CDIException(CDIResources.getString("cdi.Common.Not_implemented")); //$NON-NLS-1$
	}

}
