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

package org.eclipse.ptp.debug.external.cdi.model.variable;

import org.eclipse.cdt.debug.core.cdi.model.ICDILocalVariable;
import org.eclipse.ptp.debug.external.cdi.model.StackFrame;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.cdi.model.Thread;

/**
 * LocalVariable
 */
public class LocalVariable extends Variable implements ICDILocalVariable {

	/**
	 * @param obj
	 * @param v
	 */
	public LocalVariable(LocalVariableDescriptor obj) {
		super(obj);
	}

	/**
	 */
	public LocalVariable(Target target, Thread thread, StackFrame frame, String name, String fullName,
			int pos, int depth) {
		super(target, thread, frame, name, fullName, pos, depth);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.mi.core.cdi.model.Variable#createVariable(org.eclipse.cdt.debug.mi.core.cdi.model.Target, java.lang.String, java.lang.String, org.eclipse.cdt.debug.core.cdi.model.ICDIThread, org.eclipse.cdt.debug.core.cdi.model.ICDIStackFrame, int, int, org.eclipse.cdt.debug.mi.core.output.MIVar)
	 */
	protected Variable createVariable(Target target, Thread thread, StackFrame frame, String name,
			String fullName, int pos, int depth) {
		return new LocalVariable(target, thread, frame, name, fullName, pos, depth);
	}

}
