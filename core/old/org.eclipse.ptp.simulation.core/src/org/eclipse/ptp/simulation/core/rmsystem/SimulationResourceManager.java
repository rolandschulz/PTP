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

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.RuntimeResourceManager;
import org.eclipse.ptp.rtsystem.IRuntimeProxy;
import org.eclipse.ptp.simulation.core.rtsystem.SimulationControlSystem;
import org.eclipse.ptp.simulation.core.rtsystem.SimulationMonitoringSystem;

public class SimulationResourceManager extends RuntimeResourceManager {

	private int numMachines = 0;
	private int[] numNodes = new int[0];

	public SimulationResourceManager(IPUniverseControl universe,
			IResourceManagerConfiguration config) {
		super(universe, config);
	}

	private SimulationRMConfiguration getSimulationRMConfiguration() {
		return (SimulationRMConfiguration) getConfiguration();
	}

	protected void doDispose() {
		// TODO Auto-generated method stub

	}

	protected void doStartRuntime(IProgressMonitor monitor) {
		monitor.beginTask("Starting simulation...", 20);
		try {
			final SimulationRMConfiguration simConfig = getSimulationRMConfiguration();
			numMachines = simConfig.getNumMachines();
			numNodes = (int[]) simConfig.getNumNodesPerMachines().clone();
			/* load up the control and monitoring systems for the simulation */
			setMonitoringSystem(new SimulationMonitoringSystem(numMachines, numNodes));
			monitor.worked(10);
			setControlSystem(new SimulationControlSystem());
			monitor.worked(10);
			setRuntimeProxy((IRuntimeProxy)null);
		}
		finally {
			monitor.done();
		}
	}

	protected void doShutdown() throws CoreException {
		// TODO Auto-generated method stub

	}
}
