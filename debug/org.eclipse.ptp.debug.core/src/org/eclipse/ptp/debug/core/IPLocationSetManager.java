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
package org.eclipse.ptp.debug.core;

import org.eclipse.ptp.debug.core.model.IPLocationSet;

/**
 * Interface for managing location sets
 * 
 * @since 5.0
 */
public interface IPLocationSetManager {

	/**
	 * Get all know location sets
	 * 
	 * @return array of location sets
	 */
	public IPLocationSet[] getLocationSets();

}
