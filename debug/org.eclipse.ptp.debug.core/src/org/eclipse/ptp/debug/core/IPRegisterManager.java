/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core;

import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.model.IPPersistableRegisterGroup;
import org.eclipse.ptp.debug.core.model.IPRegisterDescriptor;
import org.eclipse.ptp.debug.core.model.IPStackFrame;

/**
 * Interface for managing registers
 * 
 */
public interface IPRegisterManager {

	/**
	 * @param qTasks
	 * @param name
	 * @param descriptors
	 * @since 4.0
	 */
	public void addRegisterGroup(final TaskSet qTasks, final String name, final IPRegisterDescriptor[] descriptors);

	/**
	 * @param qTasks
	 * @return
	 * @throws DebugException
	 * @since 4.0
	 */
	public IPRegisterDescriptor[] getAllRegisterDescriptors(TaskSet qTasks) throws DebugException;

	/**
	 * @param qTasks
	 * @return
	 * @since 4.0
	 */
	public IPStackFrame getCurrentFrame(TaskSet qTasks);

	/**
	 * @param qTasks
	 * @param frame
	 * @return
	 * @throws DebugException
	 * @since 5.0
	 */
	public IRegisterGroup[] getRegisterGroups(TaskSet qTasks, IPStackFrame frame) throws DebugException;

	/**
	 * @param qTasks
	 * @param debugTarget
	 * @since 5.0
	 */
	public void initialize(TaskSet qTasks, IPDebugTarget debugTarget);

	/**
	 * @param qTasks
	 * @param group
	 * @param descriptors
	 * @since 4.0
	 */
	public void modifyRegisterGroup(final TaskSet qTasks, final IPPersistableRegisterGroup group,
			final IPRegisterDescriptor[] descriptors);

	/**
	 * @param qTasks
	 * @param groups
	 * @since 4.0
	 */
	public void removeRegisterGroups(final TaskSet qTasks, final IRegisterGroup[] groups);

	/**
	 * @param qTasks
	 * @since 4.0
	 */
	public void restoreDefaults(final TaskSet qTasks);

	/**
	 * @param qTasks
	 * @since 4.0
	 */
	public void targetSuspended(TaskSet qTasks);

}
