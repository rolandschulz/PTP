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

package org.eclipse.ptp.rtsystem.proxy.event;

import org.eclipse.ptp.core.proxy.event.IProxyEvent;
import org.eclipse.ptp.core.proxy.event.ProxyEventFactory;

public class ProxyRuntimeEventFactory extends ProxyEventFactory {
	public IProxyEvent toEvent(int type, int transID, String[] args) {
		IProxyRuntimeEvent	evt = null;

		IProxyEvent e = super.toEvent(type, transID, args);
		if (e != null) {
			return e;
		}
		
		switch (type) {
		case IProxyRuntimeEvent.PROXY_RUNTIME_ATTR_DEF_EVENT:
			evt = new ProxyRuntimeAttributeDefEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_NEW_JOB_EVENT:
			evt = new ProxyRuntimeNewJobEvent(transID, args);
			break;
		
		case IProxyRuntimeEvent.PROXY_RUNTIME_NEW_MACHINE_EVENT:
			evt = new ProxyRuntimeNewMachineEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_NEW_NODE_EVENT:
			evt = new ProxyRuntimeNewNodeEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_NEW_PROCESS_EVENT:
			evt = new ProxyRuntimeNewProcessEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_NEW_QUEUE_EVENT:
			evt = new ProxyRuntimeNewQueueEvent(transID, args);
			break;
			
		case IProxyRuntimeEvent.PROXY_RUNTIME_JOB_CHANGE_EVENT:
			evt = new ProxyRuntimeJobChangeEvent(transID, args);
			break;
		
		case IProxyRuntimeEvent.PROXY_RUNTIME_MACHINE_CHANGE_EVENT:
			evt = new ProxyRuntimeMachineChangeEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_NODE_CHANGE_EVENT:
			evt = new ProxyRuntimeNodeChangeEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_PROCESS_CHANGE_EVENT:
			evt = new ProxyRuntimeProcessChangeEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_QUEUE_CHANGE_EVENT:
			evt = new ProxyRuntimeQueueChangeEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_REMOVE_ALL_EVENT:
			evt = new ProxyRuntimeRemoveAllEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_REMOVE_JOB_EVENT:
			evt = new ProxyRuntimeRemoveJobEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_REMOVE_MACHINE_EVENT:
			evt = new ProxyRuntimeRemoveMachineEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_REMOVE_NODE_EVENT:
			evt = new ProxyRuntimeRemoveNodeEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_REMOVE_PROCESS_EVENT:
			evt = new ProxyRuntimeRemoveProcessEvent(transID, args);
			break;

		case IProxyRuntimeEvent.PROXY_RUNTIME_REMOVE_QUEUE_EVENT:
			evt = new ProxyRuntimeRemoveQueueEvent(transID, args);
			break;
		}


		return evt;
	}

}
