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

/**
 * Breakpoint Manager for the CDI interface.
 */
public class BreakpointManager extends Manager {

	public BreakpointManager(Session session) {
		super(session, false);
		// TODO Auto-generated constructor stub
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


}
