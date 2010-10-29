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
package org.eclipse.ptp.debug.core.pdi;

import org.eclipse.ptp.debug.core.TaskSet;

/**
 * @since 5.0
 */
public interface IPDILocationSet {
	/**
	 * @return
	 */
	public TaskSet getTasks();

	/**
	 * @return
	 */
	public IPDILocator getLocator();
}
