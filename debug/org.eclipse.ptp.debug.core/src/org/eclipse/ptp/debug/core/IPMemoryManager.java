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
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IMemoryBlockRetrievalExtension;
import org.eclipse.ptp.debug.internal.core.model.PDebugTarget;

/**
 * @author greg
 *
 */
public interface IPMemoryManager {

	/**
	 * @param qTasks
	 */
	public void dispose(TaskSet qTasks);

	/**
	 * @param qTasks
	 * @param startAddress
	 * @param length
	 * @return
	 * @throws DebugException
	 */
	public IMemoryBlock getMemoryBlock(TaskSet qTasks, long startAddress, long length) throws DebugException;

	/**
	 * @param qTasks
	 * @return
	 */
	public IMemoryBlockRetrievalExtension getMemoryRetrieval(TaskSet qTasks);

	/**
	 * @param qTasks
	 * @param debugTarget
	 */
	public void initialize(TaskSet qTasks, PDebugTarget debugTarget);

	/**
	 * @param qTasks
	 */
	public void save(TaskSet qTasks);

}
