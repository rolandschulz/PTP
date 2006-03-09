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
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILineBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIWatchpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;

/**
 * @author Clement chu
 * 
 */
public interface IPCDIBreakpointManager {
	public IPBreakpoint findBreakpoint(IPCDIBreakpoint cdiBpt);
	public IPBreakpoint findBreakpoint(int bpid);
	public IPCDIBreakpoint findCDIBreakpoint(IPBreakpoint bpt);
	public IPCDIBreakpoint findCDIBreakpoint(int bpid);
	
	public void deleteBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException;
	public void setBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException;
	public void setEnableBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException;
	public void setConditionBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException;
	
	public IPCDILineBreakpoint setLineBreakpoint(BitList tasks, int type, IPCDILineLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;
	public IPCDIFunctionBreakpoint setFunctionBreakpoint(BitList tasks, int type, IPCDIFunctionLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;
	public IPCDIAddressBreakpoint setAddressBreakpoint(BitList tasks, int type, IPCDIAddressLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;
	public IPCDIWatchpoint setWatchpoint(BitList tasks, int type, int watchType, String expression, IPCDICondition condition) throws PCDIException;
	
	public IPCDICondition createCondition(int ignoreCount, String expression, String[] tids);
	public IPCDILineLocation createLineLocation(String file, int line);
	public IPCDIFunctionLocation createFunctionLocation(String file, String function);
	public IPCDIAddressLocation createAddressLocation(BigInteger address);
}

