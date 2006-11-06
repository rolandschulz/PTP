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
package org.eclipse.ptp.rmsystem;

import org.eclipse.core.runtime.Preferences;
import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ui.IMemento;

public class SimulationResourceManagerFactory extends
		AbstractResourceManagerFactory {
	
	public SimulationResourceManagerFactory() {
		super(Messages.getString("SimulationResourceManagerFactory.ResourceManagerFactoryName")); //$NON-NLS-1$
	}

	public IResourceManager create(IResourceManagerConfiguration confIn) {
		SimulationRMConfiguration configuration = (SimulationRMConfiguration) confIn;
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		Preferences p = plugin.getPluginPreferences();
		//p.setValue(PreferenceConstants.MONITORING_SYSTEM_SELECTION, MSI);
		//p.setValue(PreferenceConstants.CONTROL_SYSTEM_SELECTION, CSI);
		p.setValue(PreferenceConstants.DEVELOPER_MODE, true);
		plugin.savePluginPreferences();
//		ModelManager manager = new ModelManager(configuration.getNumMachines(),
//				configuration.getNumNodesPerMachines());
		final IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new SimulationResourceManager(universe, configuration);
	}

	public IResourceManagerConfiguration createConfiguration() {
		String name = Messages.getString("SimulationResourceManagerFactory.DefaultRMName"); //$NON-NLS-1$
		String description = Messages.getString("SimulationResourceManagerFactory.DefaultRMDescription"); //$NON-NLS-1$
		SimulationRMConfiguration config = new SimulationRMConfiguration(this, name, description);
		return config;
	}

	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		return SimulationRMConfiguration.load(this, memento);
	}

}
