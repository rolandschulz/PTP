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
package org.eclipse.ptp.debug.core.pdi;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.model.IPDIAddressBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIExceptionpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIFunctionBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDILineBreakpoint;
import org.eclipse.ptp.debug.core.pdi.model.IPDIWatchpoint;

/**
 * Represents breakpoint action management. Each methods send to debugger and replied by event.
 * @author clement
 *
 */
public interface IPDIBreakpointManagement {
	/**
	 * Requests to set a line breakpoint of specify process
	 * @param tasks target process 
	 * @param bpt line breakpoint to be set
 	 * @throws PDIException on failure
	 */
	void setLineBreakpoint(BitList tasks, IPDILineBreakpoint bpt) throws PDIException;
	
	/**
	 * Requests to set a function breakpoint of specify process
	 * @param tasks target process
	 * @param bpt function breakpoint to be set
	 * @throws PDIException on failure
	 */
	void setFunctionBreakpoint(BitList tasks, IPDIFunctionBreakpoint bpt) throws PDIException;
	
	/**
	 * Requests to set a address breakpoint of specify process
	 * @param tasks target process
	 * @param bpt address breakpoint to be set
	 * @throws PDIException on failure
	 */
	void setAddressBreakpoint(BitList tasks, IPDIAddressBreakpoint bpt) throws PDIException;

	/**
	 * Requests to set a watchpoint of specify process
	 * @param tasks target process
	 * @param bpt watchpoint to be set
	 * @throws PDIException on failure
	 */
	void setWatchpoint(BitList tasks, IPDIWatchpoint bpt) throws PDIException;
	
	/**
	 * Requests to set an exceptionpoint of specify process
	 * @param tasks target process
	 * @param bpt an exceptionpoint to be set
	 * @throws PDIException failure
	 */
	void setExceptionpoint(BitList tasks, IPDIExceptionpoint bpt) throws PDIException;
	
	/**
	 * Requests to delete a given breakpoint of specify process
	 * @param tasks target process
	 * @param bpid breakpoint id to be deleted
	 * @throws PDIException on failure
	 */
	void deleteBreakpoint(BitList tasks, int bpid) throws PDIException;
	
	/**
	 * Requests to set enable / disable a given breakpoint of specify process  
	 * @param tasks target process
	 * @param bpid breakpoint id to be enabled / disabled
	 * @param enabled true if enable
	 * @throws PDIException on failure
	 */
	void setEnabledBreakpoint(BitList tasks, int bpid, boolean enabled) throws PDIException;
	
	/**
	 * Requests to set condition on given breakpoint of specify process 
	 * @param tasks target process
	 * @param bpid breakpoint id to be set condition 
	 * @param condition condition rule
	 * @throws PDIException on failure
	 */
	void setConditionBreakpoint(BitList tasks, int bpid, String condition) throws PDIException;
}
