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

public class OpenMPIResourceManagerFactory extends AbstractResourceManagerFactory {

	public OpenMPIResourceManagerFactory() {
		// QUESTION: Wouldnt it be better to take name from extension point?
		// Extension point already has a name.
		super("Open MPI");
	}
	
	protected OpenMPIResourceManagerFactory(String name) {
		super(name);
	}

	public IResourceManagerConfiguration copyConfiguration(
			IResourceManagerConfiguration configuration) {
		return (IResourceManagerConfiguration)configuration.clone();
	}

	@Override
	public IResourceManagerControl create(IResourceManagerConfiguration confIn) {
		OpenMPIResourceManagerConfiguration configuration = (OpenMPIResourceManagerConfiguration) confIn;
		PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new OpenMPIResourceManager(universe.getNextResourceManagerId(), universe, configuration);
	}

	public IResourceManagerConfiguration createConfiguration() {
		OpenMPIResourceManagerConfiguration conf = new OpenMPIResourceManagerConfiguration(this);

		return conf;
	}

	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		return OpenMPIResourceManagerConfiguration.load(this, memento);
	}

}
