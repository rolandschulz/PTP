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

public class PJob extends Parent implements IPJob {
	protected String NAME_TAG = "root ";
	protected boolean isDebugJob = false;
	private ArrayList taskIdMap;

	final public static int BASE_OFFSET = 10000;
	
	final public static int STATE_NEW = 5000;

	public PJob(IPUniverse uni, String name, String key, int jobNumber) {
		super(uni, name, key, P_JOB);
		taskIdMap = new ArrayList();
		attribs.put(AttributeConstants.ATTRIB_JOBID, new Integer(jobNumber));
	}
	
	public boolean isDebug() {
		return isDebugJob;
	}
	
	public void setDebug() {
		isDebugJob = true;
	}

	/*
	 * returns the Machines that this job is running on. this is accomplished by
	 * drilling down to the processes, finding the nodes they are running on,
	 * and then seeing which machines those nodes are part of
	 */
	public IPMachine[] getMachines() {
		IPNode[] nodes = getNodes();
		List array = new ArrayList(0);
		for (int i = 0; i < nodes.length; i++) {
			if (!array.contains(nodes[i].getMachine())) {
				array.add(nodes[i].getMachine());
			}
		}

		return (IPMachine[]) array.toArray(new IPMachine[array.size()]);
	}

	public String getJobNumber() {
		return ""+((Integer) attribs.get(AttributeConstants.ATTRIB_JOBID)).intValue()+"";
		/*
		int i = getID();
		System.out.println("get job number - ID = "+i+", offset = "+BASE_OFFSET);
		return "" + (i - BASE_OFFSET) + "";
		*/
		/*
		 * String s = getKey(); int i = -1; try { i = (new
		 * Integer(s)).intValue(); } catch(NumberFormatException e) { } if(i !=
		 * -1) { return ""+(i - BASE_OFFSET)+""; } else return "";
		 */
	}

	public synchronized IPNode[] getSortedNodes() {
		IPNode[] nodes = getNodes();
		sort(nodes);
		return nodes;
	}

	public synchronized IPNode[] getNodes() {
		IPProcess[] processes = getProcesses();
		List array = new ArrayList(0);
		for (int i = 0; i < processes.length; i++) {
			if (!array.contains(processes[i].getNode())) {
				array.add(processes[i].getNode());
			}
		}

		return (IPNode[]) array.toArray(new IPNode[array.size()]);
	}

	/*
	 * returns all the processes in this job, which are the children of the job
	 */
	public synchronized IPProcess[] getProcesses() {
		return (IPProcess[]) getCollection().toArray(new IPProcess[size()]);
	}

	public synchronized IPProcess[] getSortedProcesses() {
		IPProcess[] processes = getProcesses();
		sort(processes);
		return processes;
	}

	public synchronized IPProcess findProcess(String processNumber) {
		IPElement element = findChild(processNumber);
		if (element != null)
			return (IPProcess) element;
		return null;
	}

	public synchronized IPProcess findProcessByName(String pname) {
		Collection col = getCollection();
		Iterator it = col.iterator();
		while (it.hasNext()) {
			Object ob = it.next();
			if (ob instanceof IPProcess) {
				IPProcess proc = (IPProcess) ob;
				if (proc.getElementName().equals(pname))
					return proc;
			}
		}
		return null;
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

	public void removeAllProcesses() {
		IPProcess[] processes = getProcesses();
		for (int i = 0; i < processes.length; i++)
			processes[i].clearOutput();

		removeChildren();
	}

	public IPUniverse getUniverse() {
		IPElement current = this;
		do {
			if (current instanceof IPUniverse)
				return (IPUniverse) current;
		} while ((current = current.getParent()) != null);
		return null;
	}
	
	public void addChild(IPElement member) {
		super.addChild(member);
		if (member instanceof IPProcess) {
			IPProcess p = (IPProcess) member;
			taskIdMap.add(p.getTaskId(), "" + p.getID() + "");
		}
	}

	public synchronized IPProcess findProcessByTaskId(int taskId) {
		String procNumber = (String) taskIdMap.get(taskId);
		if (procNumber == null)
			return null;
		else
			return findProcess(procNumber);
	}
}
