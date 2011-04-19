/*******************************************************************************
 * Copyright (c) 2010 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.generic.core.rmsystem;

import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMConfiguration;
import org.eclipse.ptp.rm.generic.core.GenericRMCorePlugin;
import org.eclipse.ptp.rm.generic.core.GenericRMPreferenceManager;
import org.eclipse.ptp.rm.generic.core.messages.Messages;
import org.eclipse.ptp.services.core.IServiceProvider;

public class GenericRMConfiguration extends AbstractToolRMConfiguration {

	public static int CAPABILITIES = CAP_LAUNCH | CAP_REMOTE_INSTALL_PATH;

	public GenericRMConfiguration(String namespace, IServiceProvider provider) {
		super(CAPABILITIES, namespace, provider);
		setLaunchCmd(Preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(), GenericRMPreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(Preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(), GenericRMPreferenceManager.PREFS_DEBUG_CMD));
		setRemoteInstallPath(Preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(),
				GenericRMPreferenceManager.PREFS_REMOTE_INSTALL_PATH));

		setUseInstallDefaults(true);
		setUseToolDefaults(true);
		setCommandsEnabled(false);
		setDescription(Messages.GenericRMServiceProvider_defaultDescription);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc
	 * ()
	 */
	public void setDefaultNameAndDesc() {
		String name = Messages.GenericRMServiceProvider_defaultName;
		String conn = getConnectionName();
		if (conn != null && !conn.equals(EMPTY_STRING)) {
			name += "@" + conn; //$NON-NLS-1$
		}
		setName(name);
		setDescription(Messages.GenericRMServiceProvider_defaultDescription);
	}
}
