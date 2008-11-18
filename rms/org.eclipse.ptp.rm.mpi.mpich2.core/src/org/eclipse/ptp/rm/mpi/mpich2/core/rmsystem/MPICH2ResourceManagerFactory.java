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
package org.eclipse.ptp.rm.mpi.mpich2.core.rmsystem;

import org.eclipse.ptp.core.PTPCorePlugin;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.rm.mpi.mpich2.core.messages.Messages;
import org.eclipse.ptp.rmsystem.AbstractResourceManagerFactory;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ui.IMemento;

/**
 * 
 * @author Daniel Felix Ferber
 *
 */
public class MPICH2ResourceManagerFactory extends AbstractResourceManagerFactory {

	public MPICH2ResourceManagerFactory() {
		// QUESTION: Wouldnt it be better to take name from extension point?
		// Extension point already has a name.
		super(Messages.MPICH2ResourceManagerFactory_ResourceManagerName);
	}

	protected MPICH2ResourceManagerFactory(String name) {
		super(name);
	}

	public IResourceManagerConfiguration copyConfiguration(
			IResourceManagerConfiguration configuration) {
		return (IResourceManagerConfiguration)configuration.clone();
	}

	@Override
	public IResourceManagerControl create(IResourceManagerConfiguration confIn) {
		MPICH2ResourceManagerConfiguration configuration = (MPICH2ResourceManagerConfiguration) confIn;
		PTPCorePlugin plugin = PTPCorePlugin.getDefault();
		IPUniverseControl universe = (IPUniverseControl) plugin.getUniverse();
		return new MPICH2ResourceManager(universe.getNextResourceManagerId(), universe, configuration);
	}

	public IResourceManagerConfiguration createConfiguration() {
		MPICH2ResourceManagerConfiguration conf = new MPICH2ResourceManagerConfiguration(this);

		return conf;
	}

	public IResourceManagerConfiguration loadConfiguration(IMemento memento) {
		return MPICH2ResourceManagerConfiguration.load(this, memento);
	}

}
