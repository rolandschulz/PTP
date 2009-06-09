/*******************************************************************************
 * Copyright (c) 2008,2009 
 * School of Computer, National University of Defense Technology, P.R.China
 *
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 			Peichang Shi <pcmutates@163.com>/<pcshi@nudt.edu.cn>
 *******************************************************************************/
package org.eclipse.ptp.rm.slurm.core.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rm.remote.ui.preferences.PreferenceConstants;
import org.eclipse.ptp.rm.slurm.core.SLURMPreferenceManager;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

public class SLURMResourceManagerFactory extends AbstractResourceManagerFactory {

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#copyConfiguration(org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public IResourceManagerConfiguration copyConfiguration(
			IResourceManagerConfiguration configuration) {
		return (IResourceManagerConfiguration)configuration.clone();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory#create(org.eclipse.ptp.rmsystem.IResourceManagerConfiguration)
	 */
	public IResourceManagerControl create(IResourceManagerConfiguration confIn) {
		SLURMResourceManagerConfiguration configuration = (SLURMResourceManagerConfiguration) confIn;
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		final IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new SLURMResourceManager(universe.getNextResourceManagerId(), universe, configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#createConfiguration()
	 */
	public IResourceManagerConfiguration createConfiguration() {
		SLURMResourceManagerConfiguration conf = new SLURMResourceManagerConfiguration(this);
		
		Preferences preferences = SLURMPreferenceManager.getPreferences();
		
		conf.setProxyServerPath(preferences.getString(PreferenceConstants.PROXY_PATH));
		conf.setOptions(preferences.getInt(PreferenceConstants.OPTIONS));

		return conf;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#loadConfiguration(org.eclipse.ui.IMemento)
	 */
	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		return SLURMResourceManagerConfiguration.load(this, memento);
	}

}
