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
package org.eclipse.ptp.rm.jaxb.core.rm;

import org.eclipse.ptp.rmsystem.AbstractResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerComponentConfiguration;
import org.eclipse.ptp.rmsystem.IResourceManagerMonitor;
import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Create a JAXB resource manager monitor. This is only used for testing
 * purposes.
 * 
 * @since 5.0
 */
public class JAXBResourceManagerMonitorFactory extends AbstractResourceManagerFactory {

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#createMonitor
	 * (org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	@Override
	public IResourceManagerMonitor createMonitor(IResourceManagerComponentConfiguration configuration) {
		return new JAXBResourceManagerMonitor((AbstractResourceManagerConfiguration) configuration);
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
		return new JAXBServiceProvider(JAXBServiceProvider.MONITOR, provider);
	}
}
