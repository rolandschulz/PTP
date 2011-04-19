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

import java.util.UUID;

import org.eclipse.ptp.services.core.ServiceProvider;

/**
 * Base service provider for all resource manager configurations that use the
 * service provider framework. A resource manager must declare a service
 * provider extension and point the extension at this class.
 * 
 * @since 5.0
 */
public class ResourceManagerServiceProvider extends ServiceProvider {

	private static final String TAG_UNIQUE_NAME = "uniqName"; //$NON-NLS-1$

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerServiceProvider#getUniqueName()
	 */
	public String getUniqueName() {
		String name = getString(TAG_UNIQUE_NAME, null);
		if (name == null) {
			name = UUID.randomUUID().toString();
			putString(TAG_UNIQUE_NAME, name);
		}
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	public boolean isConfigured() {
		return false;
	}
}
