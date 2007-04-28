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
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.eclipse.ptp.core.attributes.IAttribute;
import org.eclipse.ptp.core.elementcontrols.IPElementControl;
import org.eclipse.ptp.core.elementcontrols.IPMachineControl;
import org.eclipse.ptp.core.elementcontrols.IPNodeControl;
import org.eclipse.ptp.core.elementcontrols.IPProcessControl;
import org.eclipse.ptp.core.elementcontrols.IResourceManagerControl;
import org.eclipse.ptp.core.elements.IPNode;
import org.eclipse.ptp.core.elements.IPProcess;
import org.eclipse.ptp.core.elements.IPUniverse;
import org.eclipse.ptp.rmsystem.IResourceManager;

public class PMachine extends Parent implements IPMachineControl {
	protected String NAME_TAG = "machine ";
	protected String arch = "undefined";

	public PMachine(int id, IResourceManagerControl rm, IAttribute[] attrs) {
		super(id, rm, P_MACHINE, attrs);
	}

	public synchronized void addNode(IPNodeControl curnode) {
		addChild(curnode);
	}

	/* finds a node using a string identifier - returns null if none found */
	public synchronized IPNode findNode(String nodeNumber) {
		IPElementControl element = findChild(nodeNumber);
		if (element != null)
			return (IPNodeControl) element;
		return null;
	}

	public synchronized IPNode findNodeByName(String nname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPNode) {
				IPNode node = (IPNode) ob;
				if (node.getName().equals(nname))
					return node;
			}
		}
		return null;
	}

	/* returns a string representation of the architecture of this machine */
	public String getArch() {
		return arch;
	}

	/* returns an array of the nodes that are comprised by this machine */
	public synchronized IPNode[] getNodes() {
		return (IPNodeControl[]) getCollection().toArray(new IPNodeControl[size()]);
	}

	/*
	 * helper function to get all the processes running on this machine - doing
	 * so by looking at all the processes on each of the nodes comprised by this
	 * machine
	 */
	public synchronized IPProcessControl[] getProcessControls() {
		List<IPProcessControl> array = new ArrayList<IPProcessControl>(0);
		IPNodeControl[] nodes = (IPNodeControl[]) getNodes();
		for (int i = 0; i < nodes.length; i++)
			array.addAll(Arrays.asList(nodes[i].getProcessControls()));

		return (IPProcessControl[]) array.toArray(new IPProcessControl[array.size()]);
	}

	/*
	 * helper function to get all the processes running on this machine - doing
	 * so by looking at all the processes on each of the nodes comprised by this
	 * machine
	 */
	public IPProcess[] getProcesses() {
		return getProcessControls();
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.core.IPMachine#getResourceManager()
	 */
	public IResourceManager getResourceManager() {
		return (IResourceManager) getParent();
	}

	/* returns a list of the nodes comprised by this machine - but sorted */
	public synchronized IPNode[] getSortedNodes() {
		IPNodeControl[] nodes = (IPNodeControl[]) getNodes();
		sort(nodes);
		return nodes;
	}

	/*
	 * returns a sorted list of processes running on this machine (which may
	 * span multiple jobs)
	 */
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

	/* sets the architecture of this machine, which is merely a string */
	public void setArch(String arch) {
		this.arch = arch;
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
			counter += nodes[i].getNumProcesses();

		return counter;
	}
}
