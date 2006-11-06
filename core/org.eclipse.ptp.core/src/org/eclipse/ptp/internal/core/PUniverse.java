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
import java.util.Iterator;

import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPQueue;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elementcontrols.IPUniverseControl;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class PUniverse extends Parent implements IPUniverseControl {
	protected String NAME_TAG = "universe ";
	public PUniverse() {
		/* '1' because this is the only universe */
		super(null, "TheUniverse", "" + 1 + "", P_UNIVERSE);
		// setOutputStore();
	}

	public synchronized void addResourceManager(IResourceManager addedManager) {
		addChild(addedManager);
	}

	public synchronized void addResourceManagers(IResourceManager[] addedManagers) {
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
		removeChild(job);
	}

	public synchronized IPJob findJobById(String job_id) {
		for (Iterator rit = getCollection().iterator(); rit.hasNext(); ) {
			IResourceManager resourceManager = (IResourceManager) rit.next();
			IPQueueControl[] queueus = resourceManager.getQueueControls();
			for (int j = 0; j < queueus.length; ++j) {
				IPJobControl job = queueus[j].getJobControl(job_id);
				if (job != null) {
					return job;
				}
			}
		}
		return null;
	}
	
	public synchronized IPJob findJobByName(String jname) {
		for (Iterator rit = getCollection().iterator(); rit.hasNext(); ) {
			IResourceManager resourceManager = (IResourceManager) rit.next();
			IPQueueControl[] queueus = resourceManager.getQueueControls();
			for (int i = 0; i < queueus.length; ++i) {
				IPJobControl[] jobs = queueus[i].getJobControls();
				for (int j = 0; j < jobs.length; ++j) {
					if (jobs[i].getElementName().equals(jname))
						return jobs[i];
				}
			}
		}
		return null;
	}
	

	public synchronized IPMachine findMachineByGlobalId(String machine_id) {
		for (Iterator rit = getCollection().iterator(); rit.hasNext(); ) {
			IResourceManager resourceManager = (IResourceManager) rit.next();
			IPMachineControl[] machines = resourceManager.getMachineControls();
			for (int i = 0; i < machines.length; ++i) {
				if (machines[i].getIDString().equals(machine_id))
					return machines[i];
			}
		}
		return null;
	}

	public synchronized IPMachine findMachineById(String machine_id) {
		for (Iterator rit = getCollection().iterator(); rit.hasNext(); ) {
			IResourceManager resourceManager = (IResourceManager) rit.next();
			IPMachineControl[] machines = resourceManager.getMachineControls();
			for (int i = 0; i < machines.length; ++i) {
				if (machines[i].getMachineId().equals(machine_id))
					return machines[i];
			}
		}
		return null;
	}

	public synchronized IPMachine findMachineByName(String mname) {
		for (Iterator rit = getCollection().iterator(); rit.hasNext(); ) {
			IResourceManager resourceManager = (IResourceManager) rit.next();
			IPMachine[] machines = resourceManager.getMachines();
			for (int i = 0; i < machines.length; ++i) {
				if (machines[i].getName().equals(mname))
					return machines[i];
			}
		}
		return null;
	}
	
	public synchronized IPProcess findProcessByName(String pname) {
		IPJob[] jobs = getJobs();
		for (int i=0; i<jobs.length; ++i) {
			IPProcess proc = jobs[i].findProcessByName(pname);
			if (proc != null)
				return proc;
		}
		return null;
	}

	/*
	 */
	public synchronized IPJob[] getJobs() {
		ArrayList jobs = new ArrayList();
		for (Iterator rit = getCollection().iterator(); rit.hasNext(); ) {
			IResourceManager resourceManager = (IResourceManager) rit.next();
			IPQueue[] queueus = resourceManager.getQueues();
			for (int j = 0; j < queueus.length; ++j) {
				IPJob[] qjobs = queueus[j].getJobs();
				jobs.addAll(Arrays.asList(qjobs));
			}
		}
		return (IPJob[]) jobs.toArray(new IPJobControl[0]);
	}
	
	public synchronized IPMachine[] getMachines() {
		ArrayList machines = new ArrayList();
		for (Iterator rit = getCollection().iterator(); rit.hasNext(); ) {
			IResourceManager resourceManager = (IResourceManager) rit.next();
			IPMachineControl[] rmMachines = resourceManager.getMachineControls();
			machines.addAll(Arrays.asList(rmMachines));
		}
		return (IPMachine[]) machines.toArray(new IPMachineControl[0]);
	}

	/**
	 * @return all of the resource managers
	 */
	public synchronized IResourceManager[] getResourceManagers() {
		return (IResourceManager[]) getCollection().toArray(new IResourceManager[0]);
	}
	
	public synchronized IPJob[] getSortedJobs() {
		IPJobControl[] jobs = (IPJobControl[]) getJobs();
		sort(jobs);
		return jobs;
	}
	
	public synchronized IPMachine[] getSortedMachines() {
		IPMachineControl[] macs = (IPMachineControl[]) getMachines();
		sort(macs);

		return macs;
	}

	public synchronized void removeResourceManager(IResourceManager removedManager) {
		removeChild(removedManager);
	}

	public void removeResourceManagers(IResourceManager[] removedRMs) {
		for (int i=0; i<removedRMs.length; ++i) {
			removeResourceManager(removedRMs[i]);
		}
	}
}