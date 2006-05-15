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
package org.eclipse.ptp.internal.rm.ui.views;

import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.events.IRMResourceManagerListener;
import org.eclipse.ptp.rm.core.events.RMJobsChangedEvent;
import org.eclipse.ptp.rm.core.events.RMMachinesChangedEvent;
import org.eclipse.ptp.rm.core.events.RMNodesChangedEvent;
import org.eclipse.ptp.rm.core.events.RMQueuesChangedEvent;
import org.eclipse.ptp.rm.core.events.RMStructureChangedEvent;

public abstract class AbstractResourceManagerListener implements
		IRMResourceManagerListener {

	protected IRMResourceManager manager;

	public void jobsChanged(RMJobsChangedEvent event) {
		// no-op
	}

	public void machinesChanged(RMMachinesChangedEvent event) {
		// no-op
	}

	public void nodesChanged(RMNodesChangedEvent event) {
		// no-op
	}

	public void queuesChanged(RMQueuesChangedEvent event) {
		// no-op
	}

	public void structureChanged(RMStructureChangedEvent event) {
		// no-op
	}
	
	public void setManager(IRMResourceManager manager) {
		if (this.manager != null) {
			this.manager.removeResourceManagerListener(this);
		}
		this.manager = manager;
		this.manager.addResourceManagerListener(this);
	}
}
