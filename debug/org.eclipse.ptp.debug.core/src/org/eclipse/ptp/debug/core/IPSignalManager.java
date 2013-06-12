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
import org.eclipse.ptp.debug.core.model.IPSignal;
import org.eclipse.ptp.debug.core.pdi.model.IPDISignal;

/**
 * Interface for managing signal handling in the debugger
 * 
 */
public interface IPSignalManager {

	/**
	 * Dispose of any resources
	 * 
	 * @param qTasks
	 * @since 4.0
	 */
	public void dispose(TaskSet qTasks);

	/**
	 * Get the debugger signal handlers for the tasks
	 * 
	 * @param qTasks
	 * @return
	 * @throws DebugException
	 * @since 4.0
	 */
	public IPSignal[] getSignals(TaskSet qTasks) throws DebugException;

	/**
	 * Notify tasks that a signal handler has changed
	 * 
	 * @param qTasks
	 * @param pdiSignal
	 * @since 4.0
	 */
	public void signalChanged(TaskSet qTasks, IPDISignal pdiSignal);

}
