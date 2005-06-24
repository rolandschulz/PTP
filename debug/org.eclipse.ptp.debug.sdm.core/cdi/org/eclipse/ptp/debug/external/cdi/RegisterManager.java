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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterDescriptor;
import org.eclipse.cdt.debug.core.cdi.model.ICDIRegisterGroup;
import org.eclipse.ptp.debug.external.cdi.model.RegisterGroup;
import org.eclipse.ptp.debug.external.cdi.model.Target;


/**
 */
public class RegisterManager extends Manager {

	public RegisterManager(Session session) {
		super(session, true);
		// The register bookkeeping provides better update control.
		setAutoUpdate( true );
	}

	protected void update(Target target) throws CDIException {
		// Auto-generated method stub
		System.out.println("RegisterManager.update()");
		
	}
	
	public ICDIRegisterGroup[] getRegisterGroups(Target target) throws CDIException {
		System.out.println("RegisterManager.getRegisterGroups()");
		RegisterGroup group = new RegisterGroup(target, "Main"); //$NON-NLS-1$
		return new ICDIRegisterGroup[] { group };
	}
	
	public ICDIRegisterDescriptor[] getRegisterDescriptors(RegisterGroup group) throws CDIException {
		Target target = (Target)group.getTarget();
		return getRegisterDescriptors(target);
	}
	public ICDIRegisterDescriptor[] getRegisterDescriptors(Target target) throws CDIException {
		List regsList = new ArrayList(0);
		return (ICDIRegisterDescriptor[])regsList.toArray(new ICDIRegisterDescriptor[0]);
	}
}
