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

package org.eclipse.ptp.proxy.debug.event;

import org.eclipse.ptp.proxy.debug.client.ProxyDebugLocator;

public abstract class AbstractProxyDebugSuspendEvent extends AbstractProxyDebugEvent implements IProxyDebugEvent {
	private int	thread_id = 0;
	private ProxyDebugLocator locator;
	private String[] changed_vars;
	
	public AbstractProxyDebugSuspendEvent(int transID, String set, ProxyDebugLocator loc, int tid, String[] vars) {
		super(EVENT_DBG_SUSPEND, transID, set);
		this.locator = loc;
		this.thread_id = tid;
		this.changed_vars = vars;
	}
	
	public int getThreadId() {
		return this.thread_id;
	}
	
	public ProxyDebugLocator getLocator() {
		return this.locator;
	}
	
	public String[] getChangedVars() {
		return this.changed_vars;
	}
}
