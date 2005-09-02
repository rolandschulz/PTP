/*******************************************************************************
 * Copyright (c) 2002, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.debug.external.cdi.model.variable;

import org.eclipse.cdt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;

/**
 * GlobalVariable
 */
public class GlobalVariable extends Variable implements ICDIGlobalVariable {


	/**
	 * @param obj
	 * @param v
	 */
	public GlobalVariable(VariableDescriptor obj) {
		super(obj);
	}

	/**
	 * @param target
	 * @param n
	 * @param q
	 * @param thread
	 * @param stack
	 * @param pos
	 * @param depth
	 * @param v
	 */
	public GlobalVariable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth) {
		super(target, thread, frame, name, fullName, pos, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.Variable#createVariable(org.eclipse.cdt.debug.mi.core.cdi.model.Target, java.lang.String, java.lang.String, org.eclipse.cdt.debug.core.cdi.model.ICDIThread, org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame, int, int, org.eclipse.cdt.debug.mi.core.output.MIVar)
	 */
	protected Variable createVariable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth) {
		return new GlobalVariable(target, thread, frame, name, fullName, pos, depth);
	}
}
