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
package org.eclipse.ptp.debug.core.pdi.model;

import org.eclipse.ptp.debug.core.pdi.IPDICondition;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Provides a basic functionality for the location breakpoints, watchpoints and catchpoints.
 * @author clement
 *
 */
public interface IPDIBreakpoint extends IPDISessionObject {
	/**
	 * Regular type
	 */
	public final static int REGULAR = 0x0;
	
	/**
	 * Temporary type 
	 */
	public final static int TEMPORARY = 0x1;
	
	/**
	 * Hardware type 
	 */
	public final static int HARDWARE = 0x2;
	
	/**
	 * Determines whether this breakpoint is temporary
	 * @return whether this breakpoint is temporary
	 */
	boolean isTemporary();
	
	/**
	 * Determines whether this breakpoint is hardware-assisted
	 * @return whether this breakpoint is hardware-assisted
	 */
	boolean isHardware();
	
	/**
	 * Determines whether this breakpoint is enabled
	 * @return whether this breakpoint is enabled
	 * @throws PDIException on failure
	 */
	boolean isEnabled() throws PDIException;
	
	/**
	 * Sets the breakpoint state to be enabled or disabled.
	 * @param enabled whether this breakpoint should be enabled
	 * @throws PDIException on failure
	 */
	void setEnabled(boolean enabled) throws PDIException;
	
	/**
	 * Returns the condition of this breakpoint or null if no condition in this breakpoint
	 * @return the condition of this breakpoint
	 * @throws PDIException on failure
	 */
	IPDICondition getCondition() throws PDIException;
	
	/**
	 * Sets the condition of this breakpoint
	 * @param condition the condition to set
	 * @throws PDIException on failure
	 */
	void setCondition(IPDICondition condition) throws PDIException;
	
	/**
	 * Returns internal breakpoint id
	 * @return internal breakpoint id
	 */
	int getInternalID();
	
	/**
	 * Returns breakpoint id of this breakpoint
	 * @return breakpoint id of this breakpoint
	 */
	int getBreakpointID();
	
	/**
	 * Sets a breakpoint id for this breakpoint
	 * @param bpid breakpoint id 
	 */
	void setBreakpointID(int bpid);
}
