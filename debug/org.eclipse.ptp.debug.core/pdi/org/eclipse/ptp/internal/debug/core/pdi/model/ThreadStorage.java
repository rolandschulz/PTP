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
import org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThread;
import org.eclipse.ptp.debug.core.pdi.model.IPDIThreadStorage;
import org.eclipse.ptp.debug.core.pdi.model.IPDIVariable;

/**
 * @author clement
 *
 */
public class ThreadStorage extends Variable implements IPDIThreadStorage {
	public ThreadStorage(IPDISession session, ThreadStorageDescriptor threadDesc, String varid) {
		super(session, threadDesc, varid);
	}
	
	public ThreadStorage(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame frame, String name, String fullName, int pos, int depth, String varid) {
		super(session, tasks, thread, frame, name, fullName, pos, depth, varid);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.internal.debug.core.pdi.model.Variable#createVariable(org.eclipse.ptp.internal.debug.core.pdi.Session, org.eclipse.ptp.core.util.TaskSet, org.eclipse.ptp.internal.debug.core.pdi.model.Thread, org.eclipse.ptp.debug.core.pdi.model.IPDIStackFrame, java.lang.String, java.lang.String, int, int, java.lang.String)
	 */
	protected IPDIVariable createVariable(IPDISession session, TaskSet tasks, IPDIThread thread, IPDIStackFrame frame, String name, String fullName, int pos, int depth, String varid) {
		return null;
	}
}
