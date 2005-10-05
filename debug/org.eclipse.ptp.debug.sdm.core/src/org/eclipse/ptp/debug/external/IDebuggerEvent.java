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
/*
 * Created on Feb 18, 2005
 *
 */
package org.eclipse.ptp.debug.external;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;


/**
 * @author donny
 *
 */
public interface IDebuggerEvent {

	/* These methods will be called by
	 * public void handleDebugEvent(int eventType, BitList procs, String[] args) (IAbstractDebugger)
	 * based on the type of the event.
	 */
	public abstract IPCDIEvent handleBreakpointHitEvent(BitList procs, String[] args);
	public abstract IPCDIEvent handleEndSteppingEvent(BitList procs, String[] args);
	public abstract IPCDIEvent handleProcessResumedEvent(BitList procs, String[] args);
	public abstract IPCDIEvent handleProcessTerminatedEvent(BitList procs, String[] args);
	
}
