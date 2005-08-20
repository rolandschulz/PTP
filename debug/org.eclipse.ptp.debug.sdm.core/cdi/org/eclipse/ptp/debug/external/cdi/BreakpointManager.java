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
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.external.cdi.event.BreakpointCreatedEvent;
import org.eclipse.ptp.debug.external.cdi.model.AddressBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.AddressLocation;
import org.eclipse.ptp.debug.external.cdi.model.DebugProcessSet;
import org.eclipse.ptp.debug.external.cdi.model.FunctionBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.FunctionLocation;
import org.eclipse.ptp.debug.external.cdi.model.LineBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.LineLocation;
import org.eclipse.ptp.debug.external.cdi.model.LocationBreakpoint;
import org.eclipse.ptp.debug.external.cdi.model.Target;

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
		Session session = (Session) target.getSession();
		
		try {
			setLocationBreakpoint(bkpt);
			
			// Fire a created Event.
			int pId = target.getTargetId();
			
			IPCDIDebugProcessSet newSet = new DebugProcessSet(session, pId);
			//sess.getDebugger().fireEvent(new EBreakpointCreated(bitSet));
			session.getDebugger().fireEvent(new BreakpointCreatedEvent(session, newSet));
			
			if (bkpt instanceof LineBreakpoint) {
				session.getDebugger().setLineBreakpoint(newSet, (ICDILineBreakpoint) bkpt);
			} else if (bkpt instanceof FunctionBreakpoint) {
				session.getDebugger().setFunctionBreakpoint(newSet, (ICDIFunctionBreakpoint) bkpt);
			}
			
			
		} catch (CDIException e) {
		}
	}
}
