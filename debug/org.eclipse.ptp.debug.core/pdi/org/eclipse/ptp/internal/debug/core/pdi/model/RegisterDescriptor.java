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
import org.eclipse.ptp.debug.core.pdi.model.IPDIRegisterDescriptor;
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;

/**
 * @author clement
 *
 */
public class RegisterDescriptor extends VariableDescriptor implements IPDIRegisterDescriptor {
	public RegisterDescriptor(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame frame, String name, String fn, int pos, int depth) {
		super(session, tasks, thread, frame, name, fn, pos, depth);
	}
	public String getFullName() {
		if (fFullName == null) {
			String n = getName();
			if (!n.startsWith("$")) { //$NON-NLS-1$
				fFullName = "$" + n; //$NON-NLS-1$
			}
		}
		return fFullName;
	}
}
