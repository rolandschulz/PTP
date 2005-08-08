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
package org.eclipse.ptp.debug.core.breakpoint;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * @author Clement chu
 *
 */
public interface IPBreakpoint extends IBreakpoint {
	public static final String THREAD_ID = "org.eclipse.ptp.debug.core.threadId";
	public static final String MODULE = "org.eclipse.ptp.debug.core.module";
	public static final String SOURCE_HANDLE = "org.eclipse.ptp.debug.core.sourceHandle";
	
	public String getThreadId() throws CoreException;
	public void setThreadId(String threadId) throws CoreException;
	
	public String getSourceHandle() throws CoreException;
	public void setSourceHandle( String sourceHandle ) throws CoreException;
	
	//public void setTargetFilter(IPDebugTarget target) throws CoreException;
	//public void removeTargetFilter(IPDebugTarget target) throws CoreException;
	//public void setThreadFilters(IPThread[] threads) throws CoreException;\
	//public void removeThreadFilters(IPThread[] threads) throws CoreException;
	//public ICThread[] getThreadFilters(IPDebugTarget target) throws CoreException;
	//public IPDebugTarget[] getTargetFilters() throws CoreException; 
}
