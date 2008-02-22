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

import org.eclipse.ptp.core.util.BitList;

/**
 * @author greg
 *
 */
public interface IPSetManager {

	/**
	 * @param sid
	 * @param tasks
	 */
	public void addTasks(String sid, BitList tasks);

	/**
	 * @param sid
	 * @param tasks
	 */
	public void createSet(String sid, BitList tasks);

	/**
	 * @param sid
	 */
	public void deleteSets(String sid);

	/**
	 * @param sid
	 * @return
	 */
	public BitList getTasks(String sid);

	/**
	 * @param sid
	 * @param tasks
	 */
	public void removeTasks(String sid, BitList tasks);

}
