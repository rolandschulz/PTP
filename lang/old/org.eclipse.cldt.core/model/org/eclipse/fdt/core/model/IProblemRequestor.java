/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/

package org.eclipse.fdt.core.model;

import org.eclipse.fdt.core.parser.IProblem;

/**
 * A callback interface for receiving problem as they are discovered
 * IProblemRequestor
 */

public interface IProblemRequestor {
	
	/**
	 * Notification of a Java problem.
	 * 
	 * @param problem IProblem - The discovered Java problem.
	 */	
	void acceptProblem(IProblem problem);

	/**
	 * Notification sent before starting the problem detection process.
	 * Typically, this would tell a problem collector to clear previously recorded problems.
	 */
	void beginReporting();

	/**
	 * Notification sent after having completed problem detection process.
	 * Typically, this would tell a problem collector that no more problems should be expected in this
	 * iteration.
	 */
	void endReporting();

	/**
	 * Predicate allowing the problem requestor to signal whether or not it is currently
	 * interested by problem reports. When answering <code>false</false>, problem will
	 * not be discovered any more until the next iteration.
	 * 
	 * This  predicate will be invoked once prior to each problem detection iteration.
	 * 
	 * @return boolean - indicates whether the requestor is currently interested by problems.
	 */
	boolean isActive();
}
