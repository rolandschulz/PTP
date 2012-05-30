/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rtsystem;

import org.eclipse.ptp.rmsystem.IResourceManager;

/**
 * @since 5.0
 * 
 */
@Deprecated
public interface IRuntimeSystemFactory {
	/**
	 * Create a runtime system using the supplied configuration.
	 * 
	 * @param rm
	 *            resource manager this runtime system is for
	 * @return runtime system
	 * @since 5.0
	 */
	public IRuntimeSystem create(IResourceManager rm);
}
