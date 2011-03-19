/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.AbstractToolResourceManager;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class MPICH2ResourceManager extends AbstractToolResourceManager {

	/**
	 * @since 2.0
	 */
	public MPICH2ResourceManager(MPICH2ResourceManagerConfiguration config, MPICH2ResourceManagerControl control,
			MPICH2ResourceManagerMonitor monitor) {
		super(config, control, monitor);
	}
}
