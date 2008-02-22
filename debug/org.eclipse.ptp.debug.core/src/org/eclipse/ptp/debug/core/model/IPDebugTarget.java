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

import java.math.BigInteger;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;

/**
 * @author Clement chu
 * 
 */
public interface IPDebugTarget extends IDebugTarget, IExecFileInfo, IRestart, IResumeWithoutSignal, IPDebugElement,
		ITargetProperties, ISteppingModeTarget {
	/**
	 * @param name
	 * @param descriptors
	 */
	public void addRegisterGroup(String name, IPRegisterDescriptor[] descriptors);

	/**
	 * @param globalVariableDescriptor
	 * @return
	 */
	public IPGlobalVariable createGlobalVariable(IPGlobalVariableDescriptor globalVariableDescriptor) throws DebugException;

	/**
	 * 
	 */
	public void dispose();

	/**
	 * @param content
	 */
	public void fireChangeEvent(int content);

	/**
	 * @param breakpoint
	 * @return
	 * @throws DebugException
	 */
	public BigInteger getBreakpointAddress(IPLineBreakpoint breakpoint) throws DebugException;

	/**
	 * @return
	 */
	public IPDITarget getPDITarget();

	/**
	 * @return
	 * @throws DebugException
	 */
	public IPRegisterDescriptor[] getRegisterDescriptors() throws DebugException;

	/**
	 * @return
	 * @throws DebugException
	 */
	public IPSignal[] getSignals() throws DebugException;

	/**
	 * @return
	 */
	public BitList getTasks();

	/**
	 * @return
	 * @throws DebugException
	 */
	public boolean hasSignals() throws DebugException;

	/**
	 * @return
	 */
	public boolean isLittleEndian();

	/**
	 * @return
	 */
	public boolean isPostMortem();

	/**
	 * @param group
	 * @param descriptors
	 */
	public void modifyRegisterGroup(IPPersistableRegisterGroup group, IPRegisterDescriptor[] descriptors);

	/**
	 * @param groups
	 */
	public void removeRegisterGroups(IRegisterGroup[] groups);

	/**
	 * 
	 */
	public void restoreDefaultRegisterGroups();
}
