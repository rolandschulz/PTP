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
package org.eclipse.ptp.internal.remote;

import org.eclipse.ptp.remote.IRemoteServicesDelegate;
import org.eclipse.ptp.remote.IRemoteServicesFactory;

public class LocalServicesFactory implements IRemoteServicesFactory {
	private static LocalServices services = null;
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.IRemoteServicesFactory#getServices()
	 */
	public IRemoteServicesDelegate getServices() {
		if (services == null) {
			services = new LocalServices();
		}
		return services;
	}
}
