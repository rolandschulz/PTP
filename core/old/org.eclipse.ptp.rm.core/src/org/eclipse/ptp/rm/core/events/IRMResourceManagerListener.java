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

import java.util.EventListener;

/**
 * Registration site for Observer pattern to allow objects to be notified of
 * changes in the IRMResourceManager's state
 * 
 * @author rsqrd
 * 
 */
public interface IRMResourceManagerListener extends EventListener {

	/**
	 * The resouce manager has undergone substantial changes to its structure.
	 * This change may need to have tables change their column structures.
	 * 
	 * @param event
	 */
	void structureChanged(RMStructureChangedEvent event);

	/**
	 * Machines have been added, removed, or modified (their states)
	 * 
	 * @param event
	 */
	void machinesChanged(RMMachinesChangedEvent event);

	/**
	 * Nodes have been added, removed, or modified (their states)
	 * 
	 * @param event
	 */
	void nodesChanged(RMNodesChangedEvent event);

	/**
	 * Queues have been added, removed, or modified (their states)
	 * 
	 * @param event
	 */
	void queuesChanged(RMQueuesChangedEvent event);

	/**
	 * Jobs have been added, removed, or modified (their states)
	 * 
	 * @param event
	 */
	void jobsChanged(RMJobsChangedEvent event);
}
