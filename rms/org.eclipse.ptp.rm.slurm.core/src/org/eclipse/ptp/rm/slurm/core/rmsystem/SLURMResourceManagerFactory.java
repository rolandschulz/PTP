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
package org.eclipse.ptp.rm.slurm.core.rmsystem;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManager;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.IResourceManagerMonitor;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * @since 5.0
 */
public class SLURMResourceManagerFactory extends AbstractResourceManagerFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#create(org.eclipse
	 * .ptp.rmsystem.IResourceManagerConfiguration,
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl,
	 * org.eclipse.ptp.rmsystem.IResourceManagerMonitor)
	 */
	@Override
	public IResourceManager create(IResourceManagerConfiguration configuration, IResourceManagerControl control,
			IResourceManagerMonitor monitor) {
		return new SLURMResourceManager((SLURMResourceManagerConfiguration) configuration, (SLURMResourceManagerControl) control,
				(SLURMResourceManagerMonitor) monitor);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#createConfiguration
	 * (org.eclipse.ptp.services.core.IServiceProvider)
	 */
	@Override
	public IResourceManagerConfiguration createConfiguration(IServiceProvider provider) {
		return new SLURMResourceManagerConfiguration(SLURMResourceManagerConfiguration.BASE, provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#createControl
	 * (org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration)
	 */
	@Override
	public IResourceManagerControl createControl(IResourceManagerComponentConfiguration configuration) {
		return new SLURMResourceManagerControl((SLURMResourceManagerConfiguration) configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#
	 * createControlConfiguration
	 * (org.eclipse.ptp.services.core.IServiceProvider)
	 */
	@Override
	public IResourceManagerComponentConfiguration createControlConfiguration(IServiceProvider provider) {
		return new SLURMResourceManagerConfiguration(SLURMResourceManagerConfiguration.BASE, provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#createMonitor
	 * (org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration)
	 */
	@Override
	public IResourceManagerMonitor createMonitor(IResourceManagerComponentConfiguration configuration) {
		return new SLURMResourceManagerMonitor((SLURMResourceManagerConfiguration) configuration);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#
	 * createMonitorConfiguration
	 * (org.eclipse.ptp.services.core.IServiceProvider)
	 */
	@Override
	public IResourceManagerComponentConfiguration createMonitorConfiguration(IServiceProvider provider) {
		return new SLURMResourceManagerConfiguration(SLURMResourceManagerConfiguration.BASE, provider);
	}
}
