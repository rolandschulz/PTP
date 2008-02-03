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

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.PDIException;

/**
 * Base manager interfaces
 * 
 * @author greg
 *
 */
public interface IPDIManager extends IPDISessionObject {
	/**
	 * @return
	 */
	public boolean isAutoUpdate();
	
	/**
	 * @param update
	 */
	public void setAutoUpdate(boolean update);
	
	/**
	 * 
	 */
	public void shutdown();
	
	/**
	 * @param tasks
	 * @throws PDIException
	 */
	public void update(BitList tasks) throws PDIException;
}
