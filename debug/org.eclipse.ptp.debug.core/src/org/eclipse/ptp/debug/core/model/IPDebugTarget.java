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
import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.model.IPDITarget;

/**
 * Represents a debuggable execution context
 * 
 * @author Clement chu
 * 
 */
public interface IPDebugTarget extends IDebugTarget, IExecFileInfo, IRestart, IResumeWithoutSignal, IPDebugElement,
		ITargetProperties, ISteppingModeTarget {
	/**
	 * Add a register group
	 * 
	 * @param name
	 * @param descriptors
	 */
	public void addRegisterGroup(String name, IPRegisterDescriptor[] descriptors);

	/**
	 * Create a global variable
	 * 
	 * @param globalVariableDescriptor
	 * @return
	 */
	public IPGlobalVariable createGlobalVariable(IPGlobalVariableDescriptor globalVariableDescriptor) throws DebugException;

	/**
	 * Dispose of any resources
	 */
	public void dispose();

	/**
	 * Fire debugger change event
	 * 
	 * @param content
	 */
	public void fireChangeEvent(int content);

	/**
	 * Get the breakpoint address
	 * 
	 * @param breakpoint
	 * @return
	 * @throws DebugException
	 */
	public BigInteger getBreakpointAddress(IPLineBreakpoint breakpoint) throws DebugException;

	/**
	 * Get the PDI debug target
	 * 
	 * @return
	 */
	public IPDITarget getPDITarget();

	/**
	 * Get the register descriptors
	 * 
	 * @return
	 * @throws DebugException
	 */
	public IPRegisterDescriptor[] getRegisterDescriptors() throws DebugException;

	/**
	 * Get the debugger signal handlers
	 * 
	 * @return
	 * @throws DebugException
	 */
	public IPSignal[] getSignals() throws DebugException;

	/**
	 * Get the tasks for this target
	 * 
	 * @return
	 * @since 4.0
	 */
	public TaskSet getTasks();

	/**
	 * Check if this target has signal handlers
	 * 
	 * @return
	 * @throws DebugException
	 */
	public boolean hasSignals() throws DebugException;

	/**
	 * Check if the target is little endian
	 * 
	 * @return
	 */
	public boolean isLittleEndian();

	/**
	 * Check if this is post mortem
	 * 
	 * @return
	 */
	public boolean isPostMortem();

	/**
	 * Modify the register group
	 * 
	 * @param group
	 * @param descriptors
	 */
	public void modifyRegisterGroup(IPPersistableRegisterGroup group, IPRegisterDescriptor[] descriptors);

	/**
	 * Remove register groups
	 * 
	 * @param groups
	 */
	public void removeRegisterGroups(IRegisterGroup[] groups);

	/**
	 * Restore default register groups
	 */
	public void restoreDefaultRegisterGroups();
}
