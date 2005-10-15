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

package org.eclipse.ptp.debug.external.proxy.event;

import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugBreakpointEvent extends AbstractProxyDebugEvent implements IProxyDebugEvent {
	private int				bpId;
	
	public ProxyDebugBreakpointEvent(BitList set, int type, int id) {
		super(type, set);
		this.bpId = id;
	}
	
	public int getBreakpointId() {
		return this.bpId;
	}
	
	public String toString() {
		if (this.getEventID() == EVENT_DBG_BPHIT)
			return "EVENT_DBG_BPHIT " + this.getBitSet().toString();
		else
			return "EVENT_DBG_BPSET " + this.getBitSet().toString();
	}
}
