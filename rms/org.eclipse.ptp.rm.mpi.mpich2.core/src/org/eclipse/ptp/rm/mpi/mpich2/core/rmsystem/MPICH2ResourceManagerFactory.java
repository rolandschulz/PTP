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
package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerFactory;

/**
 * @since 2.0
 */
public class MPICH2ResourceManagerFactory implements IResourceManagerFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerFactory#create(org.eclipse.ptp
	 * .rmsystem.IResourceManagerConfiguration)
	 */
	public IResourceManager create(IResourceManagerConfiguration configuration) {
		MPICH2ResourceManagerControl control = new MPICH2ResourceManagerControl(configuration);
		MPICH2ResourceManagerMonitor monitor = new MPICH2ResourceManagerMonitor(configuration);
		return new MPICH2ResourceManager(configuration, control, monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerFactory#createConfiguration()
	 */
	public IResourceManagerConfiguration createConfiguration() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#getId()
	 */
	public String getId() {
		// TODO Auto-generated method stub
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#getName()
	 */
	public String getName() {
		// TODO Auto-generated method stub
		return null;
	}
}
