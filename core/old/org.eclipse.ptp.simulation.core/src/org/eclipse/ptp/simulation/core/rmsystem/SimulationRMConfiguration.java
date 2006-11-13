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
/**
 * 
 */
package org.eclipse.ptp.simulation.core.rmsystem;

import java.util.Arrays;

import org.eclipse.ptp.rmsystem.IResourceManagerConfiguration;
import org.eclipse.ptp.rmsystem.Messages;
import org.eclipse.ui.IMemento;

public class SimulationRMConfiguration implements IResourceManagerConfiguration {

	private static final String TAG_NUM_NODES = "numNodes"; //$NON-NLS-1$
	private static final String TAG_MACHINE = "machine"; //$NON-NLS-1$
	private static final String TAG_NUM_MACHINES = "numMachines"; //$NON-NLS-1$
	private static final String TAG_DESCRIPTION = "description"; //$NON-NLS-1$
	private static final String TAG_NAME = "name"; //$NON-NLS-1$
	private static final String TAG_FACTORY_ID = "factoryId"; //$NON-NLS-1$

	public static SimulationRMConfiguration load(SimulationResourceManagerFactory factory, IMemento memento) {
		String factoryId = memento.getString(TAG_FACTORY_ID);
		if (!factoryId.equals(factory.getId())) {
			throw new IllegalStateException(Messages.getString("SimulationRMConfiguration.IncompatableFactoryID") //$NON-NLS-1$
					+ Messages.getString("SimulationRMConfiguration.StoredId") + factoryId //$NON-NLS-1$
					+ Messages.getString("SimulationRMConfiguration.ExpectedFactoryId") + factory.getId()); //$NON-NLS-1$
		}
		String name = memento.getString(TAG_NAME);
		String desc = memento.getString(TAG_DESCRIPTION);
		final Integer integerNumMachines = memento.getInteger(TAG_NUM_MACHINES);
		int numMachines = integerNumMachines == null ? 0 : integerNumMachines.intValue();
		int[] numNodesPerMachine = new int[numMachines];
		IMemento[] machineChildren = memento.getChildren(TAG_MACHINE);
		if (machineChildren.length != numMachines) {
			throw new RuntimeException(Messages.getString("SimulationRMConfiguration.IncorrectNumMachines")); //$NON-NLS-1$
		}
		for (int i=0; i<numMachines; ++i) {
			numNodesPerMachine[i] = machineChildren[i].getInteger(TAG_NUM_NODES).intValue();
		}
		SimulationRMConfiguration config = new SimulationRMConfiguration(factory, name, desc, numMachines, numNodesPerMachine);
		return config;
	}
	private String description;
	private String name;
	private final String factoryId;

	private int numMachines = -1;
	private int[] numNodesPerMachines = null;

	public SimulationRMConfiguration(SimulationResourceManagerFactory factory, String name, String desc) {
		this.factoryId = factory.getId();
		this.name = name;
		this.description = desc;
	}
	
	public SimulationRMConfiguration(SimulationResourceManagerFactory factory, String name, String desc,
			int numMachines, int[] numNodesPerMachine) {
		this(factory, name, desc);
		this.numMachines = numMachines;
		this.numNodesPerMachines = numNodesPerMachine;
		if (numNodesPerMachine.length != numMachines) {
			throw new IllegalArgumentException(Messages.getString("SimulationRMConfiguration.NumNodesPerMachineLength")); //$NON-NLS-1$
		}
	}

	public String getDescription() {
		return this.description;
	}

	public String getName() {
		return this.name;
	}

	public int getNumMachines() {
		return numMachines;
	}

	public int getNumNodesPerMachine(int i) {
		return numNodesPerMachines[i];
	}

	public int[] getNumNodesPerMachines() {
		return (int[]) numNodesPerMachines.clone();
	}

	public String getResourceManagerId() {
		return factoryId;
	}
	
	public void save(IMemento memento) {
		memento.putString(TAG_FACTORY_ID, getResourceManagerId());
		memento.putString(TAG_NAME, name);
		memento.putString(TAG_DESCRIPTION, description);
		memento.putInteger(TAG_NUM_MACHINES, numMachines);
		for (int i=0; i < numMachines; ++i) {
			IMemento child = memento.createChild(TAG_MACHINE);
			child.putInteger(TAG_NUM_NODES, numNodesPerMachines[i]);
		}
	}

	public void setNumMachines(int nm) {
		this.numMachines = nm;
		this.numNodesPerMachines = new int[nm >= 0 ? nm : 0];
		Arrays.fill(this.numNodesPerMachines, 0);
	}

	public void setNumNodesPerMachine(int whichMachine, int value) {
		numNodesPerMachines[whichMachine] = value;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDefaultNameAndDesc() {
		String name = Messages.getString("SimulationResourceManagerFactory.DefaultRMName"); //$NON-NLS-1$
		String description = Messages.getString("SimulationResourceManagerFactory.DefaultRMDescription"); //$NON-NLS-1$
		String machinesLabel = Messages.getString("SimulationRMConfiguration.Machines"); //$NON-NLS-1$
		machinesLabel = " (" + Integer.toString(numMachines) + machinesLabel + ")"; //$NON-NLS-1$ //$NON-NLS-2$
		this.name = name + machinesLabel;
		this.description = description + machinesLabel;
	}

}
