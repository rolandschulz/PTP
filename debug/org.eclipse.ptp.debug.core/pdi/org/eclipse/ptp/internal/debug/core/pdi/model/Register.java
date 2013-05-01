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
package org.eclipse.ptp.internal.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISession;
import org.eclipse.ptp.debug.core.pdi.PDIException;
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegister;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;
import org.eclipse.ptp.debug.core.pdi.model.aif.IAIF;

/**
 * @author clement
 *
 */
public class Register extends Variable implements IPDIRegister {
	public Register(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame frame, String n, String q, int pos, int depth, String varid) {
		super(session, tasks, thread, frame, n, q, pos, depth, varid);
	}

	public Register(IPDISession session, RegisterDescriptor obj, String varid) {
		super(session, obj, varid);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.Variable#dispose()
	 */
	public void dispose() throws PDIException {
		session.getRegisterManager().destroyRegister(this);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.model.IPDIRegister#getAIF(org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame)
	 */
	public IAIF getAIF(IPDIStackFrame context) throws PDIException {
		IPDIVariable var = session.getRegisterManager().createShadowRegister(this, (StackFrame)context, getQualifiedName());
		return var.getAIF();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.VariableDescriptor#getFullName()
	 */
	public String getFullName() {
		if (fFullName == null) {
			String n = getName();
			if (!n.startsWith("$")) { //$NON-NLS-1$
				fFullName = "$" + n; //$NON-NLS-1$
			} else {
				fFullName = n;
			}
		}
		return fFullName;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.Variable#createVariable(org.eclipse.ptp.internal.debug.core.pdi.Session, org.eclipse.ptp.core.util.TaskSet, org.eclipse.ptp.internal.debug.core.pdi.model.Thread, org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String, java.lang.String, int, int, java.lang.String)
	 */
	protected IPDIVariable createVariable(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame frame, String name, String fullName, int pos, int depth, String varid) {
		return new Register(session, tasks, thread, frame, name, fullName, pos, depth, varid);
	}
}
