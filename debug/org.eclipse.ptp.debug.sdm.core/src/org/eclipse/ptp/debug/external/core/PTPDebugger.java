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
package org.eclipse.ptp.debug.external.core;

import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.debug.core.IAbstractDebugger;
import org.eclipse.ptp.debug.core.IPTPDebugger;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.external.core.cdi.Session;
import org.eclipse.ptp.debug.external.core.debugger.ParallelDebugger;

/**
 * @deprecated
 *
 */
public class PTPDebugger implements IPTPDebugger {
	IAbstractDebugger debugger;
	
	public PTPDebugger() {
		debugger = new ParallelDebugger();
		//debugger = new DebugSimulation2();
	}
	
	public int startDebuggerListener() throws CoreException {
		return debugger.getDebuggerPort();
	}
	
	public void stopDebugger() throws CoreException {
		debugger.stopDebugger();
	}

	public IPCDISession createDebuggerSession(IPLaunch launch, IBinaryObject exe, IProgressMonitor monitor) throws CoreException {
		IPJob job = launch.getPJob();
		debugger.initialize(job, 10000, monitor);
		Session session = new Session(debugger, job, launch, exe);
		session.start(monitor);
		return session;
	}
}