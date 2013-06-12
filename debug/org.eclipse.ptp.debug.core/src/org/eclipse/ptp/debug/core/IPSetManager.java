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

/**
 * Interface to manage task sets
 * 
 */
public interface IPSetManager {

	/**
	 * Add tasks to the set identified by sid
	 * 
	 * @param sid
	 * @param tasks
	 * @since 4.0
	 */
	public void addTasks(String sid, TaskSet tasks);

	/**
	 * Create a new set containing the tasks
	 * 
	 * @param sid
	 * @param tasks
	 * @since 4.0
	 */
	public void createSet(String sid, TaskSet tasks);

	/**
	 * Delete the task set
	 * 
	 * @param sid
	 */
	public void deleteSets(String sid);

	/**
	 * Get the tasks in the set sid
	 * 
	 * @param sid
	 * @return
	 * @since 4.0
	 */
	public TaskSet getTasks(String sid);

	/**
	 * Remove the tasks from the set sid
	 * 
	 * @param sid
	 * @param tasks
	 * @since 4.0
	 */
	public void removeTasks(String sid, TaskSet tasks);
}
