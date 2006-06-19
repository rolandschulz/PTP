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
package org.eclipse.ptp.debug.external.core.cdi;

import java.math.BigInteger;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;

import org.eclipse.ptp.debug.core.cdi.IPCDIEventManager;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDICreatedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDisconnectedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIExitedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIMemoryChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISignalChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIMemoryBlock;
import org.eclipse.ptp.debug.core.cdi.model.IPCDISignal;
import org.eclipse.ptp.debug.external.core.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.core.cdi.event.ChangedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.MemoryChangedEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.SignalChangedEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.MemoryBlock;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;

public class EventManager extends SessionObject implements IPCDIEventManager, Observer {
	List list = Collections.synchronizedList(new ArrayList(1));

	public void update(Observable o, Object arg) {
		IPCDIEvent event = (IPCDIEvent) arg;

		List cdiList = new ArrayList(1);		
		Session session = (Session)getSession();		
		if (event instanceof IPCDISuspendedEvent) {
			processSuspendedEvent((IPCDISuspendedEvent)event);
		}
		else if (event instanceof IPCDIResumedEvent) {
		}
		else if (event instanceof IPCDIExitedEvent) {
		}
		else if (event instanceof IPCDIDisconnectedEvent) {
		}
		else if (event instanceof IPCDICreatedEvent) {
		}
		else if (event instanceof IPCDIChangedEvent) {
			/*
			if (event instanceof IPCDIMemoryChangedEvent) {
				// We need to fire an event for all the register blocks that may contain the modified addresses.
				System.err.println("******* GOT IPCDIMemoryChangedEvent");
				MemoryManager mgr = session.getMemoryManager();
				try {
					IPCDIMemoryBlock[] blocks = (IPCDIMemoryBlock[])mgr.getMemoryBlocks((Target)event.getSource());
					MemoryChangedEvent memEvent = (MemoryChangedEvent)event;
					BigInteger[] addresses = memEvent.getAddresses();
					List new_addresses = new ArrayList(addresses.length);
					for (int i=0; i<blocks.length; i++) {
						if (blocks[i] instanceof MemoryBlock) {
							MemoryBlock block = (MemoryBlock)blocks[i];
							for (int j=0; j<addresses.length; j++) {
								if (block.contains(addresses[j]) && (! blocks[i].isFrozen() || block.isDirty())) {
									new_addresses.add(addresses[j]);
								}
							}
							cdiList.add(new MemoryChangedEvent(session, event.getAllProcesses(), event.getSource(), (BigInteger[]) new_addresses.toArray(new BigInteger[new_addresses.size()])));
							block.setDirty(false);
						}	
					}
				} catch (PCDIException e) {
					
				}
			}
			*/
			/*
			else if (event instanceof IPCDISignalChangedEvent) {
				System.err.println("******* GOT IPCDISignalChangedEvent");
				SignalChangedEvent sigEvent = (SignalChangedEvent)event;
				String name = sigEvent.getName();
				IPCDISignal signal = (IPCDISignal)event.getSource();
				if (name == null || name.length() == 0) {
					// Something change we do not know what
					// Let the signal manager handle it with an update().
					try {
						SignalManager sMgr = session.getSignalManager();
						sMgr.update((Target)signal.getTarget());
					} catch (PCDIException e) {
					}
				} else {
					cdiList.add(new ChangedEvent(session, event.getAllProcesses(), signal));
				}
			}
			*/
		}
		cdiList.add(event);
		
		// Fire the event;
		IPCDIEvent[] cdiEvents = (IPCDIEvent[])cdiList.toArray(new IPCDIEvent[0]);
		fireEvents(cdiEvents);
	}
	
	public EventManager(Session session) {
		super(session);
	}
	public void shutdown() {
		list.clear();
	}
	public void addEventListener(IPCDIEventListener listener) {
		if (!list.contains(listener))
			list.add(listener);
	}
	public void removeEventListener(IPCDIEventListener listener) {
		if (list.contains(listener))
			list.remove(listener);
	}
	public void removeEventListeners() {
		list.clear();
	}
	public synchronized void fireEvents(IPCDIEvent[] cdiEvents) {
		if (cdiEvents != null && cdiEvents.length > 0) {
			IPCDIEventListener[] listeners = (IPCDIEventListener[])list.toArray(new IPCDIEventListener[0]);
			for (int i = 0; i < listeners.length; i++) {
				listeners[i].handleDebugEvents(cdiEvents);
			}			
		}
	}
	
	boolean processSuspendedEvent(IPCDISuspendedEvent event) {
		Session session = (Session)getSession();
		SignalManager sigMgr = session.getSignalManager();
		/*
		VariableManager varMgr = session.getVariableManager();
		ExpressionManager expMgr  = session.getExpressionManager();		
		BreakpointManager bpMgr = session.getBreakpointManager();
		SourceManager srcMgr = session.getSourceManager();
		MemoryManager memMgr = session.getMemoryManager();
		*/
		int[] procs = event.getAllRegisteredProcesses().toArray();
		for (int i = 0; i < procs.length; i++) {
			Target currentTarget = (Target) session.getTarget(procs[i]);
			currentTarget.setSupended(true);
			/*
			if (processSharedLibEvent(event)) {
				return false;
			}
			if (processBreakpointHitEvent(event)) {
				return false;
			}
			*/
			int threadId = event.getThreadId();
			currentTarget.updateState(threadId);
			try {
				Thread cthread = (Thread)currentTarget.getCurrentThread();
				if (cthread != null) {
					cthread.getCurrentStackFrame();
				}
			} catch (PCDIException e1) {
				PTPDebugExternalPlugin.log(e1);
			}
			try {
				if (sigMgr.isAutoUpdate()) {
					sigMgr.update(currentTarget);
				}
				/**
				 * TODO not quite important
				if (varMgr.isAutoUpdate()) {
					varMgr.update(currentTarget);
				}
				if (expMgr.isAutoUpdate()) { 
					expMgr.update(currentTarget);
				}
				//if (bpMgr.isAutoUpdate()) {
					//bpMgr.update(currentTarget);
				//}
				if (srcMgr.isAutoUpdate()) {
					srcMgr.update(currentTarget);
				}
				if (memMgr.isAutoUpdate()) {
					memMgr.update(currentTarget);
				}
				*/
			} catch (PCDIException e) {
				e.printStackTrace();
			}
		}
		return true;
	}
}
