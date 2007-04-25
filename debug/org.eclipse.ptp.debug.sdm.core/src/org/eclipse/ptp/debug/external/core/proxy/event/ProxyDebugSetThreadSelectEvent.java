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

package org.eclipse.ptp.debug.external.core.proxy.event;

import org.eclipse.ptp.core.util.BitList;
import org.eclipse.ptp.debug.external.core.proxy.ProxyDebugStackframe;

public class ProxyDebugSetThreadSelectEvent extends AbstractProxyDebugEvent implements IProxyDebugSetThreadSelectEvent {
	private int thread_id = -1;
	private ProxyDebugStackframe frame;
	
	public ProxyDebugSetThreadSelectEvent(int transID, BitList set, int thread_id, ProxyDebugStackframe frame) {
		super(transID, EVENT_DBG_THREAD_SELECT, set);
		this.thread_id = thread_id;
		this.frame = frame;
	}
	
	public ProxyDebugStackframe getFrame() {
		return this.frame;
	}
	public int getThreadId() {
		return this.thread_id;
	}
	
	public String toString() {
		String res = "EVENT_DBG_THREAD_SELECT transid=" + getTransactionID() + " " + this.getBitSet().toString();
		res += "\n id: " + frame.toString();
		return res;
	}
}
