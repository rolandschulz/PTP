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
/*******************************************************************************
 * Copyright (c) 2000, 2004 QNX Software Systems and others.
 * All rights reserved. This program and the accompanying materials 
 * are made available under the terms of the Common Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/cpl-v10.html
 * 
 * Contributors:
 *     QNX Software Systems - Initial API and implementation
 *******************************************************************************/
package org.eclipse.ptp.debug.internal.core.model;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;
import org.eclipse.cdt.core.CCorePlugin;
import org.eclipse.cdt.core.IAddress;
import org.eclipse.cdt.core.IAddressFactory;
import org.eclipse.cdt.core.IBinaryParser.IBinaryObject;
import org.eclipse.cdt.core.IBinaryParser.ISymbol;
import org.eclipse.cdt.debug.core.CDebugUtils;
import org.eclipse.cdt.debug.core.ICGlobalVariableManager;
import org.eclipse.cdt.debug.core.cdi.CDIException;
import org.eclipse.cdt.debug.core.cdi.ICDIAddressLocation;
import org.eclipse.cdt.debug.core.cdi.ICDIBreakpointHit;
import org.eclipse.cdt.debug.core.cdi.ICDIEndSteppingRange;
import org.eclipse.cdt.debug.core.cdi.ICDIErrorInfo;
import org.eclipse.cdt.debug.core.cdi.ICDIFunctionLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILineLocation;
import org.eclipse.cdt.debug.core.cdi.ICDILocation;
import org.eclipse.cdt.debug.core.cdi.ICDISession;
import org.eclipse.cdt.debug.core.cdi.ICDISessionConfiguration;
import org.eclipse.cdt.debug.core.cdi.ICDISessionObject;
import org.eclipse.cdt.debug.core.cdi.ICDISharedLibraryEvent;
import org.eclipse.cdt.debug.core.cdi.ICDISignalReceived;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointScope;
import org.eclipse.cdt.debug.core.cdi.ICDIWatchpointTrigger;
import org.eclipse.cdt.debug.core.cdi.event.ICDIChangedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDICreatedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDestroyedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIDisconnectedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIEventListener;
import org.eclipse.cdt.debug.core.cdi.event.ICDIExitedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIRestartedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDIResumedEvent;
import org.eclipse.cdt.debug.core.cdi.event.ICDISuspendedEvent;
import org.eclipse.cdt.debug.core.cdi.model.ICDIBreakpoint;
import org.eclipse.cdt.debug.core.cdi.model.ICDIObject;
import org.eclipse.cdt.debug.core.cdi.model.ICDITargetConfiguration;
import org.eclipse.cdt.debug.core.cdi.model.ICDIThread;
import org.eclipse.cdt.debug.core.cdi.model.ICDIVariableDescriptor;
import org.eclipse.cdt.debug.core.model.CDebugElementState;
import org.eclipse.cdt.debug.core.model.ICDebugElement;
import org.eclipse.cdt.debug.core.model.ICDebugElementStatus;
import org.eclipse.cdt.debug.core.model.ICGlobalVariable;
import org.eclipse.cdt.debug.core.model.ICLineBreakpoint;
import org.eclipse.cdt.debug.core.model.ICModule;
import org.eclipse.cdt.debug.core.model.ICSignal;
import org.eclipse.cdt.debug.core.model.IDebuggerProcessSupport;
import org.eclipse.cdt.debug.core.model.IDisassembly;
import org.eclipse.cdt.debug.core.model.IExecFileInfo;
import org.eclipse.cdt.debug.core.model.IGlobalVariableDescriptor;
import org.eclipse.cdt.debug.core.model.IPersistableRegisterGroup;
import org.eclipse.cdt.debug.core.model.IRegisterDescriptor;
import org.eclipse.cdt.debug.core.sourcelookup.ICSourceLocator;
import org.eclipse.cdt.debug.core.sourcelookup.ISourceLookupChangeListener;
import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Preferences;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.Preferences.IPropertyChangeListener;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IExpressionListener;
import org.eclipse.debug.core.IExpressionManager;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IExpression;
import org.eclipse.debug.core.model.IMemoryBlock;
import org.eclipse.debug.core.model.IProcess;
import org.eclipse.debug.core.model.IRegisterGroup;
import org.eclipse.debug.core.model.ISourceLocator;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.debug.core.sourcelookup.ISourceContainer;
import org.eclipse.debug.core.sourcelookup.ISourceLookupDirector;
import org.eclipse.debug.core.sourcelookup.ISourceLookupParticipant;
import org.eclipse.debug.core.sourcelookup.containers.FolderSourceContainer;
import org.eclipse.debug.core.sourcelookup.containers.ProjectSourceContainer;
import org.eclipse.ptp.core.IPProcess;
import org.eclipse.ptp.debug.core.PTPDebugCorePlugin;
import org.eclipse.ptp.debug.core.cdi.event.IPCDIEvent;
import org.eclipse.ptp.debug.core.cdi.event.IPCDISuspendedEvent;
import org.eclipse.ptp.debug.core.cdi.model.IPCDITarget;
import org.eclipse.ptp.debug.core.launch.IPLaunch;
import org.eclipse.ptp.debug.core.model.IPBreakpoint;
import org.eclipse.ptp.debug.core.model.IPDebugTarget;
import org.eclipse.ptp.debug.core.sourcelookup.CDirectorySourceContainer;
import org.eclipse.ptp.debug.internal.core.IPDebugInternalConstants;
import org.eclipse.ptp.debug.internal.core.PGlobalVariableManager;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CSourceLookupParticipant;
import org.eclipse.ptp.debug.internal.core.sourcelookup.CSourceManager;

