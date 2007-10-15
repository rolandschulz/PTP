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

package org.eclipse.ptp.internal.proxy.runtime.event;

import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.ProxyEventFactory;
import org.eclipse.ptp.proxy.packet.ProxyPacket;
import org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEvent;

public class ProxyRuntimeEventFactory extends ProxyEventFactory {
	public IProxyEvent toEvent(ProxyPacket packet) {
		IProxyRuntimeEvent	evt = null;

		IProxyEvent e = super.toEvent(packet);
		if (e != null) {
			return e;
		}
		
		switch (packet.getID()) {
		case IProxyRuntimeEvent.ATTR_DEF:
			evt = new ProxyRuntimeAttributeDefEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.NEW_JOB:
			evt = new ProxyRuntimeNewJobEvent(packet.getTransID(), packet.getArgs());
			break;
		
		case IProxyRuntimeEvent.NEW_MACHINE:
			evt = new ProxyRuntimeNewMachineEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.NEW_NODE:
			evt = new ProxyRuntimeNewNodeEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.NEW_PROCESS:
			evt = new ProxyRuntimeNewProcessEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.NEW_QUEUE:
			evt = new ProxyRuntimeNewQueueEvent(packet.getTransID(), packet.getArgs());
			break;
			
		case IProxyRuntimeEvent.JOB_CHANGE:
			evt = new ProxyRuntimeJobChangeEvent(packet.getTransID(), packet.getArgs());
			break;
		
		case IProxyRuntimeEvent.MACHINE_CHANGE:
			evt = new ProxyRuntimeMachineChangeEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.NODE_CHANGE:
			evt = new ProxyRuntimeNodeChangeEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.PROCESS_CHANGE:
			evt = new ProxyRuntimeProcessChangeEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.QUEUE_CHANGE:
			evt = new ProxyRuntimeQueueChangeEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.REMOVE_ALL:
			evt = new ProxyRuntimeRemoveAllEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.REMOVE_JOB:
			evt = new ProxyRuntimeRemoveJobEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.REMOVE_MACHINE:
			evt = new ProxyRuntimeRemoveMachineEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.REMOVE_NODE:
			evt = new ProxyRuntimeRemoveNodeEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.REMOVE_PROCESS:
			evt = new ProxyRuntimeRemoveProcessEvent(packet.getTransID(), packet.getArgs());
			break;

		case IProxyRuntimeEvent.REMOVE_QUEUE:
			evt = new ProxyRuntimeRemoveQueueEvent(packet.getTransID(), packet.getArgs());
			break;
		}

		return evt;
	}
}
