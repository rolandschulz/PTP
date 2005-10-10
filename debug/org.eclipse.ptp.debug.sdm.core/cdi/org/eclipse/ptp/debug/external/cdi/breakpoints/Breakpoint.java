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
package org.eclipse.ptp.debug.external.cdi.breakpoints;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDITarget;
import org.eclipse.ptp.debug.external.PTPDebugExternalPlugin;

public class Breakpoint implements ICDIBreakpoint {
	ICDICondition condition;
	
	int type;
	boolean enable;

	public Breakpoint(int kind, ICDICondition cond) {
		type = kind;
		condition = cond;
		enable = true;
	}

	public ICDICondition getCondition() throws CDIException {
		if (condition == null) {
			return null;
		}
		return condition;
	}

	public boolean isEnabled() throws CDIException {
		return enable;
	}

	public boolean isHardware() {
		return (type == ICDIBreakpoint.HARDWARE);
	}

	public boolean isTemporary() {
		return (type == ICDIBreakpoint.TEMPORARY);
	}

	public void setCondition(ICDICondition newCondition) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public void setEnabled(boolean on) throws CDIException {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
	}

	public ICDITarget getTarget() {
		PTPDebugExternalPlugin.getDefault().getLogger().finer("");
		return null;
	}
}