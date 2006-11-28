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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Observable;
import java.util.Observer;
import org.eclipse.cdt.debug.core.cdi.ICDILocator;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.ptp.core.PreferenceConstants;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.IPCDIBreakpointHit;
import org.eclipse.ptp.debug.core.cdi.IPCDIEndSteppingRange;
import org.eclipse.ptp.debug.core.cdi.IPCDIErrorInfo;
import org.eclipse.ptp.debug.core.cdi.IPCDIEventManager;
import org.eclipse.ptp.debug.core.cdi.IPCDIInferiorSignaled;
import org.eclipse.ptp.debug.core.cdi.IPCDILineLocation;
import org.eclipse.ptp.debug.core.cdi.PCDIException;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIChangedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDICreatedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDebugDestroyedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIDestroyedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIErrorEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEventListener;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIResumedEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDIBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocationBreakpoint;
import org.eclipse.ptp.debug.core.cdi.model.IPCDILocator;
import org.eclipse.ptp.debug.core.events.IPDebugEvent;
import org.eclipse.ptp.debug.core.events.IPDebugInfo;
import org.eclipse.ptp.debug.core.events.PDebugErrorInfo;
import org.eclipse.ptp.debug.core.events.PDebugEvent;
import org.eclipse.ptp.debug.core.events.PDebugInfo;
import org.eclipse.ptp.debug.core.events.PDebugSuspendInfo;
import org.eclipse.ptp.debug.external.core.PTPDebugExternalPlugin;
import org.eclipse.ptp.debug.external.core.cdi.event.BreakpointHitEvent;
import org.eclipse.ptp.debug.external.core.cdi.event.EndSteppingRangeEvent;
import org.eclipse.ptp.debug.external.core.cdi.model.Target;
import org.eclipse.ptp.debug.external.core.cdi.model.Thread;

public class EventManager extends SessionObject implements IPCDIEventManager, Observer {
	List list = Collections.synchronizedList(new ArrayList());
	protected final Object lock = new Object(); 

