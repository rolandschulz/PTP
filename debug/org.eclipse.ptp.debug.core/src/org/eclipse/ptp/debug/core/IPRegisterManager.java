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
import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.model.IPPersistableRegisterGroup;
import org.eclipse.ptp.debug.core.model.IPRegisterDescriptor;
import org.eclipse.ptp.debug.core.model.IPStackFrame;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;
import org.eclipse.ptp.debug.internal.core.model.PStackFrame;

/**
 * @author greg
 *
 */
public interface IPRegisterManager {

	/**
	 * @param qTasks
	 * @param name
	 * @param descriptors
	 */
	public void addRegisterGroup(final BitList qTasks, final String name, final IPRegisterDescriptor[] descriptors);

	/**
	 * @param qTasks
	 * @return
	 * @throws DebugException
	 */
	public IPRegisterDescriptor[] getAllRegisterDescriptors(BitList qTasks) throws DebugException;

	/**
	 * @param qTasks
	 * @return
	 */
	public IPStackFrame getCurrentFrame(BitList qTasks);

	/**
	 * @param qTasks
	 * @param frame
	 * @return
	 * @throws DebugException
	 */
	public IRegisterGroup[] getRegisterGroups(BitList qTasks, PStackFrame frame) throws DebugException;

	/**
	 * @param qTasks
	 * @param debugTarget
	 */
	public void initialize(BitList qTasks, PDebugTarget debugTarget);

	/**
	 * @param qTasks
	 * @param group
	 * @param descriptors
	 */
	public void modifyRegisterGroup(final BitList qTasks, final IPPersistableRegisterGroup group,
			final IPRegisterDescriptor[] descriptors);

	/**
	 * @param qTasks
	 * @param groups
	 */
	public void removeRegisterGroups(final BitList qTasks, final IRegisterGroup[] groups);

	/**
	 * @param qTasks
	 */
	public void restoreDefaults(final BitList qTasks);

	/**
	 * @param qTasks
	 */
	public void targetSuspended(BitList qTasks);

}
