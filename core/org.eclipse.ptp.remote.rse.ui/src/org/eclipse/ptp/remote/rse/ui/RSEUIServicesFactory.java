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
package org.eclipse.ptp.remote.rse.ui;

import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIServicesDelegate;
import org.eclipse.ptp.remote.ui.IRemoteUIServicesFactory;

public class RSEUIServicesFactory implements IRemoteUIServicesFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.remote.ui.IRemoteUIServicesFactory#getServices(org.eclipse.ptp.remote.core.IRemoteServices)
	 */
	public IRemoteUIServicesDelegate getServices(IRemoteServices services) {
		return RSEUIServices.getInstance(services);
	}
}
