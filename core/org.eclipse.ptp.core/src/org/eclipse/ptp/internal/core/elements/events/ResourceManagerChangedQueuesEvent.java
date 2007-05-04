/*******************************************************************************
 * Copyright (c) 2005, 2006, 2007 Los Alamos National Security, LLC.
 * This material was produced under U.S. Government contract DE-AC52-06NA25396
 * for Los Alamos National Laboratory (LANL), which is operated by the Los Alamos
 * National Security, LLC (LANS) for the U.S. Department of Energy.  The U.S. Government has
 * rights to use, reproduce, and distribute this software. NEITHER THE
 * GOVERNMENT NOR LANS MAKES ANY WARRANTY, EXPRESS OR IMPLIED, OR
 * ASSUMES ANY LIABILITY FOR THE USE OF THIS SOFTWARE. If software is modified
 * to produce derivative works, such modified software should be clearly marked,
 * so as not to confuse it with the version available from LANL.
 *
 * Additionally, this program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *******************************************************************************/
/**
 * 
 */
package org.eclipse.ptp.internal.core.elements.events;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IResourceManager;
import org.eclipse.ptp.core.elements.events.IResourceManagerChangedQueueEvent;

/**
 * @author rsqrd
 *
 */
public class ResourceManagerChangedQueuesEvent implements
		IResourceManagerChangedQueueEvent {

	private final List<IPQueue> changedQueues;
	private final IResourceManager rm;

	public ResourceManagerChangedQueuesEvent(IResourceManager manager, List<IPQueue> queues) {
		this.rm = manager;
		this.changedQueues = new ArrayList<IPQueue>(queues);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.events.IResourceManagerChangedQueuesEvent#getChangedQueues()
	 */
	public IPQueue getQueue() {
		return Collections.unmodifiableList(changedQueues);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.rmsystem.events.IResourceManagerChangedQueuesEvent#getSource()
	 */
	public IResourceManager getSource() {
		return rm;
	}

}
