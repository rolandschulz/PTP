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
package org.eclipse.ptp.rm.core.events;

import java.util.ArrayList;
import java.util.List;
import java.util.ListIterator;

import org.eclipse.ptp.rm.core.IRMJob;
import org.eclipse.ptp.rm.core.IRMMachine;
import org.eclipse.ptp.rm.core.IRMNode;
import org.eclipse.ptp.rm.core.IRMQueue;
import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.attributes.IAttrDesc;

/**
 * A convenience class to enable an {@link {@link IRMResourceManager}} to
 * add/remove listeners and fire events to the list of
 * {@link {@link IRMResourceManagerListener}}s
 * 
 * @author rsqrd
 * 
 */
public class ResourceManagerListenerList {

	private final List listeners = new ArrayList();

	private final IRMResourceManager manager;

	public ResourceManagerListenerList(IRMResourceManager manager) {
		this.manager = manager;
	}

	public synchronized void add(IRMResourceManagerListener listener) {
		listeners.add(listener);
	}

	public synchronized void fireDisposed() {
		RMStructureChangedEvent event = new RMStructureChangedEvent(manager,
				RMResourceManagerEvent.REMOVED);
		List listeners = new ArrayList(this.listeners);
		for (ListIterator lit = listeners.listIterator(); lit.hasNext();) {
			IRMResourceManagerListener rmlistener = (IRMResourceManagerListener) lit
					.next();
			rmlistener.structureChanged(event);
		}
	}

	public synchronized void fireJobsChanged(IRMJob[] jobs,
			IAttrDesc[] modifiedAttributes, boolean statusChanged, int type) {
		RMJobsChangedEvent event = new RMJobsChangedEvent(jobs,
				modifiedAttributes, statusChanged, manager, type);
		List listeners = new ArrayList(this.listeners);
		for (ListIterator lit = listeners.listIterator(); lit.hasNext();) {
			IRMResourceManagerListener rmlistener = (IRMResourceManagerListener) lit
					.next();
			rmlistener.jobsChanged(event);
		}
	}

	public void fireJobsChanged(IRMJob[] jobs, int type) {
		fireJobsChanged(jobs, null, false, type);
	}

	public synchronized void fireMachinesChanged(IRMMachine[] machines,
			IAttrDesc[] modifiedAttributes, boolean statusChanged, int type) {
		RMMachinesChangedEvent event = new RMMachinesChangedEvent(machines,
				modifiedAttributes, statusChanged, manager, type);
		List listeners = new ArrayList(this.listeners);
		for (ListIterator lit = listeners.listIterator(); lit.hasNext();) {
			IRMResourceManagerListener rmlistener = (IRMResourceManagerListener) lit
					.next();
			rmlistener.machinesChanged(event);
		}
	}

	public void fireMachinesChanged(IRMMachine[] machines, int type) {
		fireMachinesChanged(machines, null, false, type);
	}

	public synchronized void fireNodesChanged(IRMNode[] nodes,
			IAttrDesc[] modifiedAttributes, boolean statusChanged, int type) {
		RMNodesChangedEvent event = new RMNodesChangedEvent(nodes,
				modifiedAttributes, statusChanged, manager, type);
		List listeners = new ArrayList(this.listeners);
		for (ListIterator lit = listeners.listIterator(); lit.hasNext();) {
			IRMResourceManagerListener rmlistener = (IRMResourceManagerListener) lit
					.next();
			rmlistener.nodesChanged(event);
		}
	}

	public void fireNodesChanged(IRMNode[] nodes, int type) {
		fireNodesChanged(nodes, null, false, type);
	}

	public synchronized void fireQueuesChanged(IRMQueue[] queues,
			IAttrDesc[] modifiedAttributes, boolean statusChanged, int type) {
		RMQueuesChangedEvent event = new RMQueuesChangedEvent(queues,
				modifiedAttributes, statusChanged, manager, type);
		List listeners = new ArrayList(this.listeners);
		for (ListIterator lit = listeners.listIterator(); lit.hasNext();) {
			IRMResourceManagerListener rmlistener = (IRMResourceManagerListener) lit
					.next();
			rmlistener.queuesChanged(event);
		}
	}

	public void fireQueuesChanged(IRMQueue[] queues, int type) {
		fireQueuesChanged(queues, null, false, type);
	}

	public synchronized void remove(IRMResourceManagerListener listener) {
		listeners.remove(listener);
	}

}
