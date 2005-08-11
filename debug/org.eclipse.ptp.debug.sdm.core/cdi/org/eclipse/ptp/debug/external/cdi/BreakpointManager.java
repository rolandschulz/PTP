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
import java.util.Hashtable;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.AddressBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.AddressLocation;
import org.eclipse.ptp.debug.external.cdi.model.FunctionBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.FunctionLocation;
import org.eclipse.ptp.debug.external.cdi.model.LineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.cdi.model.LocationBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.Target;
import org.eclipse.ptp.debug.external.event.EBreakpointCreated;

/**
 * Breakpoint Manager for the CDI interface.
 */
public class BreakpointManager extends Manager {
	
	boolean allowInterrupt;
	
	public BreakpointManager(Session session) {
		super(session, false);
		allowInterrupt = true;
	}
	
	protected void update(Target target) throws CDIException {
		// Auto-generated method stub
		System.out.println("BreakpointManager.update()");
	}
	
	public LineLocation createLineLocation(String file, int line) {
		return new LineLocation(file, line);
	}
	
	public FunctionLocation createFunctionLocation(String file, String function) {
		return new FunctionLocation(file, function);
	}

	public AddressLocation createAddressLocation(BigInteger address) {
		return new AddressLocation(address);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setLineBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDILineLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	public ICDILineBreakpoint setLineBreakpoint(Target target, int type, ICDILineLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		LineBreakpoint bkpt = new LineBreakpoint(target, type, location, condition);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setFunctionBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	public ICDIFunctionBreakpoint setFunctionBreakpoint(Target target, int type, ICDIFunctionLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		FunctionBreakpoint bkpt = new FunctionBreakpoint(target, type, location, condition);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpointManagement#setAddressBreakpoint(int, org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation, org.eclipse.cdt.debug.core.cdi.ICDICondition, boolean)
	 */
	public ICDIAddressBreakpoint setAddressBreakpoint(Target target, int type, ICDIAddressLocation location,
			ICDICondition condition, boolean deferred) throws CDIException {		
		AddressBreakpoint bkpt = new AddressBreakpoint(target, type, location, condition);
		setNewLocationBreakpoint(bkpt, deferred);
		return bkpt;
	}
	
	public void setLocationBreakpoint (LocationBreakpoint bkpt) throws CDIException {
		System.out.println("BreakpointManager.setLocationBreakpoint()");
	}
	
	public void setNewLocationBreakpoint(LocationBreakpoint bkpt, boolean deferred) throws CDIException {
		System.out.println("BreakpointManager.setNewLocationBreakpoint()");
		
		Target target = (Target)bkpt.getTarget();
		Session sess = (Session) target.getSession();
		
		try {
			setLocationBreakpoint(bkpt);
			
			// Fire a created Event.
			int pId = target.getTargetId();
			int tId = 0; /* thread id */
			
			Hashtable table = new Hashtable();
			table.put(new Integer(pId), new int[] { tId });
			
			sess.getDebugger().fireEvent(new EBreakpointCreated(table, new int[] { pId }));
			
		} catch (CDIException e) {
		}
	}
}
