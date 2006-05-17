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
package org.eclipse.ptp.rm.sim;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.ptp.rm.core.IRMJob;
import org.eclipse.ptp.rm.core.IRMMachine;
import org.eclipse.ptp.rm.core.IRMNode;
import org.eclipse.ptp.rm.core.IRMQueue;
import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.RMJobStatus;
import org.eclipse.ptp.rm.core.RMResourceManagerHost;
import org.eclipse.ptp.rm.core.RMStatus;
import org.eclipse.ptp.rm.core.attributes.DateAttrDesc;
import org.eclipse.ptp.rm.core.attributes.IAttrDesc;
import org.eclipse.ptp.rm.core.attributes.IAttribute;
import org.eclipse.ptp.rm.core.attributes.IntAttrDesc;
import org.eclipse.ptp.rm.core.attributes.StringAttrDesc;
import org.eclipse.ptp.rm.core.events.IRMResourceManagerListener;
import org.eclipse.ptp.rm.core.events.RMResourceManagerEvent;
import org.eclipse.ptp.rm.core.events.ResourceManagerListenerList;

public class SimResourceManager implements IRMResourceManager {

	private final ResourceManagerListenerList listeners = new ResourceManagerListenerList(
			this);

	private int numExtraNodes = 0;

	private List nodes = null;

	private Thread updater;

	public SimResourceManager(String info) {
		System.out.println("In SimResouceManager(" + info + ")");
		getAllNodes();
		updater = new Thread() {

			public void run() {
				try {
					while (true) {
						incrementNumExtraNodes();
						modifyFirstNode();
						if (numExtraNodes > 10) {
							removeNode();
						}
						if (numExtraNodes % 100 == 1)
							Thread.sleep(5000);
						else
							Thread.sleep(1000);
					}
				} catch (InterruptedException e) {
					System.out.println("updater interupted");
				}
			}
		};
		updater.start();
	}

	public void addResourceManagerListener(IRMResourceManagerListener listener) {
		listeners.add(listener);
	}

	public void dispose() {
		updater.interrupt();
		listeners.fireDisposed();
	}

	public synchronized IRMJob[] getAllJobs() {
		return new IRMJob[] { makeJob(519468, "rsqrd", "intq", "llogin",
				"lambda2", "May  2 09:20:00 2006", RMJobStatus.DONE) };
	}

	public synchronized IRMMachine[] getAllMachines() {
		final IAttrDesc[] descs = getMachineAttrDescs();
		final IAttribute[] attrs = new IAttribute[descs.length];

		final List machines = new ArrayList();
		attrs[0] = descs[0].createAttribute("lambdaserver");
		attrs[1] = descs[1].createAttribute("lsf");
		attrs[2] = descs[2].createAttribute("166");
		attrs[3] = descs[3].createAttribute("166");
		machines.add(new SimMachine(88, "LAMBDA", attrs));
		return (IRMMachine[]) machines.toArray(new SimMachine[0]);
	}

	public synchronized IRMNode[] getAllNodes() {
		if (nodes == null) {
			nodes = new ArrayList();
			final IAttrDesc[] descs = getNodeAttrDescs();
			final IAttribute[] attrs = new IAttribute[descs.length];
			attrs[0] = descs[0].createAttribute("Alpha");
			attrs[1] = descs[1].createAttribute("4");
			attrs[2] = descs[2].createAttribute(new String[] { "mem4", "rms",
					"cs", "qa" });
			nodes.add(new SimNode(42, "qd0", RMStatus.UNAVAILABLE, attrs));
			attrs[0] = descs[0].createAttribute("Beta");
			attrs[1] = descs[1].createAttribute("8");
			attrs[2] = descs[2].createAttribute(new String[] { "mem8", "rms",
					"cs", "qa" });
			nodes.add(new SimNode(420, "qd1", RMStatus.UNAVAILABLE, attrs));
			attrs[0] = descs[0].createAttribute("Gamma");
			attrs[1] = descs[1].createAttribute("16");
			attrs[2] = descs[2].createAttribute(new String[] { "mem16", "rms",
					"cs", "qa" });
			nodes.add(new SimNode(4200, "qd2", RMStatus.UNAVAILABLE, attrs));
		}
		return (IRMNode[]) nodes.toArray(new IRMNode[0]);
	}

	public synchronized IAttrDesc[] getJobAttrDescs() {
		IAttrDesc[] descs = new IAttrDesc[] {
				new IntAttrDesc("JOBID", "The job id"),
				new StringAttrDesc("USER", "The user"),
				new StringAttrDesc("QUEUE", "The queue it came from."),
				new StringAttrDesc("HOST", "The host it came from."),
				new StringAttrDesc("JOB_NAME", "The job name"),
				new DateAttrDesc("SUBMIT_TIME", "The job submit time") };
		return descs;
	}

