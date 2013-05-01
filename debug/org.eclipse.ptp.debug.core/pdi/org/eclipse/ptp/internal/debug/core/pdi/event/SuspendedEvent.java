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
package org.eclipse.ptp.internal.debug.core.pdi.event;

import org.eclipse.ptp.debug.core.pdi.IPDISessionObject;
import org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent;

/**
 * @author clement
 *
 */
public class SuspendedEvent extends AbstractEvent implements IPDISuspendedEvent {
	private IPDISessionObject reason;
	private String[] vars;
	private int thread_id;
	private int level;
	private int depth;
	
	public SuspendedEvent(IPDISessionObject reason, String[] vars, int thread_id, int level, int depth) {
		super(reason.getSession(), reason.getTasks());
		this.reason = reason;
		this.vars = vars;
		this.thread_id = thread_id;
		this.level = level;
		this.depth = depth;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent#getDepth()
	 */
	public int getDepth() {
		return depth;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent#getLevel()
	 */
	public int getLevel() {
		return level;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent#getReason()
	 */
	public IPDISessionObject getReason() {
		return reason;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent#getThreadID()
	 */
	public int getThreadID() {
		return thread_id;
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.debug.core.pdi.event.IPDISuspendedEvent#getUpdatedVariables()
	 */
	public String[] getUpdatedVariables() {
		return vars;
	}
}