	public EventManager(Session session) {
		super(session);
	}
	public void shutdown() {
		list.clear();
	}
	public void update(Observable o, Object arg) {
		IPCDIEvent event = (IPCDIEvent) arg;
		fireDebugEvent(event);
		fireEventRegistered(event);
	}
	private void fireEventRegistered(final IPCDIEvent event) {
		Job aJob = new Job("Updating registered process...") {
			protected IStatus run(IProgressMonitor monitor) {
				int[] procs = event.getAllRegisteredProcesses().toArray();
				if (procs.length == 0) {
					monitor.done();
					return Status.OK_STATUS;
				}

				if (event instanceof IPCDISuspendedEvent) {
					processSuspendedEvent((IPCDISuspendedEvent)event, procs, monitor);
				} else if (event instanceof IPCDIResumedEvent) {
					processRunningEvent((IPCDIResumedEvent)event, procs, monitor);
				}
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
				// Fire the event;
				fireEvents(new IPCDIEvent[] { event } );
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		aJob.setSystem(true);
		aJob.setPriority(Job.INTERACTIVE);
		aJob.schedule();
	}
	public void addEventListener(IPCDIEventListener listener) {
		//if (!list.contains(listener))
			list.add(listener);
	}
	public void removeEventListener(IPCDIEventListener listener) {
		//if (list.contains(listener))
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
	boolean processSuspendedEvent(IPCDISuspendedEvent event, int[] procs, IProgressMonitor monitor) {
		monitor.beginTask("Suspending registered process...", procs.length);
		Session session = (Session)getSession();
		SignalManager sigMgr = session.getSignalManager();
		VariableManager varMgr = session.getVariableManager();
		/*
		ExpressionManager expMgr  = session.getExpressionManager();		
		SourceManager srcMgr = session.getSourceManager();
		MemoryManager memMgr = session.getMemoryManager();
		*/
		for (int i=0; i<procs.length; i++) {
			try {
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
				currentTarget.updateState(event.getThreadId());
				try {
					Thread cthread = (Thread)currentTarget.getCurrentThread();
					if (cthread != null) {
						cthread.getCurrentStackFrame();
					}
					if (sigMgr.isAutoUpdate()) {
						sigMgr.update(currentTarget);
					}
					if (varMgr.isAutoUpdate()) {
						varMgr.update(currentTarget, event.getVarChanges());
					}
					/**
					 * TODO not quite important
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
					PTPDebugExternalPlugin.log(e);
				}
			} finally {
				monitor.worked(1);
			}
		}
		monitor.done();
		return true;
	}
	/**
	 * Do any processing of before a running event.
	 */
	boolean processRunningEvent(IPCDIResumedEvent event, int[] procs, IProgressMonitor monitor) {
		monitor.beginTask("Resuming registered process...", procs.length);
		Session session = (Session)getSession();
		
		for (int i = 0; i < procs.length; i++) {
			try {
				Target currentTarget = (Target) session.getTarget(procs[i]);
				currentTarget.setSupended(false);
			} finally {
				monitor.worked(1);
			}
		}
		monitor.done();
		return true;
	}	
	/***********************************************************************
	 * Debug Event
	 ***********************************************************************/
	protected IPDebugInfo getDebugInfo(IPCDIEvent event) {
		return new PDebugInfo(event.getDebugJob(), event.getAllProcesses(), event.getAllRegisteredProcesses(), event.getAllUnregisteredProcesses());
	}
	protected void fireDebugEvent(IPCDIEvent event) {
		if (event instanceof IPCDISuspendedEvent) {
			fireSuspendEvent((IPCDISuspendedEvent)event);
		}
		else if (event instanceof IPCDIResumedEvent) {
			fireResumeEvent((IPCDIResumedEvent)event);
		}
		else if (event instanceof IPCDIDebugDestroyedEvent) {
			fireDestroyEvent((IPCDIDestroyedEvent)event, IPDebugEvent.DEBUGGER);
			getSession().shutdown();
		}
		else if (event instanceof IPCDIDestroyedEvent) {
			fireDestroyEvent((IPCDIDestroyedEvent)event, IPDebugEvent.UNSPECIFIED);
		}
		else if (event instanceof IPCDIErrorEvent) {
			fireErrorEvent((IPCDIErrorEvent)event);
		}
		else if (event instanceof IPCDICreatedEvent) {
			fireCreateEvent((IPCDICreatedEvent)event);
		}
		else if (event instanceof IPCDIChangedEvent) {
			fireChangeEvent((IPCDIChangedEvent)event);
		}
	}
	public void fireSuspendEvent(IPCDISuspendedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event);
		int detail = IPDebugEvent.UNSPECIFIED;
		
		int lineNumber = 0;
		String fileName = (String) baseInfo.getJob().getAttribute(PreferenceConstants.JOB_DEBUG_DIR) + "/";
		if (event instanceof BreakpointHitEvent) {
			IPCDIBreakpoint bpt = ((IPCDIBreakpointHit) ((IPCDISuspendedEvent) event).getReason()).getBreakpoint();
			if (bpt instanceof IPCDILocationBreakpoint) {
				IPCDILocator locator = ((IPCDILocationBreakpoint) bpt).getLocator();
				lineNumber = locator.getLineNumber();
				fileName += locator.getFile();
			}
			detail = IPDebugEvent.BREAKPOINT;
		} 
		else if (event instanceof EndSteppingRangeEvent) {
			IPCDILineLocation lineLocation = ((IPCDIEndSteppingRange) ((IPCDISuspendedEvent) event).getReason()).getLineLocation();
			if (lineLocation != null) {
				lineNumber = lineLocation.getLineNumber();
				fileName += lineLocation.getFile();
			}
			detail = IPDebugEvent.STEP_END;
		}
		else {
			ICDILocator locator = ((IPCDIInferiorSignaled) ((IPCDISuspendedEvent) event).getReason()).getLocator();
			if (locator != null) {
				lineNumber = locator.getLineNumber();
				fileName += locator.getFile();
			}
			detail = IPDebugEvent.UNSPECIFIED;
		}
		if (lineNumber == 0)
			lineNumber = 1;
		
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.SUSPEND, detail, new PDebugSuspendInfo(baseInfo, fileName, lineNumber)));
	}
	
	public void fireResumeEvent(IPCDIResumedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event);
		int detail = IPDebugEvent.UNSPECIFIED;

		switch (event.getType()) {
		case IPCDIResumedEvent.STEP_INTO:
		case IPCDIResumedEvent.STEP_INTO_INSTRUCTION:
			detail = IPDebugEvent.STEP_INTO;
			break;
		case IPCDIResumedEvent.STEP_OVER:
		case IPCDIResumedEvent.STEP_OVER_INSTRUCTION:
			detail = IPDebugEvent.STEP_OVER;
			break;
		case IPCDIResumedEvent.STEP_RETURN:
			detail = IPDebugEvent.STEP_RETURN;
			break;
		}
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.RESUME, detail, baseInfo));
	}

	public void fireDestroyEvent(IPCDIDestroyedEvent event, int detail) {
		IPDebugInfo baseInfo = getDebugInfo(event);

		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.TERMINATE, detail, baseInfo));
	}
	public void fireErrorEvent(IPCDIErrorEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event);
		int detail = IPDebugEvent.UNSPECIFIED;

		switch (event.getErrorCode()) {
		case IPCDIErrorEvent.DBG_NORMAL:
			detail = IPDebugEvent.ERR_NORMAL;
			break;
		case IPCDIErrorEvent.DBG_WARNING:
			detail = IPDebugEvent.ERR_WARNING;
			break;
		case IPCDIErrorEvent.DBG_FATAL:
			detail = IPDebugEvent.ERR_FATAL;
			break;
		}
		
		IPCDIErrorInfo errInfo = (IPCDIErrorInfo)event.getReason();
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.ERROR, detail, new PDebugErrorInfo(baseInfo, errInfo.getMessage(), errInfo.getDetailMessage())));
	}
	public void fireChangeEvent(IPCDIChangedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event);
		int detail = IPDebugEvent.UNSPECIFIED;
		
		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.CHANGE, detail, baseInfo));
	}
	public void fireCreateEvent(IPCDICreatedEvent event) {
		IPDebugInfo baseInfo = getDebugInfo(event);
		int detail = IPDebugEvent.UNSPECIFIED;

		PTPDebugCorePlugin.getDefault().fireDebugEvent(new PDebugEvent(getSession(), IPDebugEvent.CREATE, detail, baseInfo));
	}
}
