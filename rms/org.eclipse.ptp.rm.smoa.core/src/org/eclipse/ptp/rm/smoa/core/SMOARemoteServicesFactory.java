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

package org.eclipse.ptp.rm.smoa.core;

import org.eclipse.ptp.remote.core.IRemoteServices;
import org.eclipse.ptp.remote.core.IRemoteServicesDescriptor;
import org.eclipse.ptp.remote.core.IRemoteServicesFactory;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOARemoteServices;

/**
 * Passed to proper extension point (in plugin.xml) adds
 * {@link SMOARemoteServices} to the public list of {@link IRemoteSrvices}
 */
public class SMOARemoteServicesFactory implements IRemoteServicesFactory {

	public static final String ID = SMOARemoteServices.class.getName();
	private SMOARemoteServices services;

	public IRemoteServices getServices(IRemoteServicesDescriptor descriptor) {

		if (services == null) {
			services = new SMOARemoteServices(ID, "SMOA Computing"); //$NON-NLS-1$
		}

		return services;
	}

}
