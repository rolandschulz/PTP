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

import org.eclipse.ptp.core.AttributeConstants;
import org.eclipse.ptp.core.IPElement;
import org.eclipse.ptp.core.IPJob;
import org.eclipse.ptp.core.IPMachine;
import org.eclipse.ptp.core.IPNode;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.core.IPUniverse;

public class PMachine extends Parent implements IPMachine {
	protected String NAME_TAG = "machine ";

	protected String arch = "undefined";

	public PMachine(IPUniverse uni, String name, int machineID) {
		super(uni, name, ""+machineID+"", P_MACHINE);
		System.out.println("Name is " + name + ", key is " + machineID);
		System.out.println("NAME_TAG = " + NAME_TAG + ", toString = "
				+ this.toString() + ", key# = " + this.getID());
		attribs.put(AttributeConstants.ATTRIB_MACHINEID, new Integer(machineID));
	}

	public String getMachineNumber() {
		return ""+((Integer) attribs.get(AttributeConstants.ATTRIB_MACHINEID)).intValue()+"";
	}
	
	public int getMachineNumberInt()
	{
		return ((Integer) attribs.get(AttributeConstants.ATTRIB_MACHINEID)).intValue();
	}

	public IPUniverse getUniverse() {
		IPElement current = this;
		do {
			if (current instanceof IPUniverse)
				return (IPUniverse) current;
		} while ((current = current.getParent()) != null);
		return null;
	}

	/* returns an array of the nodes that are comprised by this machine */
	public synchronized IPNode[] getNodes() {
		return (IPNode[]) getCollection().toArray(new IPNode[size()]);
	}

	/* returns a list of the nodes comprised by this machine - but sorted */
	public synchronized IPNode[] getSortedNodes() {
		IPNode[] nodes = getNodes();
		sort(nodes);
		return nodes;
	}

	/* finds a node using a string identifier - returns null if none found */
	public synchronized IPNode findNode(String nodeNumber) {
		IPElement element = findChild(nodeNumber);
		if (element != null)
			return (IPNode) element;
		return null;
	}

	public synchronized IPNode findNodeByName(String nname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPNode) {
				IPNode node = (IPNode) ob;
				if (node.getElementName().equals(nname))
					return node;
			}
		}
		return null;
	}

	/*
	 * helper function to get all the processes running on this machine - doing
	 * so by looking at all the processes on each of the nodes comprised by this
	 * machine
	 */
	public synchronized IPProcess[] getProcesses() {
		List array = new ArrayList(0);
		IPNode[] nodes = getNodes();
		for (int i = 0; i < nodes.length; i++)
			array.addAll(nodes[i].getCollection());

		return (IPProcess[]) array.toArray(new IPProcess[array.size()]);
	}

	/*
	 * returns a sorted list of processes running on this machine (which may
	 * span multiple jobs)
	 */
	public synchronized IPProcess[] getSortedProcesses() {
		IPProcess[] processes = getProcesses();
		sort(processes);
		return processes;
	}

	/*
	 * removes all the processes assocated with this machine. NOTE: this can
	 * remove processes from multiple jobs. the children of this machine, the
	 * nodes, are NOT removed as they need to be present for machine status
	 */
	public void removeAllProcesses() {
		IPProcess[] processes = getProcesses();
		for (int i = 0; i < processes.length; i++)
			processes[i].clearOutput();

		removeChildren();
	}

	/*
	 * returns all the nodes comprised by this machine, which is just the size()
	 * of its children group
	 */
	public int totalNodes() {
		return size();
	}

	/*
	 * counts all the processes running on this machine, which may span multiple
	 * jobs. accomplished by checking all the children processes running on all
	 * the nodes comprised by this machine
	 */
	public int totalProcesses() {
		int counter = 0;
		IPNode[] nodes = getNodes();
		for (int i = 0; i < nodes.length; i++)
			counter += nodes[i].size();

		return counter;
	}

	/*
	 * returns all the jobs that are running on this machine. this is
	 * accomplished by looking at all the processes running on the nodes and
	 * finding the unique set of jobs that those processes belong to
	 */
	public synchronized IPJob[] getJobs() {
		IPProcess[] processes = getProcesses();
		List array = new ArrayList(0);
		for (int i = 0; i < processes.length; i++) {
			if (!array.contains(processes[i].getJob())) {
				array.add(processes[i].getJob());
			}
		}
		return (IPJob[]) array.toArray(new IPJob[array.size()]);
	}

	/* returns a string representation of the architecture of this machine */
	public String getArch() {
		return arch;
	}

	/* sets the architecture of this machine, which is merely a string */
	public void setArch(String arch) {
		this.arch = arch;
	}
}
