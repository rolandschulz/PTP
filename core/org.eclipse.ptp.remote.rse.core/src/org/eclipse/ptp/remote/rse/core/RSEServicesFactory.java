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
package org.eclipse.ptp.remote.rse.core;

import org.eclipse.ptp.remote.core.IRemoteServicesDelegate;
import org.eclipse.ptp.remote.core.IRemoteServicesFactory;

public class RSEServicesFactory implements IRemoteServicesFactory {
	private static RSEServices services = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesFactory#getServices()
	 */
	public IRemoteServicesDelegate getServices() {
		if (services == null) {
			services = new RSEServices();
		}
		return services;
	}
}
