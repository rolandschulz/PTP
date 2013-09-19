/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public class RemoteSettings implements IRemoteSettings {
	protected String fRemoteServices;
	protected String fConnectionName;

	public String getConnectionName() {
		return fConnectionName;
	}

	public String getRemoteServices() {
		return fRemoteServices;
	}

	public String getSummary() {
		return getRemoteServices() + '_' + getConnectionName();
	}

	public void load(ISettingsStore store) {
		fRemoteServices = store.get("RemoteServices");//$NON-NLS-1$
		fConnectionName = store.get("ConnectionName");//$NON-NLS-1$
	}

	public void save(ISettingsStore store) {
		store.put("RemoteServices", fRemoteServices);//$NON-NLS-1$
		store.put("ConnectionName", fConnectionName);//$NON-NLS-1$
	}

	public void setConnectionName(String name) {
		fConnectionName = name;
	}

	public void setRemoteServices(String remoteServices) {
		fRemoteServices = remoteServices;
	}
}