	public synchronized IAttrDesc[] getMachineAttrDescs() {
		IAttrDesc[] descs = new IAttrDesc[] {
				new StringAttrDesc("MASTER_HOST", "The master host."),
				new StringAttrDesc("ADMIN", "The admin."),
				new IntAttrDesc("NODES", "The number of nodes on this machine."),
				new IntAttrDesc("SERVERS",
						"The number of serveers for this machine.") };
		return descs;
	}

	public synchronized IAttrDesc[] getNodeAttrDescs() {
		IAttrDesc[] descs = new IAttrDesc[3];
		descs[0] = new StringAttrDesc("type", "The type of the machine.");
		descs[1] = new IntAttrDesc("ncpus",
				"The number of cpus on this machine.");
		descs[2] = new StringAttrDesc("RESOURCES",
				"The resources for this machine.");
		return descs;
	}

	public synchronized IAttrDesc[] getQueueAttrDescs() {
		final List descs = new ArrayList();
		descs.add(new IntAttrDesc("PRIO", "Priority"));
		descs
				.add(new StringAttrDesc("State",
						"The detailed Status information"));
		descs.add(new IntAttrDesc("NJOBS", "The number of jobs"));
		descs.add(new IntAttrDesc("PEND", "The number of pending jobs"));
		descs.add(new IntAttrDesc("RUN", "The number of pending jobs"));
		descs.add(new IntAttrDesc("SUSP", "The number of pending jobs"));
		return (IAttrDesc[]) descs.toArray(new IAttrDesc[0]);
	}

	public synchronized IRMQueue[] getQueues() {
		return new IRMQueue[] {
				makeQueue(666, "intq", 25, RMStatus.OK, "Active", 10, 0, 10, 0),
				makeQueue(667, "largeq", 24, RMStatus.DOWN, "Fickle", 133, 133,
						0, 0), };
	}

	public RMResourceManagerHost getResourceManagerHost() {
		// TODO Auto-generated method stub
		return null;
	}

	public void removeResourceManagerListener(
			IRMResourceManagerListener listener) {
		listeners.remove(listener);
	}

	private synchronized void modifyFirstNode() {
		IRMNode node = getAllNodes()[0];
		final IAttrDesc attrDesc = getNodeAttrDescs()[1];
		final int nodeVal = numExtraNodes; 
		System.out.println("modifyFirstNode: " + nodeVal);
		final IAttribute attr = attrDesc.createAttribute(Integer.toString(nodeVal));
		node.setAttribute(attrDesc, attr);
		listeners.fireNodesChanged(new IRMNode[] { node }, new IAttrDesc[]{attrDesc},
				RMResourceManagerEvent.MODIFIED);
	}

	private synchronized void incrementNumExtraNodes() {
		++numExtraNodes;
		System.out.println("incrementNumExtraNodes: " + numExtraNodes);
		final IAttrDesc[] descs = getNodeAttrDescs();
		final IAttribute[] attrs = new IAttribute[descs.length];
		attrs[0] = descs[0].createAttribute("Zippo" + numExtraNodes);
		attrs[1] = descs[1].createAttribute(Integer
				.toString(numExtraNodes * 10));
		attrs[2] = descs[2].createAttribute(new String[] { "mem4", "rms", "cs",
				"qa" });
		final SimNode node = new SimNode(numExtraNodes, "qx" + numExtraNodes,
				RMStatus.ALLOCATED_OTHER, attrs);
		nodes.add(node);
		listeners.fireNodesChanged(new IRMNode[] { node },
				RMResourceManagerEvent.ADDED);
	}

	private IRMJob makeJob(int id, String user, String queue, String node,
			String name, String date, RMJobStatus status) {
		IAttrDesc[] descs = getJobAttrDescs();
		return new SimJob(id, name, status, new IAttribute[] {
				descs[0].createAttribute(Integer.toString(id)),
				descs[1].createAttribute(user),
				descs[2].createAttribute(queue),
				descs[3].createAttribute(node), descs[4].createAttribute(name),
				descs[5].createAttribute(date) });
	}

	private IRMQueue makeQueue(int id, String name, int prio, RMStatus status,
			String state, int njobs, int pend, int run, int susp) {
		IAttrDesc[] descs = getQueueAttrDescs();
		return new SimQueue(id, name, status, new IAttribute[] {
				descs[0].createAttribute(Integer.toString(prio)),
				descs[1].createAttribute(state),
				descs[2].createAttribute(Integer.toString(njobs)),
				descs[3].createAttribute(Integer.toString(pend)),
				descs[4].createAttribute(Integer.toString(run)),
				descs[5].createAttribute(Integer.toString(susp)) });
	}

	private synchronized void removeNode() {
		IRMNode node = (IRMNode) nodes.remove(5);
		listeners.fireNodesChanged(new IRMNode[] { node },
				RMResourceManagerEvent.REMOVED);
	}

}
