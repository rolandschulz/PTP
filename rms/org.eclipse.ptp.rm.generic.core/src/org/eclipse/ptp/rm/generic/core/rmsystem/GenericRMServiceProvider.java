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

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.Preferences;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rm.core.rmsystem.AbstractToolRMServiceProvider;
import org.eclipse.ptp.rm.generic.core.GenericRMCorePlugin;
import org.eclipse.ptp.rm.generic.core.GenericRMPreferenceManager;
import org.eclipse.ptp.rm.generic.core.messages.Messages;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;

public class GenericRMServiceProvider extends AbstractToolRMServiceProvider {

	public static int CAPABILITIES = CAP_LAUNCH | CAP_REMOTE_INSTALL_PATH;

	public GenericRMServiceProvider() {
		super(CAPABILITIES);

		setLaunchCmd(Preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(), GenericRMPreferenceManager.PREFS_LAUNCH_CMD));
		setDebugCmd(Preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(), GenericRMPreferenceManager.PREFS_DEBUG_CMD));
		setRemoteInstallPath(Preferences.getString(GenericRMCorePlugin.getUniqueIdentifier(),
				GenericRMPreferenceManager.PREFS_REMOTE_INSTALL_PATH));

		setUseInstallDefaults(true);
		setUseToolDefaults(true);
		setCommandsEnabled(false);
		setDescription(Messages.GenericRMServiceProvider_defaultDescription);
	}

	/**
	 * Constructor for creating a working copy of the service provider
	 * 
	 * @param provider
	 *            provider we are making a copy from
	 */
	public GenericRMServiceProvider(GenericRMServiceProvider provider) {
		super(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.IServiceProvider#copy()
	 */
	@Override
	public IServiceProviderWorkingCopy copy() {
		return new GenericRMServiceProvider(this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#
	 * createResourceManager()
	 */
	@Override
	public IResourceManagerControl createResourceManager() {
		IPUniverseControl universe = (IPUniverseControl) PTPCorePlugin.getDefault().getUniverse();
		return new GenericResourceManager(Integer.valueOf(universe.getNextResourceManagerId()), universe, this);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerServiceProvider#
	 * getResourceManagerId()
	 */
	@Override
	public String getResourceManagerId() {
		return getId();
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
