/*******************************************************************************
 * Copyright (c) 2006 The Regents of the University of California. 
 * This material was produced under U.S. Government contract W-7405-ENG-36 
 * for Los Alamos National Laboratory, which is operated by the University 
 * of California for the U.S. Department of Energy. The U.S. Government has 
 * rights to use, reproduce, and distribute this software. NEITHER THE 
 * GOVERNMENT NOR THE UNIVERSITY MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR 
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified 
 * to produce derivative works, such modified software should be clearly marked, 
 * so as not to confuse it with the version available from LANL.
 * 
 * Additionally, this program and the accompanying materials 
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * LA-CC 04-115
 *******************************************************************************/
package org.eclipse.ptp.rm.ompi.core.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rm.ompi.core.OMPIPreferenceManager;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

public class OMPIResourceManagerFactory extends AbstractResourceManagerFactory {

	public OMPIResourceManagerFactory() {
		super("Open MPI");
	}

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
		OMPIResourceManagerConfiguration configuration = (OMPIResourceManagerConfiguration) confIn;
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		final IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new OMPIResourceManager(universe.getNextResourceManagerId(), universe, configuration);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#createConfiguration()
	 */
	public IResourceManagerConfiguration createConfiguration() {
		OMPIResourceManagerConfiguration conf = new OMPIResourceManagerConfiguration(this);
		
		Preferences preferences = OMPIPreferenceManager.getPreferences();
		
		conf.setLaunchCmd(preferences.getString(OMPIPreferenceManager.PREFS_LAUNCH_CMD));
		conf.setPath(preferences.getString(OMPIPreferenceManager.PREFS_PATH));
		conf.setOptions(preferences.getInt(OMPIPreferenceManager.PREFS_OPTIONS));

		return conf;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.IResourceManagerFactory#loadConfiguration(org.eclipse.ui.IMemento)
	 */
	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		return OMPIResourceManagerConfiguration.load(this, memento);
	}

}