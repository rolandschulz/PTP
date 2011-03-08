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
package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import org.eclipse.ptp.rm.core.rmsystem.AbstractToolResourceManager;
import org.eclipse.ptp.rm.mpi.openmpi.core.parameters.OmpiInfo;
import org.eclipse.ptp.rm.mpi.openmpi.core.rtsystem.OpenMPIRuntimeSystem;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerControl;
import org.eclipse.ptp.rtsystem.AbstractRuntimeResourceManagerMonitor;
import org.eclipse.ptp.rtsystem.IRuntimeSystem;

/**
 * 
 * @author Daniel Felix Ferber
 * 
 */
public class OpenMPIResourceManager extends AbstractToolResourceManager {
	/**
	 * @since 4.0
	 */
	public OpenMPIResourceManager(IResourceManagerConfiguration config, AbstractRuntimeResourceManagerControl control,
			AbstractRuntimeResourceManagerMonitor monitor) {
		super(config, control, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractRuntimeResourceManager#doCreateRuntimeSystem
	 * ()
	 */
	@Override
	protected IRuntimeSystem doCreateRuntimeSystem() {
		IOpenMPIResourceManagerConfiguration config = (IOpenMPIResourceManagerConfiguration) getConfiguration();
		return new OpenMPIRuntimeSystem(this, config);
	}

	/**
	 * Get OpenMPI info
	 * 
	 * @return OmpiInfo
	 */
	public OmpiInfo getOmpiInfo() {
		IRuntimeSystem rts = getRuntimeSystem();
		if (rts == null || !(rts instanceof OpenMPIRuntimeSystem)) {
			return null;
		}
		try {
			return ((OpenMPIRuntimeSystem) rts).getOmpiInfo().clone();
		} catch (CloneNotSupportedException e) {
			return null;
		}
	}
}
