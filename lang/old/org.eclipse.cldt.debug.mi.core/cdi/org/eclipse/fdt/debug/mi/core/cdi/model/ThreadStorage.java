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

package org.eclipse.fdt.debug.mi.core.cdi.model;

import org.eclipse.fdt.debug.core.cdi.model.ICDIThreadStorage;
import org.eclipse.fdt.debug.mi.core.output.MIVar;

/**
 * ThreadStorage
 */
public class ThreadStorage extends Variable implements ICDIThreadStorage {

	/**
	 * @param obj
	 * @param v
	 */
	public ThreadStorage(VariableDescriptor obj, MIVar v) {
		super(obj, v);
	}

	/**
	 * @param target
	 * @param thread
	 * @param frame
	 * @param n
	 * @param q
	 * @param pos
	 * @param depth
	 * @param v
	 */
	public ThreadStorage(Target target, Thread thread, StackFrame frame,
			String n, String q, int pos, int depth, MIVar v) {
		super(target, thread, frame, n, q, pos, depth, v);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.fdt.debug.mi.core.cdi.model.Variable#createVariable(org.eclipse.fdt.debug.mi.core.cdi.model.Target, org.eclipse.fdt.debug.mi.core.cdi.model.Thread, org.eclipse.fdt.debug.mi.core.cdi.model.StackFrame, java.lang.String, java.lang.String, int, int, org.eclipse.fdt.debug.mi.core.output.MIVar)
	 */
	protected Variable createVariable(Target target, Thread thread,
			StackFrame frame, String name, String fullName, int pos, int depth,
			MIVar miVar) {
		return null;
	}

}
