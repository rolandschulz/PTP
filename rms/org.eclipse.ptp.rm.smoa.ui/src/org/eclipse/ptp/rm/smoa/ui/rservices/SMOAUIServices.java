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
import org.eclipse.ptp.remote.ui.IRemoteUIConnectionManager;
import org.eclipse.ptp.remote.ui.IRemoteUIFileManager;
import org.eclipse.ptp.remote.ui.IRemoteUIServices;
import org.eclipse.ptp.rm.smoa.core.rservices.SMOARemoteServices;

/**
 * Extends PTP for providing GUI access to {@link SMOARemoteServices}.
 */
public class SMOAUIServices implements IRemoteUIServices {

	static private SMOAUIServices instance;

	public static SMOAUIServices getInstance(IRemoteServices services) {
		if (instance == null) {
			instance = new SMOAUIServices(services);
		}
		return instance;
	}

	private final IRemoteServices remoteServices;

	public SMOAUIServices(IRemoteServices services) {
		remoteServices = services;
	}

	public String getId() {
		return remoteServices.getId();
	}

	public String getName() {
		return remoteServices.getName();
	}

	public IRemoteUIConnectionManager getUIConnectionManager() {
		return new SMOAUIConnectionManager();
	}

	public IRemoteUIFileManager getUIFileManager() {
		return new SMOAUIFileManager(remoteServices);
	}

}
