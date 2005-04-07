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

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIArgumentDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariableDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIValue;
import org.eclipse.ptp.debug.core.cdi.Location;

/**
 */
public class StackFrame extends PTPObject implements ICDIStackFrame {

	Thread cthread;
	int level;
	ICDIArgumentDescriptor[] argDescs;
	ICDILocalVariableDescriptor[] localDescs;
	Location fLocation;

	public StackFrame(Thread thread, int l) {
		super((Target)thread.getTarget());
		cthread = thread;
		level = l;
		argDescs = new ICDIArgumentDescriptor[0];
		localDescs = new ICDILocalVariableDescriptor[0];
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getThread()
	 */
	public ICDIThread getThread() {
		return cthread;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getArgumentDescriptors()
	 */
	public ICDIArgumentDescriptor[] getArgumentDescriptors() throws CDIException {
		return argDescs;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocalVariableDescriptors()
	 */
	public ICDILocalVariableDescriptor[] getLocalVariableDescriptors() throws CDIException {
		return localDescs;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLocation()
	 */
	public ICDILocation getLocation() {
		BigInteger addr = BigInteger.ZERO;
		return new Location("", "", 0, addr); //$NON-NLS-1$ //$NON-NLS-2$
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#getLevel()
	 */
	public int getLevel() {
		return level;
	}

	/**
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame#equals(ICDIStackFrame)
	 */
	public boolean equals(ICDIStackFrame stackframe) {
		if (stackframe instanceof StackFrame) {
			StackFrame stack = (StackFrame)stackframe;
			return  cthread != null &&
				cthread.equals(stack.getThread()) &&
				getLevel() == stack.getLevel() &&
				getLocation().equals(stack.getLocation());
		}
		return super.equals(stackframe);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStepReturn#stepReturn()
	 */
	public void stepReturn() throws CDIException {
		finish();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIExecuteStepReturn#stepReturn(org.eclipse.cdt.debug.core.cdi.model.ICDIValue)
	 */
	public void stepReturn(ICDIValue value) throws CDIException {
		execReturn(value.toString());
	}

	/**
	 */
	protected void finish() throws CDIException {
	}

	/**
	 */
	protected void execReturn(String value) throws CDIException {
	}

}
