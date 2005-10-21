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

import java.util.Observer;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.util.Queue;
import org.eclipse.ptp.debug.core.cdi.IPCDISession;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcess;
import org.eclipse.ptp.debug.external.cdi.PCDIException;

/**
 * @author donny
 *
 */
public interface IAbstractDebugger {

	/* Debugger Initialization/Termination */
	public void initialize(IPJob job);
	public void exit() throws PCDIException;
	
	/* Events */
	public final int IDBGEV_BPHIT = 0;
	public final int IDBGEV_ENDSTEPPING = 1;
	public final int IDBGEV_PROCESSRESUMED = 2;
	public final int IDBGEV_PROCESSTERMINATED = 3;

	public void addDebuggerObserver(Observer obs);
	public void deleteDebuggerObserver(Observer obs);
	public void fireEvents(IPCDIEvent[] events);
	public void fireEvent(IPCDIEvent event);
	public void notifyObservers(Object arg);
	public Queue getEventQueue();
	
	/* Miscellaneous */
	public IPCDISession getSession();
	public void setSession(IPCDISession session);
	public boolean isExiting();
	public IPCDIDebugProcess getProcess(int number);
	public IPCDIDebugProcess getProcess();
	public IPCDIDebugProcess[] getProcesses();
	public Process getPseudoProcess(IPCDIDebugProcess proc);
	public void removePseudoProcess(IPCDIDebugProcess proc);

}