/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.external.cdi;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDILocationBreakpoint;
import org.eclipse.ptp.debug.external.DebugSession;
import org.eclipse.ptp.debug.external.cdi.model.Breakpoint;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.EBreakpointCreated;

/**
 * Breakpoint Manager for the CDI interface.
 */
public class BreakpointManager extends Manager {
	
	boolean allowInterrupt;
	
	public BreakpointManager(Session session) {
		super(session, false);
		// TODO Auto-generated constructor stub
		
		allowInterrupt = true;
	}
	
	protected void update(org.eclipse.ptp.debug.external.cdi.model.Target target) throws CDIException {
		// Auto-generated method stub
		System.out.println("BreakpointManager.update()");
		
	}
	
	public Location createLocation(String file, String function, int line) {
		// Auto-generated method stub
		System.out.println("BreakpointManager.createLocation()");
		
		return new Location(file, function, line);
	}
	
	public Location createLocation(BigInteger address) {
		// Auto-generated method stub
		System.out.println("BreakpointManager.createLocation()");
		
		return new Location(address);
	}
	
	public void setLocationBreakpoint (Breakpoint bkpt) throws CDIException {
		Target target = (Target)bkpt.getTarget();
		DebugSession miSession = target.getDebugSession();
		
		target.suspend();
		
		/* Set the breakpoints in the IDebugger */
		miSession.getDebugger().breakpoint(bkpt.getLocation().getFunction());
		
		target.resume();
		
		/* Should put the actual actionpoint in bkpt */
		bkpt.setMIBreakpoints(null);
	}
	
	public ICDILocationBreakpoint setLocationBreakpoint(Target target, int type, ICDILocation location,
			ICDICondition condition, boolean deferred) throws CDIException {
		System.out.println("BreakpointManager.setLocationBreakpoint()");
		DebugSession dSession = target.getDebugSession();
		Breakpoint bkpt = new Breakpoint(target, type, location, condition);
		
		try {
			setLocationBreakpoint(bkpt);
			
			int procsNum = target.getProcesses().length;
			
			/* testing lots and lots of breakpoints */
			long start = System.currentTimeMillis();
			for (int j = 0; j < procsNum; j++)
				dSession.getDebugger().fireEvent(new EBreakpointCreated(dSession));
			long end = System.currentTimeMillis();
			double totalseconds = (double)(end - start) / (double)1000;
			System.out.println("BreakpointManager.setLocationBreakpoint() takes " + totalseconds + " seconds");
			/* testing lots and lots of breakpoints */
			
			// Fire a created Event.
			//dSession.getDebugger().fireEvent(new EBreakpointCreated(dSession));
			
		} catch (CDIException e) {
		}
		return bkpt;
	}
}
