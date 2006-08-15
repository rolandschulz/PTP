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

import java.util.Collection;
import java.util.Iterator;
import java.util.Vector;

import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.internal.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.internal.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.internal.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.internal.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.internal.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.internal.core.elementcontrols.IPUniverseControl;

public class PUniverse extends Parent implements IPUniverseControl {
	protected String NAME_TAG = "universe ";

	public PUniverse() {
		/* '1' because this is the only universe */
		super(null, "TheUniverse", "" + 1 + "", P_UNIVERSE);
		// setOutputStore();
	}

	/*
	 * public String getOutputStoreDirectory() { return outputDirPath; } public
	 * int getStoreLine() { return storeLine; }
	 */

	/*
	 * there is a single collection but in this collection we keep two different
	 * kinds of classes - they are the machines and the jobs. So we have to go
	 * through the entire collection pulling out the right class and return an
	 * array of them
	 */
	public synchronized IPMachine[] getMachines() {
		Collection col = getCollection();
		Iterator it = col.iterator();
		Vector m = new Vector();

		while (it.hasNext()) {
			Object ob = it.next();

			if (ob instanceof IPMachineControl)
				m.add((IPMachineControl) ob);
		}


		IPMachineControl[] mac = (IPMachineControl[]) m.toArray(new IPMachineControl[0]);
		return mac;
	}

	public synchronized IPMachine[] getSortedMachines() {
		IPMachineControl[] macs = (IPMachineControl[]) getMachines();
		sort(macs);

		return macs;
	}

	public synchronized IPMachine findMachineByName(String mname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPMachine) {
				IPMachine mac = (IPMachine) ob;
				if (mac.getName().equals(mname))
					return mac;
			}
		}
		return null;
	}

	public synchronized IPMachine findMachineById(String machine_id) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPMachine) {
				IPMachine mac = (IPMachine) ob;
				if (mac.getMachineId().equals(machine_id))
					return mac;
			}
		}
		return null;
	}
	
	public synchronized IPMachine findMachineByGlobalId(String machine_id) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPMachine) {
				IPMachine mac = (IPMachine) ob;
				if (mac.getIDString().equals(machine_id))
					return mac;
			}
		}
		return null;
	}
	

	/*
	 * there is a single collection but in this collection we keep two different
	 * kinds of classes - they are the machines and the jobs. So we have to go
	 * through the entire collection pulling out the right class and return an
	 * array of them
	 */
	public synchronized IPJob[] getJobs() {
		Collection col = getCollection();
		Iterator it = col.iterator();
		Vector m = new Vector();

		while (it.hasNext()) {
			Object ob = it.next();

			if (ob instanceof IPJobControl)
				m.add((IPJobControl) ob);
		}

		Object[] o = m.toArray();
		IPJobControl[] job = new IPJobControl[o.length];
		for (int i = 0; i < o.length; i++) {
			job[i] = (IPJobControl) o[i];
		}

		return job;
	}

	public synchronized IPJob[] getSortedJobs() {
		IPJobControl[] jobs = (IPJobControl[]) getJobs();
		sort(jobs);
		return jobs;
	}

	public synchronized IPJob findJobByName(String jname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPJobControl) {
				IPJobControl job = (IPJobControl) ob;
				if (job.getElementName().equals(jname))
					return job;
			}
		}
		return null;
	}
	
	public synchronized IPJob findJobById(String job_id) {
		IPElementControl element = findChild(job_id);
		if (element instanceof IPJobControl) {
			return (IPJobControl) element;
		}
		return null;
	}

	public synchronized IPProcess findProcessByName(String pname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPJob) {
				IPProcess proc = ((IPJob) ob).findProcessByName(pname);
				if (proc != null)
					return proc;
			}
		}
		return null;
	}
	
	public void deleteJob(IPJob jobIn) {
		IPJobControl job = (IPJobControl) jobIn;
		for (Iterator i=job.getCollection().iterator(); i.hasNext();) {
			IPProcessControl process = (IPProcessControl)i.next();
			if (process == null)
				continue;
			IPNodeControl node = (IPNodeControl) process.getNode();
			if (node == null)
				continue;
			node.removeChild(process);
		}
		job.removeAllProcesses();
		removeChild(job);
	}
}