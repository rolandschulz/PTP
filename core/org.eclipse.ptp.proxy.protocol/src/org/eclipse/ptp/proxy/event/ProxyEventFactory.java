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

package org.eclipse.ptp.proxy.event;

import org.eclipse.ptp.internal.proxy.event.ProxyErrorEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyMessageEvent;
import org.eclipse.ptp.internal.proxy.event.ProxyOKEvent;
import org.eclipse.ptp.proxy.packet.ProxyPacket;

public class ProxyEventFactory extends AbstractProxyEventFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.event.AbstractProxyEventFactory#toEvent(org.eclipse.ptp.proxy.packet.ProxyPacket)
	 */
	public IProxyEvent toEvent(ProxyPacket packet) {
		IProxyEvent evt = null;

		switch (packet.getID()) {
		case IProxyEvent.OK:
			evt = new ProxyOKEvent(packet.getTransID());
			break;
			
		case IProxyEvent.MESSAGE:
			evt = new ProxyMessageEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyEvent.ERROR:
			evt = new ProxyErrorEvent(packet.getTransID(), packet.getArgs());
			break;
		}

		return evt;
	}
	
	public static IProxyOKEvent newOKEvent(int transID) {
		return new ProxyOKEvent(transID);
	}
	
	public static IProxyErrorEvent newErrorEvent(int transID, int code, String message) {
		String[] args = new String[] {
				IProxyErrorEvent.ERROR_CODE_ATTR + "=" + code,  //$NON-NLS-1$
				IProxyErrorEvent.ERROR_MESSAGE_ATTR + "=" + message //$NON-NLS-1$
		};
		
		return new ProxyErrorEvent(transID, args);
	}
}
