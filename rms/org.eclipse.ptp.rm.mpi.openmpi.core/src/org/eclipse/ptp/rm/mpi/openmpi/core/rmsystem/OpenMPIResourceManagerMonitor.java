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
package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.AbstractToolResourceManagerMonitor;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;

/**
 * @since 4.0
 */
public class OpenMPIResourceManagerMonitor extends AbstractToolResourceManagerMonitor {
	public OpenMPIResourceManagerMonitor(IResourceManagerConfiguration config) {
		super(config);
	}
}
