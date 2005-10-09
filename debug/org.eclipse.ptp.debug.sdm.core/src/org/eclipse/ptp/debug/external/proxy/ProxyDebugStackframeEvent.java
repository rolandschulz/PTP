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

package org.eclipse.ptp.debug.external.proxy;

import org.eclipse.ptp.core.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.util.BitList;

public class ProxyDebugStackframeEvent extends AbstractProxyEvent implements IProxyEvent {
	private ProxyDebugStackframe[] frames;
	
	public ProxyDebugStackframeEvent(BitList set, ProxyDebugStackframe[] frames) {
		super(EVENT_DBG_FRAMES, set);
		this.frames = frames;
	}
	
	public ProxyDebugStackframe[] getFrames() {
		return this.frames;
	}
	
	public String toString() {
		String res = "EVENT_DBG_FRAMES " + this.getBitSet().toString();
		for (int i = 0; i < frames.length; i++) {
			res += "\n " + frames[i].toString();	
		}
		return res;
	}
}
