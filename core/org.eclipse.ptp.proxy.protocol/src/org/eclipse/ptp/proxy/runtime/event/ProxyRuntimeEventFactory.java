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

package org.eclipse.ptp.proxy.runtime.event;

import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeAttributeDefEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeConnectedStateEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeErrorStateEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeJobChangeEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeMachineChangeEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeMessageEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeNewJobEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeNewMachineEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeNewNodeEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeNewProcessEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeNewQueueEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeNodeChangeEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeProcessChangeEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeQueueChangeEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeRemoveAllEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeRemoveJobEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeRemoveMachineEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeRemoveNodeEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeRemoveProcessEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeRemoveQueueEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeRunningStateEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeShutdownStateEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeStartupErrorEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeSubmitJobErrorEvent;
import org.eclipse.ptp.internal.proxy.runtime.event.ProxyRuntimeTerminateJobErrorEvent;
import org.eclipse.ptp.proxy.event.IProxyEvent;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent;
import org.eclipse.ptp.proxy.event.ProxyEventFactory;
import org.eclipse.ptp.proxy.event.IProxyMessageEvent.Level;
import org.eclipse.ptp.proxy.packet.ProxyPacket;

public class ProxyRuntimeEventFactory extends ProxyEventFactory implements IProxyRuntimeEventFactory {
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeAttributeDefEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeAttributeDefEvent newProxyRuntimeAttributeDefEvent(int transID, String[] args) {
		return new ProxyRuntimeAttributeDefEvent(transID, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeConnectedStateEvent()
	 */
	public IProxyRuntimeConnectedStateEvent newProxyRuntimeConnectedStateEvent() {
		return new ProxyRuntimeConnectedStateEvent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeErrorStateEvent()
	 */
	public IProxyRuntimeErrorStateEvent newProxyRuntimeErrorStateEvent() {
		return new ProxyRuntimeErrorStateEvent();
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeJobChangeEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeJobChangeEvent newProxyRuntimeJobChangeEvent(int transID, String[] args) {
		return new ProxyRuntimeJobChangeEvent(transID, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeMachineChangeEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeMachineChangeEvent newProxyRuntimeMachineChangeEvent(int transID, String[] args) {
		return new ProxyRuntimeMachineChangeEvent(transID, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeMessageEvent(org.eclipse.ptp.proxy.event.IProxyMessageEvent)
	 */
	public IProxyRuntimeMessageEvent newProxyRuntimeMessageEvent(
			IProxyMessageEvent event) {
		return new ProxyRuntimeMessageEvent(event);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeMessageEvent(org.eclipse.ptp.proxy.event.IProxyMessageEvent.Level, java.lang.String)
	 */
	public IProxyRuntimeMessageEvent newProxyRuntimeMessageEvent(Level level,
			String message) {
		return new ProxyRuntimeMessageEvent(level, message);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeNewJobEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeNewJobEvent newProxyRuntimeNewJobEvent(int transID, String[] args) {
		return new ProxyRuntimeNewJobEvent(transID, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeNewMachineEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeNewMachineEvent newProxyRuntimeNewMachineEvent(int transID, String[] args) {
		return new ProxyRuntimeNewMachineEvent(transID, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeNewNodeEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeNewNodeEvent newProxyRuntimeNewNodeEvent(int transID, String[] args) {
		return new ProxyRuntimeNewNodeEvent(transID, args);
	}

	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeNewProcessEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeNewProcessEvent newProxyRuntimeNewProcessEvent(int transID, String[] args) {
		return new ProxyRuntimeNewProcessEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeNewQueueEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeNewQueueEvent newProxyRuntimeNewQueueEvent(int transID, String[] args) {
		return new ProxyRuntimeNewQueueEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeNodeChangeEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeNodeChangeEvent newProxyRuntimeNodeChangeEvent(int transID, String[] args) {
		return new ProxyRuntimeNodeChangeEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeProcessChangeEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeProcessChangeEvent newProxyRuntimeProcessChangeEvent(int transID, String[] args) {
		return new ProxyRuntimeProcessChangeEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeRemoveAllEventt(int, java.lang.String[])
	 */
	public IProxyRuntimeRemoveAllEvent newProxyRuntimeRemoveAllEventt(int transID, String[] args) {
		return new ProxyRuntimeRemoveAllEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeRemoveJobEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeRemoveJobEvent newProxyRuntimeRemoveJobEvent(int transID, String[] args) {
		return new ProxyRuntimeRemoveJobEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeRemoveMachineEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeRemoveMachineEvent newProxyRuntimeRemoveMachineEvent(int transID, String[] args) {
		return new ProxyRuntimeRemoveMachineEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeRemoveNodeEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeRemoveNodeEvent newProxyRuntimeRemoveNodeEvent(int transID, String[] args) {
		return new ProxyRuntimeRemoveNodeEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeRemoveProcessEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeRemoveProcessEvent newProxyRuntimeRemoveProcessEvent(int transID, String[] args) {
		return new ProxyRuntimeRemoveProcessEvent(transID, args);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeRemoveQueueEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeRemoveQueueEvent newProxyRuntimeRemoveQueueEvent(int transID, String[] args) {
		return new ProxyRuntimeRemoveQueueEvent(transID, args);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeRunningStateEvent()
	 */
	public IProxyRuntimeRunningStateEvent newProxyRuntimeRunningStateEvent() {
		return new ProxyRuntimeRunningStateEvent();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeShutdownStateEvent()
	 */
	public IProxyRuntimeShutdownStateEvent newProxyRuntimeShutdownStateEvent() {
		return new ProxyRuntimeShutdownStateEvent();
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeStartupErrorEvent(java.lang.String)
	 */
	public IProxyRuntimeStartupErrorEvent newProxyRuntimeStartupErrorEvent(
			String message) {
		return new ProxyRuntimeStartupErrorEvent(message);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeStartupErrorEvent(java.lang.String[])
	 */
	public IProxyRuntimeStartupErrorEvent newProxyRuntimeStartupErrorEvent(
			String[] args) {
		return new ProxyRuntimeStartupErrorEvent(args);
	}	
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeSubmitJobErrorEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeSubmitJobErrorEvent newProxyRuntimeSubmitJobErrorEvent(
			int transID, String[] args) {
		return new ProxyRuntimeSubmitJobErrorEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.runtime.event.IProxyRuntimeEventFactory#newProxyRuntimeTerminateJobErrorEvent(int, java.lang.String[])
	 */
	public IProxyRuntimeTerminateJobErrorEvent newProxyRuntimeTerminateJobErrorEvent(
			int transID, String[] args) {
		return new ProxyRuntimeTerminateJobErrorEvent(transID, args);
	}
	
	/* (non-Javadoc)
	 * @see org.eclipse.ptp.proxy.event.ProxyEventFactory#toEvent(org.eclipse.ptp.proxy.packet.ProxyPacket)
	 */
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
