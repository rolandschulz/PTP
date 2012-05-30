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
package org.eclipse.ptp.rmsystem;

import org.eclipse.ptp.services.core.IServiceProvider;

/**
 * Factory to create a resource manager monitor.
 * 
 * @since 5.0
 * 
 */
@Deprecated
public interface IResourceManagerMonitorFactory {
	/**
	 * Create a monitor configuration using the supplied service provider
	 * 
	 * @param provider
	 *            service provider
	 * @return monitor configuration
	 */
	public IResourceManagerComponentConfiguration createMonitorConfiguration(IServiceProvider provider);

	/**
	 * Create a resource manager monitor using the supplied configuration.
	 * 
	 * @param configuration
	 *            configuration to use when creating resource manager
	 * @return resource manager monitor
	 * @since 5.0
	 */
	public IResourceManagerMonitor createMonitor(IResourceManagerComponentConfiguration configuration);
}
