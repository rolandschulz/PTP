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
import org.eclipse.ptp.internal.debug.core.messages.Messages;

/**
 * @author Clement chu
 * 
 */
public interface IPBreakpoint extends IBreakpoint {
	public static final String GLOBAL = Messages.IPBreakpoint_0;

	public static final String CONDITION = "org.eclipse.ptp.debug.core.condition"; //$NON-NLS-1$
	public static final String IGNORE_COUNT = "org.eclipse.ptp.debug.core.ignoreCount"; //$NON-NLS-1$
	public static final String SOURCE_HANDLE = "org.eclipse.ptp.debug.core.sourceHandle"; //$NON-NLS-1$
	public static final String INSTALL_COUNT = "org.eclipse.ptp.debug.core.installCount"; //$NON-NLS-1$
	public static final String CUR_SET_ID = "org.eclipse.ptp.debug.core.cursetid"; //$NON-NLS-1$
	public static final String JOB_NAME = "org.eclipse.ptp.debug.core.jobname"; //$NON-NLS-1$

	/**
	 * @return
	 * @throws CoreException
	 */
	public int decrementInstallCount() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getCondition() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getCurSetId() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public int getIgnoreCount() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getJobId() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getJobName() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getSetId() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public String getSourceHandle() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public int incrementInstallCount() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public boolean isConditional() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public boolean isGlobal() throws CoreException;

	/**
	 * @return
	 * @throws CoreException
	 */
	public boolean isInstalled() throws CoreException;

	/**
	 * @throws CoreException
	 */
	public void resetInstallCount() throws CoreException;

	/**
	 * @param condition
	 * @throws CoreException
	 */
	public void setCondition(String condition) throws CoreException;

	/**
	 * @param id
	 * @throws CoreException
	 */
	public void setCurSetId(String id) throws CoreException;

	/**
	 * @param ignoreCount
	 * @throws CoreException
	 */
	public void setIgnoreCount(int ignoreCount) throws CoreException;

	/**
	 * @param id
	 * @throws CoreException
	 */
	public void setJobId(String id) throws CoreException;

	/**
	 * @param name
	 * @throws CoreException
	 */
	public void setJobName(String name) throws CoreException;

	/**
	 * @param id
	 * @throws CoreException
	 */
	public void setSetId(String id) throws CoreException;

	/**
	 * @param sourceHandle
	 * @throws CoreException
	 */
	public void setSourceHandle(String sourceHandle) throws CoreException;

	/**
	 * @throws CoreException
	 */
	public void updateMarkerMessage() throws CoreException;

	/*
	 * TODO - Not Implemented String getThreadId() throws CoreException; void
	 * setThreadId(String threadId) throws CoreException;
	 * 
	 * String getModule() throws CoreException; void setModule(String module)
	 * throws CoreException;
	 * 
	 * void setTargetFilter(IPDebugTarget target) throws CoreException; void
	 * removeTargetFilter(IPDebugTarget target) throws CoreException; void
	 * setThreadFilters(IPThread[] threads) throws CoreException; void
	 * removeThreadFilters(IPThread[] threads) throws CoreException; IPThread[]
	 * getThreadFilters(IPDebugTarget target) throws CoreException;
	 * IPDebugTarget[] getTargetFilters() throws CoreException;
	 */
}
