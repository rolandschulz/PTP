/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.core.model;

import org.eclipse.ptp.debug.core.TaskSet;

/**
 * A location set is used to provide information about a location (e.g. a breakpoint location)
 * 
 * @since 5.0
 */
public interface IPLocationSet {
	/**
	 * Get the file name for this location set.
	 * 
	 * @return file name
	 */
	public String getFile();

	/**
	 * Get the function name for this location set.
	 * 
	 * @return function name
	 */
	public String getFunction();

	/**
	 * Get the line number for this location set.
	 * 
	 * @return line number
	 */
	public int getLineNumber();

	/**
	 * Get the tasks associated with this location set.
	 * 
	 * @return task set
	 */
	public TaskSet getTasks();
}
