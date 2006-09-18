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
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;

/**
 * @author Clement chu
 * 
 */
public interface IPCDIBreakpointManager {
	/** Find PTP Breakpoint by matching PCDI Breakpoint
	 * @param cdiBpt
	 * @return
	 */
	public IPBreakpoint findBreakpoint(IPCDIBreakpoint cdiBpt);
	/** Find PTP Breakpoint by breakpoint ID
	 * @param bpid
	 * @return
	 */
	public IPBreakpoint findBreakpoint(int bpid);
	/** Find PCDI Breakpoint by maching PTP Breakpoint
	 * @param bpt
	 * @return
	 */
	public IPCDIBreakpoint findCDIBreakpoint(IPBreakpoint bpt);
	/** Find PCDI Breakpoint by breakpoint ID
	 * @param bpid
	 * @return
	 */
	public IPCDIBreakpoint findCDIBreakpoint(int bpid);
	
	/** Delete breakpoint by given PTP breakpoint and job
	 * @param job_id
	 * @param bpt
	 * @throws CoreException
	 */
	public void deleteBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException;
	/** Add breakpoint with given PTP breakpoint and job
	 * @param job_id
	 * @param bpt
	 * @param ignoreCheck
	 * @throws CoreException
	 */
	public void setBreakpoint(String job_id, IPBreakpoint bpt, boolean ignoreCheck) throws CoreException;
	/** Set enable breakpoint with given PTP breakpoint and job
	 * @param job_id
	 * @param bpt
	 * @throws CoreException
	 */
	public void setEnableBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException;
	/** Set condition breakpoint with given PTP breakpoint and job
	 * @param job_id
	 * @param bpt
	 * @throws CoreException
	 */
	public void setConditionBreakpoint(String job_id, IPBreakpoint bpt) throws CoreException;
	/** Set line breakpoint
	 * @param tasks processes to set a line breakpoint
	 * @param type hardware or temporary breakpoint
	 * @param location breakpoint location
	 * @param condition breakpoint condition
	 * @param deferred is deferred breakpoint
	 * @return PCDI line breakpoint
	 * @throws PCDIException
	 */
	//public IPCDILineBreakpoint setLineBreakpoint(BitList tasks, int type, IPCDILineLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;
	/** Set function breakpoint
	 * @param tasks processes to set a function breakpoint
	 * @param type hardware or temporary breakpoint
	 * @param location breakpoint location
	 * @param condition breakpoint condition
	 * @param deferred is deferred breakpoint
	 * @return PCDI function breakpoint
	 * @throws PCDIException
	 */
	//public IPCDIFunctionBreakpoint setFunctionBreakpoint(BitList tasks, int type, IPCDIFunctionLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;
	/** Set address breakpoint
	 * @param tasks processes to set a function breakpoint
	 * @param type hardware or temporary breakpoint
	 * @param location breakpoint location
	 * @param condition breakpoint condition
	 * @param deferred is deferred breakpoint
	 * @return
	 * @throws PCDIException
	 */
	//public IPCDIAddressBreakpoint setAddressBreakpoint(BitList tasks, int type, IPCDIAddressLocation location, IPCDICondition condition, boolean deferred) throws PCDIException;
	/** Set watch point
	 * @param tasks processes to set a function breakpoint
	 * @param type hardware or temporary breakpoint
	 * @param watchType
	 * @param expression variable name
	 * @param condition breakpoint condition
	 * @return PCDI watch point
	 * @throws PCDIException
	 */
	//public IPCDIWatchpoint setWatchpoint(BitList tasks, int type, int watchType, String expression, IPCDICondition condition) throws PCDIException;
	
	/** Create breakpoint condition
	 * @param ignoreCount the number of ignoring
	 * @param expression condition text
	 * @param tids thread ids
	 * @return PCDI condition
	 */
	public IPCDICondition createCondition(int ignoreCount, String expression, String[] tids);
	/** Create line location
	 * @param file locate on file name
	 * @param line line number
	 * @return PCDI line location
	 */
	public IPCDILineLocation createLineLocation(String file, int line);
	/** Create function location
	 * @param file locate on file name
	 * @param function function name
	 * @return PCDI function location
	 */
	public IPCDIFunctionLocation createFunctionLocation(String file, String function);
	/** Create address location
	 * @param address address
	 * @return PCDI address location
	 */
	public IPCDIAddressLocation createAddressLocation(BigInteger address);
}

