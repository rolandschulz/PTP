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
package org.eclipse.ptp.core.elements.events;

import java.util.Collection;

import org.eclipse.ptp.core.elements.IPQueue;
import org.eclipse.ptp.core.elements.IPResourceManager;

/**
 * This event is generated when one or more new queues are created. It is a bulk event that is sent to child listeners on the source
 * resource manager.
 * 
 * @see org.eclipse.ptp.core.elements.listeners.IResourceManagerChildListener
 */
@Deprecated
public interface INewQueueEvent {

	/**
	 * Get the queues that have been created
	 * 
	 * @return the new queue
	 */
	public Collection<IPQueue> getQueues();

	/**
	 * Get the source of this event
	 * 
	 * @return the source of the event
	 * @since 5.0
	 */
	public IPResourceManager getSource();
}
