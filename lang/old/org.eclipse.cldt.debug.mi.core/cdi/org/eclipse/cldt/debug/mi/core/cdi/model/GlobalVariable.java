/**********************************************************************
 * Copyright (c) 2002,2003,2004 QNX Software Systems and others.
 * All rights reserved.   This program and the accompanying materials
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors: 
 * QNX Software Systems - Initial API and implementation
 ***********************************************************************/

package org.eclipse.cldt.debug.mi.core.cdi.model;

import org.eclipse.cldt.debug.core.cdi.model.ICDIGlobalVariable;
import org.eclipse.cldt.debug.mi.core.output.MIVar;

/**
 * GlobalVariable
 */
public class GlobalVariable extends Variable implements ICDIGlobalVariable {


	/**
	 * @param obj
	 * @param v
	 */
	public GlobalVariable(VariableDescriptor obj, MIVar v) {
		super(obj, v);
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
	public GlobalVariable(Target target, Thread thread, StackFrame frame, String n, String q, int pos, int depth, MIVar v) {
		super(target, thread, frame, n, q, pos, depth, v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.Variable#createVariable(org.eclipse.cdt.debug.mi.core.cdi.model.Target, java.lang.String, java.lang.String, org.eclipse.cdt.debug.core.cdi.model.ICDIThread, org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame, int, int, org.eclipse.cdt.debug.mi.core.output.MIVar)
	 */
	protected Variable createVariable(Target target, Thread thread, StackFrame frame, String name, String fullName, int pos, int depth, MIVar miVar) {
		return new GlobalVariable(target, thread, frame, name, fullName, pos, depth, miVar);
	}
}
