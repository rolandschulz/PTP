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
package org.eclipse.ptp.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;

/**
 * Notifies that the originator has been suspended.
 * 
 * @author clement
 * 
 */
public interface IPDISuspendedEvent extends IPDIEvent {
	/**
	 * Returns the session object that caused the suspension
	 * eg:
	 * IPDIBreakpointInfo
	 * IPDIEndSteppingRangeInfo
	 * IPDILocationReachedInfo
	 * IPDISignalInfo
	 * IPDIFunctionFinishedInfo
	 * IPDISharedLibraryInfo
	 * IPDIWatchpointScopeInfo
	 * IPDIWatchpointTriggerInfo
	 * 
	 * @return IPDISessionObject the session object that caused the suspension
	 */
	public IPDISessionObject getReason();

	/**
	 * Returns a list of variables that are changed
	 * 
	 * @return a list of variables
	 */
	public String[] getUpdatedVariables();

	/**
	 * Returns thread ID
	 * 
	 * @return thread ID
	 */
	public int getThreadID();

	/**
	 * Returns level of current suspended frame
	 * 
	 * @return level of current suspended frame
	 */
	public int getLevel();

	/**
	 * Returns the maximum level of suspended frame
	 * 
	 * @return the maximum level of suspended frame
	 */
	public int getDepth();
}
