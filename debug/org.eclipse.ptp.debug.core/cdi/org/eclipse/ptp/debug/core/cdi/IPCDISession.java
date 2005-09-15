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
package org.eclipse.ptp.debug.core.cdi;

import java.math.BigInteger;

import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDICondition;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.model.ICDIAddressBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIFunctionBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIDebugProcessSet;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;

public interface IPCDISession extends ICDISession {
	
	public void registerTarget(int procNum, boolean sendEvent);
	public void registerTargets(int[] procNums, boolean sendEvent);
	public void unregisterTarget(int procNum, boolean sendEvent);
	public void unregisterTargets(int[] targets, boolean sendEvent);
	public int[] getRegisteredTargetIds();
	public boolean isRegistered(int i);
	public IPCDITarget getTarget(int i);
	public IPCDIModelManager getModelManager();
	
	/* Breakpoint */
	public ICDILineBreakpoint setLineBreakpoint(int type,
			ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException;
	public ICDIFunctionBreakpoint setFunctionBreakpoint(int type,
			ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException;
	public ICDIAddressBreakpoint setAddressBreakpoint(int type,
			ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException;
	
	public ICDILineBreakpoint setLineBreakpoint(IPCDIDebugProcessSet bSet, int type,
			ICDILineLocation location, ICDICondition condition, boolean deferred) throws CDIException;
	public ICDIFunctionBreakpoint setFunctionBreakpoint(IPCDIDebugProcessSet bSet, int type,
			ICDIFunctionLocation location, ICDICondition condition, boolean deferred) throws CDIException;
	public ICDIAddressBreakpoint setAddressBreakpoint(IPCDIDebugProcessSet bSet, int type,
			ICDIAddressLocation location, ICDICondition condition, boolean deferred) throws CDIException;

	/* Location */
	public ICDILineLocation createLineLocation(String file, int line);
	public ICDIFunctionLocation createFunctionLocation(String file, String function);
	public ICDIAddressLocation createAddressLocation(BigInteger address);
	
	/* Execution */
	public void stepOver(String setName);
	public void stepOver(String setName, int count);
	public void stepInto(String setName);
	public void stepInto(String setName, int count);
	public void stepFinish(String setName);
}
