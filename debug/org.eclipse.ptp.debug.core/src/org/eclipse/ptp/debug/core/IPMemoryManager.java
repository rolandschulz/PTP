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
import org.eclipse.ptp.debug.core.model.IPDebugTarget;

/**
 * Interface for managing memory blocks
 * 
 */
public interface IPMemoryManager {

	/**
	 * @param qTasks
	 * @since 4.0
	 */
	public void dispose(TaskSet qTasks);

	/**
	 * @param qTasks
	 * @param startAddress
	 * @param length
	 * @return
	 * @throws DebugException
	 * @since 4.0
	 */
	public IMemoryBlock getMemoryBlock(TaskSet qTasks, long startAddress, long length) throws DebugException;

	/**
	 * @param qTasks
	 * @return
	 * @since 4.0
	 */
	public IMemoryBlockRetrievalExtension getMemoryRetrieval(TaskSet qTasks);

	/**
	 * Initialize memory manager
	 * 
	 * @param qTasks
	 * @param debugTarget
	 * @since 5.0
	 */
	public void initialize(TaskSet qTasks, IPDebugTarget debugTarget);

	/**
	 * @param qTasks
	 * @since 4.0
	 */
	public void save(TaskSet qTasks);

}
