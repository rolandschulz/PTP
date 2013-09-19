/*******************************************************************************
 * Copyright (c) 2007 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * IBM Corporation - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.rse.core;

import org.eclipse.remote.core.IRemoteServices;
import org.eclipse.remote.core.IRemoteServicesDescriptor;
import org.eclipse.remote.core.IRemoteServicesFactory;

public class RSEServicesFactory implements IRemoteServicesFactory {
	private static RSEServices services = null;

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.remote.core.IRemoteServicesFactory#getServices(org.eclipse.remote.core.IRemoteServicesDescriptor)
	 */
	public IRemoteServices getServices(IRemoteServicesDescriptor descriptor) {
		if (services == null) {
			services = new RSEServices(descriptor);
		}
		return services;
	}
}
