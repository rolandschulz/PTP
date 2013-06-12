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

import org.eclipse.ptp.debug.core.TaskSet;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Base manager interfaces
 * 
 */
public interface IPDIManager extends IPDISessionObject {
	/**
	 * Test if auto update is set
	 * 
	 * @return
	 */
	public boolean isAutoUpdate();

	/**
	 * Set auto update
	 * 
	 * @param update
	 */
	public void setAutoUpdate(boolean update);

	/**
	 * Shut down the manager
	 */
	public void shutdown();

	/**
	 * Update the tasks
	 * 
	 * @param tasks
	 * @throws PDIException
	 * @since 4.0
	 */
	public void update(TaskSet tasks) throws PDIException;
}