public class PDebugTarget extends PDebugElement implements IPDebugTarget, ICDIEventListener, ILaunchListener, IExpressionListener, ISourceLookupChangeListener {
	private final String PROCESS_NAME = "Process ";
	private ArrayList fThreads;
	private IProcess fDebuggeeProcess = null;
	private IPCDITarget fCDITarget;
	private IPProcess process;
	private IPLaunch fLaunch;
	private ICDITargetConfiguration fConfig;
	private PGlobalVariableManager fGlobalVariableManager;
	private IBinaryObject fBinaryFile;
	private Boolean fIsLittleEndian = null;
	private Preferences fPreferences = null;
	private IAddressFactory fAddressFactory;

	public PDebugTarget(IPLaunch launch, IPCDITarget cdiTarget, IProcess debuggeeProcess, IBinaryObject file, boolean allowsTerminate, boolean allowsDisconnect) {
		super(null);
		fLaunch = launch;
		fCDITarget = cdiTarget;
		process = cdiTarget.getPProcess();
		setDebugTarget(this);
		setProcess(debuggeeProcess);
		setExecFile(file);
		initializePreferences();
		setConfiguration(cdiTarget.getConfiguration());
		if (!process.getStatus().equals(IPProcess.EXITED)) {
			setState(CDebugElementState.SUSPENDED);
			setThreadList(new ArrayList(5));
			setGlobalVariableManager(new PGlobalVariableManager(this));
			initialize();	
			DebugPlugin.getDefault().getLaunchManager().addLaunchListener(this);
			DebugPlugin.getDefault().getExpressionManager().addExpressionListener(this);
			getCDISession().getEventManager().addEventListener(this);
		} else {
			setState(CDebugElementState.TERMINATED);
			setThreadList(new ArrayList(5));
		}
	}
	public int getTargetID() {
		return fCDITarget.getTargetID();
	}
	protected void initialize() {
		initializeSourceLookupPath();
		ArrayList debugEvents = new ArrayList(1);
		debugEvents.add(createCreateEvent());
		initializeThreads(debugEvents);
		initializeSourceManager();
		fireEventSet((DebugEvent[]) debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}
	protected void initializeThreads(List debugEvents) {
		ICDIThread[] cdiThreads = new ICDIThread[0];
		try {
			cdiThreads = getCDITarget().getThreads();
		} catch (CDIException e) {
			// ignore
		}
		DebugEvent suspendEvent = null;
		for (int i = 0; i < cdiThreads.length; ++i) {
			CThread thread = createThread(cdiThreads[i]);
			debugEvents.add(thread.createCreateEvent());
			try {
				if (cdiThreads[i].equals(getCDITarget().getCurrentThread()) && thread.isSuspended()) {
					// Use BREAKPOINT as a detail to force perspective switch
					suspendEvent = thread.createSuspendEvent(DebugEvent.BREAKPOINT);
				}
			} catch (CDIException e) {
				// ignore
			}
		}
		if (suspendEvent != null) {
			debugEvents.add(suspendEvent);
		}
	}
	protected void initializeSourceManager() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if (locator instanceof IAdaptable) {
			ICSourceLocator clocator = (ICSourceLocator) ((IAdaptable) locator).getAdapter(ICSourceLocator.class);
			if (clocator instanceof IAdaptable) {
				CSourceManager sm = (CSourceManager) ((IAdaptable) clocator).getAdapter(CSourceManager.class);
				if (sm != null)
					sm.setDebugTarget(this);
			}
			IResourceChangeListener listener = (IResourceChangeListener) ((IAdaptable) locator).getAdapter(IResourceChangeListener.class);
			if (listener != null)
				CCorePlugin.getWorkspace().addResourceChangeListener(listener);
		}
	}
	protected void initializeSourceLookupPath() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if (locator instanceof ISourceLookupDirector) {
			ISourceLookupParticipant[] participants = ((ISourceLookupDirector) locator).getParticipants();
			for (int i = 0; i < participants.length; ++i) {
				if (participants[i] instanceof CSourceLookupParticipant) {
					((CSourceLookupParticipant) participants[i]).addSourceLookupChangeListener(this);
				}
			}
			setSourceLookupPath(((ISourceLookupDirector) locator).getSourceContainers());
		}
	}
	public IProcess getProcess() {
		return fDebuggeeProcess;
	}
	protected void setProcess(IProcess debuggeeProcess) {
		fDebuggeeProcess = debuggeeProcess;
	}
	public IThread[] getThreads() {
		List threads = getThreadList();
		return (IThread[]) threads.toArray(new IThread[threads.size()]);
	}
	public boolean hasThreads() throws DebugException {
		return getThreadList().size() > 0;
	}
	public String getName() throws DebugException {
		return PROCESS_NAME + getTargetID();
	}
	public boolean supportsBreakpoint(IBreakpoint breakpoint) {
		if (!getConfiguration().supportsBreakpoints())
			return false;
		// return (breakpoint instanceof IPBreakpoint && getBreakpointManager().supportsBreakpoint( (IPBreakpoint)breakpoint ));
		return (breakpoint instanceof IPBreakpoint);
	}
	public void launchRemoved(ILaunch launch) {
		if (!isAvailable()) {
			return;
		}
		if (launch.equals(getLaunch())) {
			disconnected();
		}
	}
	public void launchAdded(ILaunch launch) {}
	public void launchChanged(ILaunch launch) {}

	public boolean canTerminate() {
		return supportsTerminate() && isAvailable();
	}
	public boolean isTerminated() {
		return (getState().equals(CDebugElementState.TERMINATED));
	}
	public void terminate() throws DebugException {
		if (!canTerminate()) {
			return;
		}
		changeState(CDebugElementState.TERMINATING);
		try {
			getCDITarget().terminate();
		} catch (CDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public boolean canResume() {
		return getConfiguration().supportsResume() && isSuspended();
	}
	public boolean canSuspend() {
		if (!getConfiguration().supportsSuspend())
			return false;
		if (getState().equals(CDebugElementState.RESUMED)) {
			// only allow suspend if no threads are currently suspended
			IThread[] threads = getThreads();
			for (int i = 0; i < threads.length; i++) {
				if (threads[i].isSuspended()) {
					return false;
				}
			}
			return true;
		}
		return false;
	}
	public boolean isSuspended() {
		return (getState().equals(CDebugElementState.SUSPENDED));
	}
	public void resume() throws DebugException {
		if (!canResume())
			return;
		changeState(CDebugElementState.RESUMING);
		try {
			getCDITarget().resume(false);
		} catch (CDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public void suspend() throws DebugException {
		if (!canSuspend())
			return;
		changeState(CDebugElementState.SUSPENDING);
		try {
			getCDITarget().suspend();
		} catch (CDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}
	protected boolean isSuspending() {
		return (getState().equals(CDebugElementState.SUSPENDING));
	}
	protected void suspendThreads(ICDISuspendedEvent event) {
		Iterator it = getThreadList().iterator();
		while (it.hasNext()) {
			CThread thread = (CThread) it.next();
			ICDIThread suspensionThread = null;
			try {
				suspensionThread = getCDITarget().getCurrentThread();
			} catch (CDIException e) {
				// ignore
			}
			thread.suspendByTarget(event.getReason(), suspensionThread);
		}
	}
	protected synchronized List refreshThreads() {
		ArrayList newThreads = new ArrayList(5);
		ArrayList list = new ArrayList(5);
		ArrayList debugEvents = new ArrayList(5);
		List oldList = (List) getThreadList().clone();
		ICDIThread[] cdiThreads = new ICDIThread[0];
		ICDIThread currentCDIThread = null;
		try {
			cdiThreads = getCDITarget().getThreads();
			currentCDIThread = getCDITarget().getCurrentThread();
		} catch (CDIException e) {
		}
		for (int i = 0; i < cdiThreads.length; ++i) {
			CThread thread = findThread(oldList, cdiThreads[i]);
			if (thread == null) {
				thread = new CThread(this, cdiThreads[i]);
				newThreads.add(thread);
			} else {
				oldList.remove(thread);
			}
			thread.setCurrent(cdiThreads[i].equals(currentCDIThread));
			list.add(thread);
		}
		Iterator it = oldList.iterator();
		while (it.hasNext()) {
			CThread thread = (CThread) it.next();
			thread.terminated();
			debugEvents.add(thread.createTerminateEvent());
		}
		setThreadList(list);
		it = newThreads.iterator();
		while (it.hasNext()) {
			debugEvents.add(((CThread) it.next()).createCreateEvent());
		}
		if (debugEvents.size() > 0)
			fireEventSet((DebugEvent[]) debugEvents.toArray(new DebugEvent[debugEvents.size()]));
		return newThreads;
	}
	protected synchronized void resumeThreads(List debugEvents, int detail) {
		Iterator it = getThreadList().iterator();
		while (it.hasNext()) {
			((CThread) it.next()).resumedByTarget(detail, debugEvents);
		}
	}

	public void breakpointAdded( IBreakpoint breakpoint ) {}
	public void breakpointRemoved( IBreakpoint breakpoint, IMarkerDelta delta ) {}
	public void breakpointChanged( IBreakpoint breakpoint, IMarkerDelta delta ) {}
	
	protected boolean supportsDisconnect() {
		//No Discount
		//return getConfiguration().supportsDisconnect();
		return false;
	}
	protected boolean supportsTerminate() {
		return getConfiguration().supportsTerminate();
	}
	public boolean canDisconnect() {
		//No disconnet
		//return supportsDisconnect() && isAvailable();
		return false;
	}
	public void disconnect() throws DebugException {
		if (isDisconnecting()) {
			return;
		}
		changeState(CDebugElementState.DISCONNECTING);
		try {
			getCDITarget().disconnect();
		} catch (CDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), null);
		}
	}
	public boolean isDisconnected() {
		return (getState().equals(CDebugElementState.DISCONNECTED));
	}
	public boolean supportsStorageRetrieval() {
		return false;
	}
	public IMemoryBlock getMemoryBlock(long startAddress, long length) throws DebugException {
		return null;
	}
	public ILaunch getLaunch() {
		return fLaunch;
	}
	protected ArrayList getThreadList() {
		return fThreads;
	}
	private void setThreadList(ArrayList threads) {
		fThreads = threads;
	}
	public Object getAdapter(Class adapter) {
		if (adapter.equals(ICDebugElement.class))
			return this;
		if (adapter.equals(PDebugElement.class))
			return this;
		if (adapter.equals(IDebugTarget.class))
			return this;
		if (adapter.equals(IPDebugTarget.class))
			return this;
		if (adapter.equals(PDebugTarget.class))
			return this;
		if (adapter.equals(IPCDITarget.class))
			return fCDITarget;
		if (adapter.equals(IDebuggerProcessSupport.class))
			return this;
		if (adapter.equals(IExecFileInfo.class))
			return this;
		if (adapter.equals(ICGlobalVariableManager.class))
			return getGlobalVariableManager();
		if (adapter.equals(ICDISession.class))
			return getCDISession();
		return super.getAdapter(adapter);
	}
	public void handleDebugEvents(ICDIEvent[] events) {		
		for (int i = 0; i < events.length; i++) {
			IPCDIEvent event = (IPCDIEvent)events[i];
			if (!event.containTask(getTargetID()))
				return;
			
			ICDIObject source = event.getSource(getTargetID());
			if (source == null && event instanceof ICDIDestroyedEvent) {
				handleTerminatedEvent((ICDIDestroyedEvent) event, source);
			}
			else if (source != null && source.getTarget().equals(getCDITarget())) {
				if (event instanceof ICDICreatedEvent) {
					if (source instanceof ICDIThread) {
						handleThreadCreatedEvent((ICDICreatedEvent) event, source);
					}
				} else if (event instanceof IPCDISuspendedEvent) {
					if (source instanceof IPCDITarget) {
						handleSuspendedEvent((IPCDISuspendedEvent) event, source);
					}
				} else if (event instanceof ICDIResumedEvent) {
					if (source instanceof IPCDITarget) {
						handleResumedEvent((ICDIResumedEvent) event, source);
					}
				} else if (event instanceof ICDIExitedEvent) {
					if (source instanceof IPCDITarget) {
						handleExitedEvent((ICDIExitedEvent) event, source);
					}
				} else if (event instanceof ICDIDestroyedEvent) {
					if (source instanceof ICDIThread) {
						handleThreadTerminatedEvent((ICDIDestroyedEvent) event, source);
					}
				} else if (event instanceof ICDIDisconnectedEvent) {
					if (source instanceof IPCDITarget) {
						handleDisconnectedEvent((ICDIDisconnectedEvent) event, source);
					}
				} else if (event instanceof ICDIChangedEvent) {
					if (source instanceof IPCDITarget) {
						handleChangedEvent((ICDIChangedEvent) event, source);
					}
				} else if (event instanceof ICDIRestartedEvent) {
					if (source instanceof IPCDITarget) {
						handleRestartedEvent((ICDIRestartedEvent) event, source);
					}
				}
			}
		}
	}
	public boolean canRestart() {
		//return getConfiguration().supportsRestart() && isSuspended();
		//Cannot restart
		return false;
	}
	public void restart() throws DebugException {
		if (!canRestart()) {
			return;
		}
		changeState(CDebugElementState.RESTARTING);
		ICDILocation location = getCDITarget().createFunctionLocation("", "main");
		setInternalTemporaryBreakpoint(location);
		try {
			getCDITarget().restart();
		} catch (CDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), e);
		}
	}
	public boolean isAvailable() {
		return !(isTerminated() || isTerminating() || isDisconnected() || isDisconnecting());
	}
	protected boolean isTerminating() {
		return (getState().equals(CDebugElementState.TERMINATING));
	}
	protected void terminated() {
		if (!isTerminated()) {
			if (!isDisconnected()) {
				setState(CDebugElementState.TERMINATED);
			}
			cleanup();
			fireTerminateEvent();
		}
	}
	protected boolean isDisconnecting() {
		return (getState().equals(CDebugElementState.DISCONNECTING));
	}
	protected void disconnected() {
		if (!isDisconnected()) {
			setState(CDebugElementState.DISCONNECTED);
			cleanup();
			fireTerminateEvent();
		}
	}
	protected void cleanup() {
		resetStatus();
		removeAllThreads();
		getCDISession().getEventManager().removeEventListener(this);
		DebugPlugin.getDefault().getExpressionManager().removeExpressionListener(this);
		DebugPlugin.getDefault().getLaunchManager().removeLaunchListener(this);
		saveGlobalVariables();
		disposeGlobalVariableManager();
		disposeSourceManager();
		disposeSourceLookupPath();
		removeAllExpressions();
		disposePreferences();
	}
	protected void removeAllThreads() {
		List threads = getThreadList();
		setThreadList(new ArrayList(0));
		ArrayList debugEvents = new ArrayList(threads.size());
		Iterator it = threads.iterator();
		while (it.hasNext()) {
			CThread thread = (CThread) it.next();
			thread.terminated();
			debugEvents.add(thread.createTerminateEvent());
		}
		fireEventSet((DebugEvent[]) debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}
	protected void removeAllExpressions() {
		IExpressionManager em = DebugPlugin.getDefault().getExpressionManager();
		IExpression[] expressions = em.getExpressions();
		for (int i = 0; i < expressions.length; ++i) {
			if (expressions[i] instanceof CExpression && expressions[i].getDebugTarget().equals(this)) {
				em.removeExpression(expressions[i]);
			}
		}
	}
	protected CThread createThread(ICDIThread cdiThread) {
		CThread thread = new CThread(this, cdiThread);
		getThreadList().add(thread);
		return thread;
	}
	private void handleSuspendedEvent(IPCDISuspendedEvent event, ICDIObject source) {
		setState(CDebugElementState.SUSPENDED);
		ICDISessionObject reason = event.getReason();
		setCurrentStateInfo(reason);
		skipBreakpoints(false);
		List newThreads = refreshThreads();
		if (source instanceof IPCDITarget) {
			suspendThreads(event);
		}
		// We need this for debuggers that don't have notifications
		// for newly created threads.
		else if (source instanceof ICDIThread) {
			CThread thread = findThread((ICDIThread)source);
			if (thread != null && newThreads.contains(thread)) {
				ICDIEvent[] evts = new ICDIEvent[] { event };
				thread.handleDebugEvents(evts);
			}
		}
		if (reason instanceof ICDIEndSteppingRange) {
			handleEndSteppingRange((ICDIEndSteppingRange) reason);
		} else if (reason instanceof ICDIBreakpointHit) {
			handleBreakpointHit((ICDIBreakpointHit) reason);
		} else if (reason instanceof ICDISignalReceived) {
			handleSuspendedBySignal((ICDISignalReceived) reason);
		} else if (reason instanceof ICDIWatchpointTrigger) {
			handleWatchpointTrigger((ICDIWatchpointTrigger) reason);
		} else if (reason instanceof ICDIWatchpointScope) {
			handleWatchpointScope((ICDIWatchpointScope) reason);
		} else if (reason instanceof ICDIErrorInfo) {
			handleErrorInfo((ICDIErrorInfo) reason);
		} else if (reason instanceof ICDISharedLibraryEvent) {
			handleSuspendedBySolibEvent((ICDISharedLibraryEvent) reason);
		} else { // reason is not specified
			fireSuspendEvent(DebugEvent.UNSPECIFIED);
		}
	}
	private void handleResumedEvent(ICDIResumedEvent event, ICDIObject source) {
		setState(CDebugElementState.RESUMED);
		setCurrentStateInfo(null);
		resetStatus();
		ArrayList debugEvents = new ArrayList(10);
		int detail = DebugEvent.UNSPECIFIED;
		switch (event.getType()) {
		case ICDIResumedEvent.CONTINUE:
			detail = DebugEvent.CLIENT_REQUEST;
			break;
		case ICDIResumedEvent.STEP_INTO:
		case ICDIResumedEvent.STEP_INTO_INSTRUCTION:
			detail = DebugEvent.STEP_INTO;
			break;
		case ICDIResumedEvent.STEP_OVER:
		case ICDIResumedEvent.STEP_OVER_INSTRUCTION:
			detail = DebugEvent.STEP_OVER;
			break;
		case ICDIResumedEvent.STEP_RETURN:
			detail = DebugEvent.STEP_RETURN;
			break;
		}
		debugEvents.add(createResumeEvent(detail));
		resumeThreads(debugEvents, detail);
		fireEventSet((DebugEvent[]) debugEvents.toArray(new DebugEvent[debugEvents.size()]));
	}
	private void handleEndSteppingRange(ICDIEndSteppingRange endSteppingRange) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleBreakpointHit(ICDIBreakpointHit breakpointHit) {
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}
	private void handleWatchpointTrigger(ICDIWatchpointTrigger wt) {
		fireSuspendEvent(DebugEvent.BREAKPOINT);
	}
	private void handleWatchpointScope(ICDIWatchpointScope ws) {
	// DONNY
	//getBreakpointManager().watchpointOutOfScope( ws.getWatchpoint() ); fireSuspendEvent( DebugEvent.BREAKPOINT );
	}
	private void handleSuspendedBySignal(ICDISignalReceived signal) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleErrorInfo(ICDIErrorInfo info) {
		setStatus(ICDebugElementStatus.ERROR, (info != null) ? info.getMessage() : null);
		if (info != null) {
			MultiStatus status = new MultiStatus(PTPDebugCorePlugin.getUniqueIdentifier(), IPDebugInternalConstants.STATUS_CODE_ERROR, CoreModelMessages.getString("CDebugTarget.1"), null);
			StringTokenizer st = new StringTokenizer(info.getDetailMessage(), "\n\r");
			while (st.hasMoreTokens()) {
				String token = st.nextToken();
				if (token.length() > 200) {
					token = token.substring(0, 200);
				}
				status.add(new Status(IStatus.ERROR, status.getPlugin(), IPDebugInternalConstants.STATUS_CODE_ERROR, token, null));
			}
			CDebugUtils.error(status, this);
		}
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleSuspendedBySolibEvent(ICDISharedLibraryEvent solibEvent) {
		fireSuspendEvent(DebugEvent.UNSPECIFIED);
	}
	private void handleExitedEvent(ICDIExitedEvent event, ICDIObject source) {
		removeAllThreads();
		setState(CDebugElementState.EXITED);
		setCurrentStateInfo(event.getReason());
		fireChangeEvent(DebugEvent.CONTENT);
		ICDISessionConfiguration sessionConfig = getCDISession().getConfiguration();
		if (sessionConfig != null && sessionConfig.terminateSessionOnExit())
			terminated();
	}
	private void handleTerminatedEvent(ICDIDestroyedEvent event, ICDIObject source) {
		terminated();
	}
	private void handleDisconnectedEvent(ICDIDisconnectedEvent event, ICDIObject source) {
		disconnected();
	}
	private void handleChangedEvent(ICDIChangedEvent event, ICDIObject source) {}
	private void handleRestartedEvent(ICDIRestartedEvent event, ICDIObject source) {}
	private void handleThreadCreatedEvent(ICDICreatedEvent event, ICDIObject source) {
		ICDIThread cdiThread = (ICDIThread) source;
		CThread thread = findThread(cdiThread);
		if (thread == null) {
			thread = createThread(cdiThread);
			thread.fireCreationEvent();
		}
	}
	private void handleThreadTerminatedEvent(ICDIDestroyedEvent event, ICDIObject source) {
		ICDIThread cdiThread = (ICDIThread) source;
		CThread thread = findThread(cdiThread);
		if (thread != null) {
			getThreadList().remove(thread);
			thread.terminated();
			thread.fireTerminateEvent();
		}
	}
	public CThread findThread(ICDIThread cdiThread) {
		List threads = getThreadList();
		for (int i = 0; i < threads.size(); i++) {
			CThread t = (CThread) threads.get(i);
			if (t.getCDIThread().equals(cdiThread))
				return t;
		}
		return null;
	}
	public CThread findThread(List threads, ICDIThread cdiThread) {
		for (int i = 0; i < threads.size(); i++) {
			CThread t = (CThread) threads.get(i);
			if (t.getCDIThread().equals(cdiThread))
				return t;
		}
		return null;
	}
	protected ICDITargetConfiguration getConfiguration() {
		return fConfig;
	}
	private void setConfiguration(ICDITargetConfiguration config) {
		fConfig = config;
	}
	protected boolean supportsExpressionEvaluation() {
		return getConfiguration().supportsExpressionEvaluation();
	}
	public void expressionAdded(IExpression expression) {}
	public void expressionChanged(IExpression expression) {}
	public void expressionRemoved(IExpression expression) {
		if (expression instanceof CExpression && expression.getDebugTarget().equals(this)) {
			((CExpression) expression).dispose();
		}
	}
	public void setInternalTemporaryBreakpoint(ICDILocation location) throws DebugException {
		try {
			if (location instanceof ICDIFunctionLocation) {
				getCDITarget().setFunctionBreakpoint(ICDIBreakpoint.TEMPORARY, (ICDIFunctionLocation) location, null, false);
			} else if (location instanceof ICDILineLocation) {
				getCDITarget().setLineBreakpoint(ICDIBreakpoint.TEMPORARY, (ICDILineLocation) location, null, false);
			} else if (location instanceof ICDIAddressLocation) {
				getCDITarget().setAddressBreakpoint(ICDIBreakpoint.TEMPORARY, (ICDIAddressLocation) location, null, false);
			} else {
				// ???
				targetRequestFailed("not_a_location", null);
			}
		} catch (CDIException e) {
			targetRequestFailed(e.getMessage(), null);
		}
	}
	protected IThread getCurrentThread() throws DebugException {
		IThread[] threads = getThreads();
		for (int i = 0; i < threads.length; ++i) {
			if (((CThread) threads[i]).isCurrent())
				return threads[i];
		}
		return null;
	}
	protected ISourceLocator getSourceLocator() {
		return getLaunch().getSourceLocator();
	}
	public boolean isLittleEndian() {
		if (fIsLittleEndian == null) {
			fIsLittleEndian = Boolean.TRUE;
			IBinaryObject file = getBinaryFile();
			if (file != null) {
				fIsLittleEndian = new Boolean(file.isLittleEndian());
			}
		}
		return fIsLittleEndian.booleanValue();
	}
	public IBinaryObject getExecFile() {
		return getBinaryFile();
	}
	public IBinaryObject getBinaryFile() {
		return fBinaryFile;
	}
	private void setExecFile(IBinaryObject file) {
		fBinaryFile = file;
	}
	protected void saveGlobalVariables() {
		fGlobalVariableManager.save();
	}
	protected void disposeGlobalVariableManager() {
		fGlobalVariableManager.dispose();
	}
	public boolean canResumeWithoutSignal() {
		// Check if the configuration supports this!!!
		return (canResume() && getCurrentStateInfo() instanceof ICDISignalReceived);
	}
	public void resumeWithoutSignal() throws DebugException {
		if (!canResume())
			return;
		changeState(CDebugElementState.RESUMING);
		try {
			getCDITarget().resume(false);
		} catch (CDIException e) {
			restoreOldState();
			targetRequestFailed(e.getMessage(), e);
		}
	}
	protected void disposeSourceManager() {
		ISourceLocator locator = getSourceLocator();
		if (locator instanceof IAdaptable) {
			IResourceChangeListener listener = (IResourceChangeListener) ((IAdaptable) locator).getAdapter(IResourceChangeListener.class);
			if (listener != null)
				CCorePlugin.getWorkspace().removeResourceChangeListener(listener);
		}
	}
	protected void disposeSourceLookupPath() {
		ISourceLocator locator = getLaunch().getSourceLocator();
		if (locator instanceof ISourceLookupDirector) {
			ISourceLookupParticipant[] participants = ((ISourceLookupDirector) locator).getParticipants();
			for (int i = 0; i < participants.length; ++i) {
				if (participants[i] instanceof CSourceLookupParticipant) {
					((CSourceLookupParticipant) participants[i]).removeSourceLookupChangeListener(this);
				}
			}
		}
	}
	public String toString() {
		String result = "";
		try {
			result = getName();
		} catch (DebugException e) {
		}
		return result;
	}
	public ICSignal[] getSignals() throws DebugException {
		//TODO Not implement yet
		return new ICSignal[0];
	}
	public boolean hasSignals() throws DebugException {
		//TODO Not implement yet
		return false;
	}
	public IAddress getBreakpointAddress(ICLineBreakpoint breakpoint) throws DebugException {
		//TODO Not implement yet
		return null;
	}
	public void enableInstructionStepping(boolean enabled) {
		fPreferences.setValue(PREF_INSTRUCTION_STEPPING_MODE, enabled);
	}
	public boolean supportsInstructionStepping() {
		return getConfiguration().supportsInstructionStepping();
	}
	public boolean isInstructionSteppingEnabled() {
		//TODO Not implement yet
		//return fPreferences.getBoolean( PREF_INSTRUCTION_STEPPING_MODE ) || CDebugCorePlugin.getDefault().getPluginPreferences().getBoolean( ICDebugConstants.PREF_INSTRUCTION_STEP_MODE_ON );
		return false;
	}
	private void initializePreferences() {
		fPreferences = new Preferences();
		fPreferences.setDefault(PREF_INSTRUCTION_STEPPING_MODE, false);
	}
	private void disposePreferences() {
		fPreferences = null;
	}
	public void addPropertyChangeListener(IPropertyChangeListener listener) {
		if (fPreferences != null)
			fPreferences.addPropertyChangeListener(listener);
	}
	public void removePropertyChangeListener(IPropertyChangeListener listener) {
		if (fPreferences != null)
			fPreferences.removePropertyChangeListener(listener);
	}
	protected PGlobalVariableManager getGlobalVariableManager() {
		return fGlobalVariableManager;
	}
	private void setGlobalVariableManager(PGlobalVariableManager globalVariableManager) {
		fGlobalVariableManager = globalVariableManager;
	}
	public boolean isPostMortem() {
		return false;
	}
	public IAddressFactory getAddressFactory() {
		if (fAddressFactory == null) {
			if (getExecFile() != null) {
				IBinaryObject file = getBinaryFile();
				if (file != null) {
					fAddressFactory = file.getAddressFactory();
				}
			}
		}
		return fAddressFactory;
	}
	private void changeState(CDebugElementState state) {
		setState(state);
		Iterator it = getThreadList().iterator();
		while (it.hasNext()) {
			((CThread) it.next()).setState(state);
		}
	}
	protected void restoreOldState() {
		restoreState();
		Iterator it = getThreadList().iterator();
		while (it.hasNext()) {
			((CThread) it.next()).restoreState();
		}
	}
	protected void skipBreakpoints(boolean enabled) {
	// FIXME Donny
	// We do not have skipBreakpoints support for each individual target
	// getBreakpointManager().skipBreakpoints( enabled );
	}
	public IDisassembly getDisassembly() throws DebugException {
		//TODO Not implement yet
		return null;
	}
	public ICGlobalVariable createGlobalVariable(IGlobalVariableDescriptor info) throws DebugException {
		ICDIVariableDescriptor vo = null;
		try {
			vo = getCDITarget().getGlobalVariableDescriptors(info.getPath().lastSegment(), null, info.getName());
		} catch (CDIException e) {
			throw new DebugException(new Status(IStatus.ERROR, PTPDebugCorePlugin.getUniqueIdentifier(), DebugException.TARGET_REQUEST_FAILED, (vo != null) ? vo.getName() + ": " + e.getMessage() : e.getMessage(), null)); //$NON-NLS-1$
		}
		return CVariableFactory.createGlobalVariable(this, info, vo);
	}
	public boolean hasModules() throws DebugException {
		//TODO Not implement yet
		return false;
	}
	public ICModule[] getModules() throws DebugException {
		//TODO Not implement yet
		return null;
	}
	public void loadSymbolsForAllModules() throws DebugException {
		//TODO Not implement yet
	}
	public IRegisterDescriptor[] getRegisterDescriptors() throws DebugException {
		//TODO Not implement yet
		return null;
	}
	public void addRegisterGroup(String name, IRegisterDescriptor[] descriptors) {
		//TODO Not implement yet
	}
	public void removeRegisterGroups(IRegisterGroup[] groups) {
		//TODO Not implement yet
	}
	public void modifyRegisterGroup(IPersistableRegisterGroup group, IRegisterDescriptor[] descriptors) {
		//TODO Not implement yet
	}
	public void restoreDefaultRegisterGroups() {
		//TODO Not implement yet
	}
	public IGlobalVariableDescriptor[] getGlobals() throws DebugException {
		ArrayList list = new ArrayList();
		IBinaryObject file = getBinaryFile();
		if (file != null) {
			list.addAll(getCFileGlobals(file));
		}
		return (IGlobalVariableDescriptor[]) list.toArray(new IGlobalVariableDescriptor[list.size()]);
	}
	private List getCFileGlobals(IBinaryObject file) throws DebugException {
		ArrayList list = new ArrayList();
		ISymbol[] symbols = file.getSymbols();
		for (int i = 0; i < symbols.length; ++i) {
			if (symbols[i].getType() == ISymbol.VARIABLE) {
				list.add(CVariableFactory.createGlobalVariableDescriptor(symbols[i]));
			}
		}
		return list;
	}
	public void sourceContainersChanged(ISourceLookupDirector director) {
		setSourceLookupPath(director.getSourceContainers());
	}
	private void setSourceLookupPath(ISourceContainer[] containers) {
		ArrayList list = new ArrayList(containers.length);
		getSourceLookupPath(list, containers);
		try {
			getCDITarget().setSourcePaths((String[]) list.toArray(new String[list.size()]));
		} catch (CDIException e) {
			PTPDebugCorePlugin.log(e);
		}
	}
	private void getSourceLookupPath(List list, ISourceContainer[] containers) {
		for (int i = 0; i < containers.length; ++i) {
			if (containers[i] instanceof ProjectSourceContainer) {
				IProject project = ((ProjectSourceContainer) containers[i]).getProject();
				if (project != null && project.exists())
					list.add(project.getLocation().toPortableString());
			}
			if (containers[i] instanceof FolderSourceContainer) {
				IContainer container = ((FolderSourceContainer) containers[i]).getContainer();
				if (container != null && container.exists())
					list.add(container.getLocation().toPortableString());
			}
			if (containers[i] instanceof CDirectorySourceContainer) {
				File dir = ((CDirectorySourceContainer) containers[i]).getDirectory();
				if (dir != null && dir.exists()) {
					IPath path = new Path(dir.getAbsolutePath());
					list.add(path.toPortableString());
				}
			}
			if (containers[i].isComposite()) {
				try {
					getSourceLookupPath(list, containers[i].getSourceContainers());
				} catch (CoreException e) {
					PTPDebugCorePlugin.log(e.getStatus());
				}
			}
		}
	}
}
