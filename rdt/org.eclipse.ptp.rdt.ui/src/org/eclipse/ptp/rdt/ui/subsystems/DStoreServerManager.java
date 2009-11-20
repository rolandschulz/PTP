/*******************************************************************************
 * Copyright (c) 2009 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
package org.eclipse.ptp.rdt.ui.subsystems;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ptp.remote.core.IRemoteConnection;
import org.eclipse.ptp.remote.core.IRemoteServices;

public class DStoreServerManager {
	private final static Map<String, DStoreServerRunner> fServers = new HashMap<String, DStoreServerRunner>();
	
	private static DStoreServerManager fInstance;
	
	public static synchronized DStoreServerManager getInstance() {
		if(fInstance == null) {
			fInstance = new DStoreServerManager();
		}
		return fInstance;
	}
	
	private String getKey(IRemoteServices services, IRemoteConnection connection) {
		return services.getId() + connection.getName();
	}
	
	public DStoreServerRunner getServer(IRemoteServices services, IRemoteConnection connection) {
		DStoreServerRunner server = fServers.get(getKey(services, connection));
		if (server == null) {
			server = new DStoreServerRunner(services, connection);
			fServers.put(getKey(services, connection), server);
		}
		return server;
	}
}
