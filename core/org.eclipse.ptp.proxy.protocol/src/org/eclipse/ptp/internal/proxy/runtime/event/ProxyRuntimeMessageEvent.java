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

package org.eclipse.ptp.internal.proxy.runtime.event;

import org.eclipse.ptp.proxy.event.AbstractProxyEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeMessageEvent;

public class ProxyRuntimeMessageEvent extends AbstractProxyEvent 
		implements IProxyRuntimeMessageEvent {

	public ProxyRuntimeMessageEvent(int transID, String[] args) {
		super(MESSAGE, transID, args);
	}
	
	public ProxyRuntimeMessageEvent(Level level, String message) {
		this(level, 0, message);
	}

	public ProxyRuntimeMessageEvent(Level level, int code, String message) {
		super(MESSAGE, 0, new String[] {
			IProxyMessageEvent.LEVEL_ATTR + "=" + level.name(),
			IProxyMessageEvent.CODE_ATTR + "=" + code,
			IProxyMessageEvent.TEXT_ATTR + "=" + message
		});
	}
	
	public ProxyRuntimeMessageEvent(IProxyMessageEvent event) {
		this(event.getTransactionID(), event.getAttributes());
	}
}
