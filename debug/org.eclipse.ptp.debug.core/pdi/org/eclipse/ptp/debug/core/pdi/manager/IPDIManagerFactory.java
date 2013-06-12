/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.pdi.manager;

import org.eclipse.ptp.debug.core.pdi.IPDISession;

/**
 * Factory for creating PDI elements
 * 
 */
public interface IPDIManagerFactory {
	/**
	 * Create breakpoint manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDIBreakpointManager newBreakpointManager(IPDISession session);

	/**
	 * Create session manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDIEventManager newEventManager(IPDISession session);

	/**
	 * Create event request manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDIEventRequestManager newEventRequestManager(IPDISession session);

	/**
	 * Create expression manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDIExpressionManager newExpressionManager(IPDISession session);

	/**
	 * Create memory manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDIMemoryManager newMemoryManager(IPDISession session);

	/**
	 * Create register manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDIRegisterManager newRegisterManager(IPDISession session);

	/**
	 * Create signal manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDISignalManager newSignalManager(IPDISession session);

	/**
	 * Create source manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDISourceManager newSourceManager(IPDISession session);

	/**
	 * Create target manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDITargetManager newTargetManager(IPDISession session);

	/**
	 * Create task manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDITaskManager newTaskManager(IPDISession session);

	/**
	 * Create thread manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDIThreadManager newThreadManager(IPDISession session);

	/**
	 * Create variable manager
	 * 
	 * @param session
	 * @return
	 */
	public IPDIVariableManager newVariableManager(IPDISession session);
}
