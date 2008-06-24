/*******************************************************************************
 * Copyright (c) 2008 IBM Corporation.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     IBM Corporation - initial API and implementation
 ******************************************************************************/
package org.eclipse.ptp.rm.mpi.openmpi.core.rmsystem;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

public class OpenMpiResourceManagerFactory extends AbstractResourceManagerFactory {

	public OpenMpiResourceManagerFactory() {
		// QUESTION: Wouldnt it be better to take name from extension point?
		// Extension point already has a name.
		super("Open MPI (new)");
	}

	public IResourceManagerConfiguration copyConfiguration(
			IResourceManagerConfiguration configuration) {
		return (IResourceManagerConfiguration)configuration.clone();
	}

	public IResourceManagerControl create(IResourceManagerConfiguration confIn) {
		OpenMpiResourceManagerConfiguration configuration = (OpenMpiResourceManagerConfiguration) confIn;
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		final IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new OpenMpiResourceManager(universe.getNextResourceManagerId(), universe, configuration);
	}

	public IResourceManagerConfiguration createConfiguration() {
		OpenMpiResourceManagerConfiguration conf = new OpenMpiResourceManagerConfiguration(this);

		// QUESTION: if configuration constructor with no arguments uses defaults, then there is 
		// no need to apply defaults again.
//		
//		Preferences preferences = OpenMpiPreferenceManager.getPreferences();
//		
//		conf.setLaunchCmd(preferences.getString(OpenMpiPreferenceManager.PREFS_LAUNCH_CMD));
//		conf.setPath(preferences.getString(OpenMpiPreferenceManager.PREFS_PATH));
//		conf.setOptions(preferences.getInt(OpenMpiPreferenceManager.PREFS_OPTIONS));

		return conf;
	}

	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		return OpenMpiResourceManagerConfiguration.load(this, memento);
	}

}
