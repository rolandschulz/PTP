/*******************************************************************************
 * Copyright (c) 2011 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM - Initial API and implementation
 *******************************************************************************/

package org.eclipse.ptp.rmsystem;

import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Factory to create a resource manager. This class can be used to create either the control or monitor sides of a resource manager
 * as well as the base resource manager itself.
 * 
 * @since 5.0
 * 
 */
@Deprecated
public abstract class AbstractResourceManagerFactory implements IResourceManagerFactory, IResourceManagerControlFactory,
		IResourceManagerMonitorFactory {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#create(org.eclipse.ptp .rmsystem.IResourceManagerConfiguration,
	 * org.eclipse.ptp.rmsystem.IResourceManagerControl, org.eclipse.ptp.rmsystem.IResourceManagerMonitor)
	 */
	public IResourceManager create(IResourceManagerConfiguration configuration, IResourceManagerControl control,
			IResourceManagerMonitor monitor) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#createConfiguration( org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public IResourceManagerConfiguration createConfiguration(IServiceProvider provider) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControlFactory#createControl
	 * (org.eclipse.ptp.rmsystem.IResourceManagerControlConfiguration)
	 */
	public IResourceManagerControl createControl(IResourceManagerComponentConfiguration configuration) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerControlFactory# createControlConfiguration
	 * (org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public IResourceManagerComponentConfiguration createControlConfiguration(IServiceProvider provider) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitorFactory#createMonitor
	 * (org.eclipse.ptp.rmsystem.IResourceManagerMonitorConfiguration)
	 */
	public IResourceManagerMonitor createMonitor(IResourceManagerComponentConfiguration configuration) {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerMonitorFactory# createMonitorConfiguration
	 * (org.eclipse.ptp.services.core.IServiceProvider)
	 */
	public IResourceManagerComponentConfiguration createMonitorConfiguration(IServiceProvider provider) {
		return null;
	}

}
