/*******************************************************************************
 * Copyright (c) 2010 Poznan Supercomputing and Networking Center
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     Jan Konczak (PSNC) - initial implementation
 ******************************************************************************/

package org.eclipse.ptp.rm.smoa.ui.rservices;

import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.remote.ui.IRemoteUIServicesFactory;

/**
 * Part of extension, creates {@link SMOAUIServices} on demand.
 */
public class SMOAUIServicesFactory implements IRemoteUIServicesFactory {

	public IRemoteUIServices getServices(IRemoteServices services) {
		return SMOAUIServices.getInstance(services);
	}

}
