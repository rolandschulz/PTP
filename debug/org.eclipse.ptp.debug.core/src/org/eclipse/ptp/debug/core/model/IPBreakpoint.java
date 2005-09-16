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
package org.eclipse.ptp.debug.core.model;

import org.eclipse.cdt.debug.core.model.ICDebugTarget;
import org.eclipse.cdt.debug.core.model.ICThread;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * @author Clement chu
 *
 */
public interface IPBreakpoint extends IBreakpoint {
	public static final String GLOBAL = "Global";
	
	public static final String INSTALL_COUNT = "org.eclipse.ptp.debug.core.installCount";
	public static final String CONDITION = "org.eclipse.ptp.debug.core.condition";
	public static final String IGNORE_COUNT = "org.eclipse.ptp.debug.core.ignoreCount";
	public static final String THREAD_ID = "org.eclipse.ptp.debug.core.threadId";
	public static final String MODULE = "org.eclipse.ptp.debug.core.module";
	public static final String SOURCE_HANDLE = "org.eclipse.ptp.debug.core.sourceHandle";
	
	public static final String SET_ID = "org.eclipse.ptp.debug.core.setid";
	public static final String CUR_SET_ID = "org.eclipse.ptp.debug.core.cursetid";
	public static final String JOB_ID = "org.eclipse.ptp.debug.core.jobid";
	public static final String JOB_NAME = "org.eclipse.ptp.debug.core.jobname";

	public void updateMarkerMessage() throws CoreException;
	
	public boolean isGlobal() throws CoreException;
	
	public String getCurSetId() throws CoreException;
	public void setCurSetId(String id) throws CoreException;

	public String getSetId() throws CoreException;
	public void setSetId(String id) throws CoreException;
	
	public String getJobId() throws CoreException;
	public void setJobId(String id) throws CoreException;

	public String getJobName() throws CoreException;
	public void setJobName(String name) throws CoreException;
	
	public boolean isInstalled() throws CoreException;
	public boolean isConditional() throws CoreException;
	
	public String getCondition() throws CoreException;
	public void setCondition(String condition) throws CoreException;
	
	public int getIgnoreCount() throws CoreException;
	public void setIgnoreCount(int ignoreCount) throws CoreException;
	
	public String getThreadId() throws CoreException;
	public void setThreadId(String threadId) throws CoreException;
	
	public String getModule() throws CoreException;
	public void setModule(String module) throws CoreException;
	
	public String getSourceHandle() throws CoreException;
	public void setSourceHandle(String sourceHandle) throws CoreException;
	
	public int incrementInstallCount() throws CoreException;
	public int decrementInstallCount() throws CoreException;
	public void resetInstallCount() throws CoreException;
	
	public void setTargetFilter(ICDebugTarget target) throws CoreException;
	public void removeTargetFilter(ICDebugTarget target) throws CoreException;
	public void setThreadFilters(ICThread[] threads) throws CoreException;
	public void removeThreadFilters(ICThread[] threads) throws CoreException;
	public ICThread[] getThreadFilters(ICDebugTarget target) throws CoreException;
	public ICDebugTarget[] getTargetFilters() throws CoreException; 
}
