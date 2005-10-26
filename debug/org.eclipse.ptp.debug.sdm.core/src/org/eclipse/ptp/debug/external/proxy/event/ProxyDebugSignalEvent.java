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
import org.eclipse.ptp.debug.external.proxy.ProxyDebugStackframe;

public class ProxyDebugSignalEvent extends AbstractProxyDebugEvent implements IProxyDebugEvent {
	private String				signalName;
	private String				signalMeaning;
	private int					threadID;
	private ProxyDebugStackframe	frame;
	
	public ProxyDebugSignalEvent(BitList set, String name, String meaning, int tid, ProxyDebugStackframe frame) {
		super(EVENT_DBG_SIGNAL, set);
		this.signalName = name;
		this.signalMeaning = meaning;
		this.threadID = tid;
		this.frame = frame;
	}
	
	public String getSignalName() {
		return this.signalName;
	}
	
	public String getSignalMeaning() {
		return this.signalMeaning;
	}
	
	public int getThreadID() {
		return this.threadID;
	}
	
	public ProxyDebugStackframe getFrame() {
		return this.frame;
	}
	
	public String toString() {
		String res = "EVENT_DBG_SIGNAL " + this.getBitSet().toString() + " " + this.signalName;
		if (this.frame != null)
			res += " " + frame.toString();
		return res;
	}
}
