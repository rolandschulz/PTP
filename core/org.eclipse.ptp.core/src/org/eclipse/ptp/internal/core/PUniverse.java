/*******************************************************************************
 * Copyright (c) 2005 The Regents of the University of California. 
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
package org.eclipse.ptp.internal.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.ptp.core.attributes.AttributeDefinitionManager;
import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.attributes.IllegalValueException;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPJob;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class PUniverse extends PElement implements IPUniverseControl {
	private static final int RMID_SHIFT = 24;
	private int nextResourceManagerId = 1;
	private final List<IResourceManagerControl> resourceManagers =
		new LinkedList<IResourceManagerControl>();
	protected String NAME_TAG = "universe ";
	
	public PUniverse() {
		/* '1' because this is the only universe */
		super(1, null, P_UNIVERSE, getDefaultAttributes("TheUniverse"));
		// setOutputStore();
	}
	
	private static IAttribute[] getDefaultAttributes(String name) {
		IAttribute nameAttr = null;
		
		try {
			 nameAttr = AttributeDefinitionManager.getNameAttributeDefinition().create(name);
		} catch (IllegalValueException e) {
		}
		
		return new IAttribute[]{nameAttr};
	}

	public synchronized void addResourceManager(IResourceManagerControl addedManager) {
		resourceManagers.add(addedManager);
	}

	public synchronized void addResourceManagers(IResourceManagerControl[] addedManagers) {
		for (int i=0; i<addedManagers.length; ++i) {
			addResourceManager(addedManagers[i]);
		}
	}

	public void deleteJob(IPJob jobIn) {
		IPJobControl job = (IPJobControl) jobIn;
		IPProcessControl[] processes = job.getProcessControls();
		for (int i = 0; i < processes.length; ++i) {
			IPProcessControl process = processes[i];
			if (process == null)
				continue;
			IPNodeControl node = (IPNodeControl) process.getNode();
			if (node == null)
				continue;
			node.removeProcess(process);
		}
		job.removeAllProcesses();
		IResourceManager resourceManager = job.getResourceManager();
		resourceManager.removeJob(job);
	}

	public synchronized IPJob findJobById(String job_id) {
		for (IResourceManager resourceManager : resourceManagers) {
			IPJob job = resourceManager.findJobById(job_id);
			if (job != null) {
				return job;
			}
		}
		return null;
	}

	public synchronized IPMachine findMachineByGlobalId(String machine_id) {
		for (IResourceManagerControl resourceManager : resourceManagers) {
			IPMachineControl[] machines = resourceManager.getMachineControls();
			for (int i = 0; i < machines.length; ++i) {
				if (machines[i].getIDString().equals(machine_id))
					return machines[i];
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPUniverse#findQueueById(java.lang.String)
	 */
	public IPQueue findQueueById(String id) {
		for (IResourceManager resourceManager : resourceManagers) {
			IPQueue[] queues = resourceManager.getQueues();
			for (int i = 0; i < queues.length; ++i) {
				if (queues[i].getIDString().equals(id))
					return queues[i];
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPUniverse#findResourceManagerById(java.lang.String)
	 */
	public synchronized IResourceManager findResourceManagerById(String id) {
		for (IResourceManager resourceManager : resourceManagers) {
			if (resourceManager.getIDString().equals(id)) {
				return resourceManager;
			}
		}
		return null;
	}
	
	/*
	 */
	public synchronized IPJob[] getJobs() {
		ArrayList<IPJob> jobs = new ArrayList<IPJob>();
		for (IResourceManager resourceManager : resourceManagers) {
			IPJob[] rmjobs = resourceManager.getJobs();
			jobs.addAll(Arrays.asList(rmjobs));
		}
		return (IPJob[]) jobs.toArray(new IPJobControl[0]);
	}

	public synchronized IPMachine[] getMachines() {
		ArrayList<IPMachine> machines = new ArrayList<IPMachine>();
		for (IResourceManager resourceManager : resourceManagers) {
			IPMachine[] rmMachines = resourceManager.getMachines();
			machines.addAll(Arrays.asList(rmMachines));
		}
		return (IPMachine[]) machines.toArray(new IPMachineControl[0]);
	}
	
	public int getNextResourceManagerId() {
		return (nextResourceManagerId++ << RMID_SHIFT);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPUniverse#getQueues()
	 */
	public IPQueue[] getQueues() {
		ArrayList<IPQueue> queues = new ArrayList<IPQueue>();
		for (IResourceManager resourceManager : resourceManagers) {
			IPQueue[] rmQueues = resourceManager.getQueues();
			queues.addAll(Arrays.asList(rmQueues));
		}
		return (IPQueue[]) queues.toArray(new IPQueue[0]);
	}

	/**
	 * @return all of the resource managers
	 */
	public synchronized IResourceManagerControl[] getResourceManagerControls() {
		return resourceManagers.toArray(new IResourceManagerControl[0]);
	}

	/**
	 * @return all of the resource managers
	 */
	public synchronized IResourceManager[] getResourceManagers() {
		return (IResourceManager[]) getResourceManagerControls();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#hasChildren()
	 */
	public boolean hasChildren() {
		return !resourceManagers.isEmpty();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.elementcontrols.IPElementControl#isAllStop()
	 */
	public boolean isAllStop() {
		for (IResourceManagerControl resourceManager : resourceManagers) {
			if (!resourceManager.isAllStop())
				return false;
		}
		return true;
	}

	public synchronized void removeResourceManager(IResourceManager removedManager) {
		resourceManagers.remove(removedManager);
	}

	public void removeResourceManagers(IResourceManager[] removedRMs) {
		for (int i=0; i<removedRMs.length; ++i) {
			removeResourceManager(removedRMs[i]);
		}
	}
}