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

import org.eclipse.cldt.debug.core.cdi.model.ICDIThreadStorageDescriptor;

/**
 * ThreadStorageDescriptor
 */
public class ThreadStorageDescriptor extends VariableDescriptor implements
		ICDIThreadStorageDescriptor {

	/**
	 * @param target
	 * @param thread
	 * @param stack
	 * @param n
	 * @param fn
	 * @param pos
	 * @param depth
	 */
	public ThreadStorageDescriptor(Target target, Thread thread,
			StackFrame stack, String n, String fn, int pos, int depth) {
		super(target, thread, stack, n, fn, pos, depth);
	}

}
