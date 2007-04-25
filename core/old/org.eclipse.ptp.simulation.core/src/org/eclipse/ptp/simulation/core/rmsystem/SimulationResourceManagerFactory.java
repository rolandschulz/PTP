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
package org.eclipse.ptp.simulation.core.rmsystem;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.Messages;
import org.eclipse.ui.IMemento;

public class SimulationResourceManagerFactory extends
		AbstractResourceManagerFactory {
	
	public SimulationResourceManagerFactory() {
		super(Messages.getString("SimulationResourceManagerFactory.ResourceManagerFactoryName")); //$NON-NLS-1$
	}

	public IResourceManagerControl create(IResourceManagerConfiguration confIn) {
		SimulationRMConfiguration configuration = (SimulationRMConfiguration) confIn;
		final PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		plugin.savePluginPreferences();
		final IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new SimulationResourceManager(universe.getNextResourceManagerId(), universe, configuration);
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
