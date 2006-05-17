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

import org.eclipse.ptp.rm.core.IRMNode;
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

	public synchronized void fireNodesChanged(IRMNode[] nodes,
			IAttrDesc[] modifiedAttributes, int type) {
		RMNodesChangedEvent event = new RMNodesChangedEvent(nodes,
				modifiedAttributes, manager, type);
		List listeners = new ArrayList(this.listeners);
		for (ListIterator lit = listeners.listIterator(); lit.hasNext();) {
			IRMResourceManagerListener rmlistener = (IRMResourceManagerListener) lit
					.next();
			rmlistener.nodesChanged(event);
		}
	}

	public void fireNodesChanged(IRMNode[] nodes, int type) {
		fireNodesChanged(nodes, null, type);
	}

	public synchronized void remove(IRMResourceManagerListener listener) {
		listeners.remove(listener);
	}

}
