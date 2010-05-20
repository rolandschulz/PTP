/*******************************************************************************
 * Copyright (c) 2009, 2010 IBM Corporation and others.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation 
 *     Albert L. Rossi (NCSA) - full implementation (bug 310188)
 *     						  - modifications to store template and memento
 *     							(05/11/2010)
 *******************************************************************************/
package org.eclipse.ptp.rm.pbs.ui;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rm.core.rmsystem.AbstractRemoteResourceManagerServiceProvider;
import org.eclipse.ptp.rm.pbs.core.PBSPreferenceManager;
import org.eclipse.ptp.rm.pbs.core.rmsystem.IPBSResourceManagerConfiguration;
import org.eclipse.ptp.rm.pbs.core.rmsystem.PBSResourceManager;
import org.eclipse.ptp.rm.pbs.ui.messages.Messages;
import org.eclipse.ptp.rm.pbs.ui.utils.ConfigUtils;
import org.eclipse.ptp.services.core.IServiceProvider;
import org.eclipse.ptp.services.core.IServiceProviderWorkingCopy;

/**
 * Service provider for IBM Parallel Environment
 */
public class PBSServiceProvider extends AbstractRemoteResourceManagerServiceProvider implements IPBSResourceManagerConfiguration {
	private final Preferences preferences = PBSPreferenceManager.getPreferences();

	public PBSServiceProvider() {
		super();
		setDescription(Messages.PBSResourceManager);
	}

	/**
	 * Constructor for creating a working copy of the service provider
	 * 
	 * @param provider
	 *            provider we are making a copy from
	 */
	public PBSServiceProvider(IServiceProvider provider) {
		super(provider);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ptp.services.core.ServiceProvider#copy()
	 */
	@Override
	public IServiceProviderWorkingCopy copy() {
		return new PBSServiceProvider(this);
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
		return new PBSResourceManager(Integer.valueOf(universe.getNextResourceManagerId()), universe, this);
	}

	/**
	 * @return name of the default template (file) for this resource manager
	 *         (set in the edit wizard).
	 * @since 4.0
	 */
	public String getDefaultTemplateName() {
		return getString(getResourceManagerId() + Messages.PBSServiceProvider_defaultTemplateName, ConfigUtils.EMPTY_STRING);
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
	 * @see org.eclipse.ptp.services.core.IServiceProvider#isConfigured()
	 */
	@Override
	public boolean isConfigured() {
		return true;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see
	 * org.eclipse.ptp.rmsystem.IResourceManagerConfiguration#setDefaultNameAndDesc
	 * ()
	 */
	public void setDefaultNameAndDesc() {
		String name = "PBS"; //$NON-NLS-1$
		String conn = getConnectionName();
		if (conn != null && !conn.equals("")) //$NON-NLS-1$
			name += "@" + conn; //$NON-NLS-1$
		setName(name);
		setDescription(Messages.PBSResourceManager);
	}

	/**
	 * @param name
	 *            of the default template (file) for this resource manager (set
	 *            in the edit wizard).
	 * @since 4.0
	 */
	public void setDefaultTemplateName(String name) {
		putString(getResourceManagerId() + Messages.PBSServiceProvider_defaultTemplateName, name);
	}

}
