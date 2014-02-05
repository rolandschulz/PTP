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
	public static final String CONNECTION_NAME = "ConnectionName"; //$NON-NLS-1$
	public static final String REMOTE_SERVICES = "RemoteServices"; //$NON-NLS-1$
	public static final String PROJECT_NAME = "Project"; //$NON-NLS-1$

	protected String fRemoteServices;
	protected String fConnectionName;
	protected String fProjectName;

	public RemoteSettings() {
	}

	public String getConnectionName() {
		return fConnectionName;
	}

	public String getRemoteServices() {
		return fRemoteServices;
	}

	public String getProjectName() {
		return fProjectName;
	}

	public String getSummary() {
		return "Remote:" + getRemoteServices() + '_' + getConnectionName(); //$NON-NLS-1$
	}

	@Override
	public String toString() {
		return getSummary();
	}

	/**
	 * Load information into the RemoteSettings object.
	 */
	@SuppressWarnings("restriction")
	public void load(ISettingsStore store) {
		fRemoteServices = store.get(REMOTE_SERVICES);
		fConnectionName = store.get(CONNECTION_NAME);
		fProjectName = store.get(PROJECT_NAME);
	}

	/**
	 * Extract information from the RemoteSettings object.
	 */
	@SuppressWarnings("restriction")
	public void save(ISettingsStore store) {
		store.put(REMOTE_SERVICES, fRemoteServices);
		store.put(CONNECTION_NAME, fConnectionName);
		store.put(PROJECT_NAME, fProjectName);
	}

	public void setConnectionName(String name) {
		fConnectionName = name;
	}

	public void setRemoteServices(String remoteServices) {
		fRemoteServices = remoteServices;
	}

	public void setProjectName(String projectName) {
		fProjectName = projectName;
	}
}
