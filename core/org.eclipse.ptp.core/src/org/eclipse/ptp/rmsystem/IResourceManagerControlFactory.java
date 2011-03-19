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
 * Factory to create a resource manager control.
 * 
 * @since 5.0
 * 
 */
public interface IResourceManagerControlFactory {
	/**
	 * Create a control configuration using the supplied service provider
	 * 
	 * @param provider
	 *            service provider
	 * @return control configuration
	 */
	public IResourceManagerComponentConfiguration createControlConfiguration(IServiceProvider provider);

	/**
	 * Create a resource manager control using the supplied configuration.
	 * 
	 * @param configuration
	 *            configuration to use when creating resource manager
	 * @return resource manager control
	 * @since 5.0
	 */
	public IResourceManagerControl createControl(IResourceManagerComponentConfiguration configuration);
}
