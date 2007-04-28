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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPJobControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IPQueueControl;
import org.eclipse.ptp.core.elements.IPMachine;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class PJob extends Parent implements IPJobControl {
	final public static int BASE_OFFSET = 10000;
	final public static int STATE_NEW = 5000;
	private ArrayList<String> taskIdMap = new ArrayList<String>();

	protected String NAME_TAG = "root ";
	
	protected boolean isDebugJob = false;

	public PJob(int id, IPQueueControl queue, IAttribute[] attrs) {
		super(id, queue, P_JOB, attrs);
	}
	
	public void addProcess(IPProcessControl p) {
		addChild(p);
		taskIdMap.add(p.getTaskId(), "" + p.getID() + "");
	}
	
	public synchronized IPProcess findProcess(String processNumber) {
		IPElementControl element = findChild(processNumber);
		if (element != null)
			return (IPProcessControl) element;
		return null;
	}

	public synchronized IPProcess findProcessByName(String pname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPProcessControl) {
				IPProcessControl proc = (IPProcessControl) ob;
				if (proc.getName().equals(pname))
					return proc;
			}
		}
		return null;
	}

	public synchronized IPProcess findProcessByTaskId(int taskId) {
		String procNumber = (String) taskIdMap.get(taskId);
		if (procNumber == null)
			return null;
		else
			return findProcess(procNumber);
	}
	
	/*
	 * returns the Machines that this job is running on. this is accomplished by
	 * drilling down to the processes, finding the nodes they are running on,
	 * and then seeing which machines those nodes are part of
	 */
	public IPMachine[] getMachines() {
		IPNode[] nodes = getNodes();
		ArrayList<IPMachine> array = new ArrayList<IPMachine>(0);
		for (int i = 0; i < nodes.length; i++) {
			final IPMachine machine = nodes[i].getMachine();
			if (machine != null && !array.contains(machine)) {
				array.add(machine);
			}
		}

		return (IPMachine[]) array.toArray(new IPMachine[array.size()]);
	}

	public synchronized IPNode[] getNodes() {
		IPProcess[] processes = getProcesses();
		List array = new ArrayList(0);
		for (int i = 0; i < processes.length; i++) {
			final IPNode node = processes[i].getNode();
			if (node != null && !array.contains(node)) {
				array.add(node);
			}
		}

		return (IPNodeControl[]) array.toArray(new IPNodeControl[array.size()]);
	}

	/*
	 * returns all the processes in this job, which are the children of the job
	 */
	public synchronized IPProcessControl[] getProcessControls() {
		return (IPProcessControl[]) getCollection().toArray(new IPProcessControl[size()]);
	}

	/*
	 * returns all the processes in this job, which are the children of the job
	 */
	public IPProcess[] getProcesses() {
		return getProcessControls();
	}

	public synchronized IPNode[] getSortedNodes() {
		IPNodeControl[] nodes = (IPNodeControl[]) getNodes();
		sort(nodes);
		return nodes;
	}

	public synchronized IPProcess[] getSortedProcesses() {
		IPProcessControl[] processes = (IPProcessControl[]) getProcesses();
		sort(processes);
		return processes;
	}

	public IPUniverse getUniverse() {
		IPElementControl current = this;
		do {
			if (current instanceof IPUniverse)
				return (IPUniverse) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	
	public boolean isDebug() {
		return isDebugJob;
	}

	public void removeAllProcesses() {
		IPProcess[] processes = getProcesses();
		for (int i = 0; i < processes.length; i++)
			processes[i].clearOutput();

		removeChildren();
	}
	
	public void setDebug() {
		isDebugJob = true;
	}
	
	/*
	 * returns the number of nodes that this job is running on by counting each
	 * node that each process in this job is running on
	 */
	public int totalNodes() {
		return getNodes().length;
	}

	public int totalProcesses() {
		return size();
	}

	public IPQueue getQueue() {
		return (IPQueue) getParent();
	}

	public IResourceManager getResourceManager() {
		return getQueue().getResourceManager();
	}
}
