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
package org.eclipse.ptp.debug.core.cdi.model;

import org.eclipse.ptp.debug.core.cdi.IPCDIAddressLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDICondition;
import org.eclipse.ptp.debug.core.cdi.IPCDIFunctionLocation;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.PCDIException;

/**
 * @author Clement chu
 * 
 */
public interface IPCDIBreakpointManagement {
	IPCDILineBreakpoint setLineBreakpoint(int type, IPCDILineLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;		
	IPCDIFunctionBreakpoint setFunctionBreakpoint(int type, IPCDIFunctionLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;
	IPCDIAddressBreakpoint setAddressBreakpoint(int type, IPCDIAddressLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;		
	IPCDIWatchpoint setWatchpoint(int type, int watchType, String expression, IPCDICondition condition) throws PCDIException;
	IPCDIExceptionpoint setExceptionBreakpoint(String clazz, boolean stopOnThrow, boolean stopOnCatch) throws PCDIException;
	IPCDIBreakpoint[] getBreakpoints() throws PCDIException;
	void deleteBreakpoints(IPCDIBreakpoint[] breakpoints) throws PCDIException;
	void deleteAllBreakpoints() throws PCDIException;
}
