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

import org.eclipse.ptp.rm.core.IRMQueue;
import org.eclipse.ptp.rm.core.IRMResourceManager;
import org.eclipse.ptp.rm.core.attributes.IAttrDesc;

/**
 * Determine type of changed in the IRMResourceManager's state The type can be
 * ADDED, MODIFIED, or REMOVED
 * 
 * Event created when the ARM has added, modified, or removed queues
 * 
 * @author rsqrd
 * 
 */
public class RMQueuesChangedEvent extends RMResourceManagerEvent {

	private IRMQueue[] queues;

	public RMQueuesChangedEvent(IRMQueue[] queues, IAttrDesc[] modifiedAttributes,
			IRMResourceManager manager, int type) {
		super(modifiedAttributes, manager, type);
		this.queues = (IRMQueue[]) queues.clone();
	}

	/**
	 * @return the list of added, modified, or removed queues
	 */
	public IRMQueue[] getQueues() {
		return (IRMQueue[]) queues.clone();
	}
}
