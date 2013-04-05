/*******************************************************************************
 * Copyright (c) 2012 IBM and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0 
 * which accompanies this distribution, and is available at 
 * http://www.eclipse.org/legal/epl-v10.html 
 *******************************************************************************/
package org.eclipse.ptp.internal.remote.terminal;

import org.eclipse.tm.internal.terminal.provisional.api.ISettingsStore;

public interface IRemoteToolsSettings {
	/**
	 * Get the host name or IP address of remote system to connect.
	 * 
	 * @return host name or IP address of the remote system.
	 */
	String getRemoteServices();

	/**
	 * Get the login name for connecting to the remote system.
	 * 
	 * @return remote login name
	 */
	String getConnectionName();

	/**
	 * Return a human-readable String summarizing all relevant connection data. This String can be displayed in the Terminal
	 * caption, for instance.
	 * 
	 * @return a human-readable String summarizing relevant connection data.
	 */
	String getSummary();

	/**
	 * Load connection data from a settings store.
	 * 
	 * @param store
	 *            the settings store to access.
	 */
	void load(ISettingsStore store);

	/**
	 * Store connection data into a settings store.
	 * 
	 * @param store
	 *            the settings store to access.
	 */
	void save(ISettingsStore store);
}
