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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;

/**
 * @author Clement chu
 *
 */
public interface IPBreakpoint extends IBreakpoint {
	public static final String GLOBAL = "Global";
	
	public static final String CONDITION = "org.eclipse.ptp.debug.core.condition";
	public static final String IGNORE_COUNT = "org.eclipse.ptp.debug.core.ignoreCount";
	public static final String SOURCE_HANDLE = "org.eclipse.ptp.debug.core.sourceHandle";
	public static final String INSTALL_COUNT = "org.eclipse.ptp.debug.core.installCount";
	//public static final String THREAD_ID = "org.eclipse.ptp.debug.core.threadId";
	//public static final String MODULE = "org.eclipse.ptp.debug.core.module";
	
	public static final String SET_ID = "org.eclipse.ptp.debug.core.setid";
	public static final String CUR_SET_ID = "org.eclipse.ptp.debug.core.cursetid";
	public static final String JOB_ID = "org.eclipse.ptp.debug.core.jobid";
	public static final String JOB_NAME = "org.eclipse.ptp.debug.core.jobname";

	void updateMarkerMessage() throws CoreException;
	
	boolean isGlobal() throws CoreException;
	
	String getCurSetId() throws CoreException;
	void setCurSetId(String id) throws CoreException;

	String getSetId() throws CoreException;
	void setSetId(String id) throws CoreException;
	
	String getJobId() throws CoreException;
	void setJobId(String id) throws CoreException;
	
	String getJobName() throws CoreException;
	void setJobName(String name) throws CoreException;
	
	boolean isInstalled() throws CoreException;
	boolean isConditional() throws CoreException;

	int incrementInstallCount() throws CoreException;
	int decrementInstallCount() throws CoreException;
	void resetInstallCount() throws CoreException;

	String getCondition() throws CoreException;
	void setCondition(String condition) throws CoreException;
	
	int getIgnoreCount() throws CoreException;
	void setIgnoreCount(int ignoreCount) throws CoreException;
	
	String getSourceHandle() throws CoreException;
	void setSourceHandle(String sourceHandle) throws CoreException;
/*
 * TODO - Not Implemented
	String getThreadId() throws CoreException;
	void setThreadId(String threadId) throws CoreException;
	
	String getModule() throws CoreException;
	void setModule(String module) throws CoreException;
	
	void setTargetFilter(IPDebugTarget target) throws CoreException;
	void removeTargetFilter(IPDebugTarget target) throws CoreException;
	void setThreadFilters(IPThread[] threads) throws CoreException;
	void removeThreadFilters(IPThread[] threads) throws CoreException;
	IPThread[] getThreadFilters(IPDebugTarget target) throws CoreException;
	IPDebugTarget[] getTargetFilters() throws CoreException;
*/ 
}
