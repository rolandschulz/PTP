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

package org.eclipse.ptp.internal.core.proxy.event;

import org.eclipse.ptp.core.proxy.event.AbstractProxyEventFactory;
import org.eclipse.ptp.core.proxy.event.IProxyEvent;

public class ProxyEventFactory extends AbstractProxyEventFactory {
	public IProxyEvent toEvent(int type, int transID, String[] args) {
		IProxyEvent evt = null;

		switch (type) {
		case IProxyEvent.EVENT_OK:
			evt = new ProxyOKEvent(transID);
			break;
			
		case IProxyEvent.EVENT_MESSAGE:
			evt = new ProxyMessageEvent(transID, args);
			break;

		case IProxyEvent.EVENT_ERROR:
			evt = new ProxyErrorEvent(transID, args);
			break;
		}

		return evt;
	}
}
